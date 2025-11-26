package edu.courseregistration.core;

import edu.courseregistration.engine.*;
import edu.courseregistration.model.*;
import edu.courseregistration.repository.*;
import edu.courseregistration.result.RegistrationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Registrar.
 * Tests complete workflows with all components working together.
 */
class RegistrarTest {
    private Registrar registrar;
    private StudentRepository studentRepository;
    private CourseRepository courseRepository;
    private EnrollmentRepository enrollmentRepository;
    private PrerequisiteEngine prerequisiteEngine;
    private SeatAllocator seatAllocator;
    private WaitlistManager waitlistManager;

    @BeforeEach
    void setUp() {
        // Initialize repositories
        studentRepository = new StudentRepository();
        courseRepository = new CourseRepository();
        enrollmentRepository = new EnrollmentRepository();

        // Initialize engines
        prerequisiteEngine = new PrerequisiteEngine(courseRepository, studentRepository);
        seatAllocator = new SeatAllocator(enrollmentRepository);
        waitlistManager = new WaitlistManager(seatAllocator);

        // Set dependencies for student repository cascade delete
        studentRepository.setEnrollmentRepository(enrollmentRepository);
        studentRepository.setWaitlistManager(waitlistManager);
        studentRepository.setCourseRepository(courseRepository);

        // Initialize registrar
        registrar = new Registrar(studentRepository, courseRepository, enrollmentRepository,
                prerequisiteEngine, seatAllocator, waitlistManager);
    }

    // ========== Happy Path Tests ==========

    @Test
    void testRegisterStudent_HappyPath_Success() {
        // Setup
        Student student = new Student("S001", "John Doe", "CS", 1);
        studentRepository.save(student);

        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        courseRepository.save(course);

        // Register
        RegistrationResult result = registrar.registerStudentForCourse("S001", "CS101");

        // Verify
        assertTrue(result.isSuccess());
        assertEquals(EnrollmentStatus.ENROLLED, result.getStatus());
        assertEquals(1, course.getEnrolled());
        assertEquals(3, student.getCurrentCredits());
    }

    @Test
    void testRegisterStudent_WithPrerequisites_Success() {
        // Setup student who completed prerequisites
        Student student = new Student("S001", "Jane Doe", "CS", 2);
        student.addCompletedCourse("CS101");
        studentRepository.save(student);

        // Course with prerequisite
        Course course = new Course("CS201", "Data Structures", 4, 25, 5);
        course.addPrerequisite("CS101");
        courseRepository.save(course);

        // Register
        RegistrationResult result = registrar.registerStudentForCourse("S001", "CS201");

        // Verify
        assertTrue(result.isSuccess());
        assertEquals(EnrollmentStatus.ENROLLED, result.getStatus());
    }

    // ========== Prerequisite Failure Tests ==========

    @Test
    void testRegisterStudent_MissingPrerequisite_Failure() {
        // Setup student without prerequisites
        Student student = new Student("S001", "John Doe", "CS", 2);
        studentRepository.save(student);

        // Course with prerequisite
        Course course = new Course("CS201", "Data Structures", 4, 25, 5);
        course.addPrerequisite("CS101");
        courseRepository.save(course);

        // Register
        RegistrationResult result = registrar.registerStudentForCourse("S001", "CS201");

        // Verify
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Prerequisites not met"));
        assertEquals(0, course.getEnrolled()); // Not enrolled
    }

    @Test
    void testRegisterStudent_MissingCorequisite_Failure() {
        Student student = new Student("S001", "John Doe", "CS", 2);
        studentRepository.save(student);

        // Course with corequisite
        Course course = new Course("CS250", "Lab", 1, 30, 5);
        course.addCorequisite("CS201");
        courseRepository.save(course);

        RegistrationResult result = registrar.registerStudentForCourse("S001", "CS250");

        assertFalse(result.isSuccess());
        assertEquals(0, course.getEnrolled());
    }

    // ========== Capacity and Wait list Tests ==========

    @Test
    void testRegisterStudent_CourseFull_AddedToWaitlist() {
        // Setup student
        Student student = new Student("S001", "John Doe", "CS", 1);
        studentRepository.save(student);

        // Course at capacity
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        course.setEnrolled(30); // Full
        courseRepository.save(course);

        // Register
        RegistrationResult result = registrar.registerStudentForCourse("S001", "CS101");

        // Verify
        assertTrue(result.isSuccess());
        assertEquals(EnrollmentStatus.WAITLISTED, result.getStatus());
        assertEquals(30, course.getEnrolled()); // Still at capacity
        assertEquals(1, waitlistManager.getWaitlistSize("CS101"));
    }

    @Test
    void testRegisterStudent_BothFullWaitlistFull_Failure() {
        // Setup student
        Student student = new Student("S001", "John Doe", "CS", 1);
        studentRepository.save(student);

        // Course at capacity
        Course course = new Course("CS101", "Intro to CS", 3, 30, 2);
        course.setEnrolled(30); // Full
        courseRepository.save(course);

        // Fill waitlist
        Student s1 = new Student("S100", "Alice", "CS", 1);
        Student s2 = new Student("S101", "Bob", "CS", 1);
        studentRepository.save(s1);
        studentRepository.save(s2);
        waitlistManager.addToWaitlist("S100", course);
        waitlistManager.addToWaitlist("S101", course);

        // Try to register
        RegistrationResult result = registrar.registerStudentForCourse("S001", "CS101");

        // Verify
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("waitlist is at capacity"));
    }

    // ========== Drop Course Tests ==========

    @Test
    void testDropCourse_Enrolled_Success() {
        // Setup and register
        Student student = new Student("S001", "John Doe", "CS", 1);
        studentRepository.save(student);

        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        courseRepository.save(course);

        registrar.registerStudentForCourse("S001", "CS101");

        // Verify setup
        assertEquals(1, course.getEnrolled());
        assertEquals(3, student.getCurrentCredits());

        // Drop
        boolean dropped = registrar.dropCourse("S001", "CS101");

        // Verify
        assertTrue(dropped);
        assertEquals(0, course.getEnrolled()); // Seat released
        assertEquals(0, student.getCurrentCredits()); // Credits removed
    }

    @Test
    void testDropCourse_WithWaitlistPromotion_Success() {
        // Setup: two students, one course with 1 seat
        Student s1 = new Student("S001", "John Doe", "CS", 1);
        Student s2 = new Student("S002", "Jane Doe", "CS", 1);
        studentRepository.save(s1);
        studentRepository.save(s2);

        Course course = new Course("CS101", "Intro to CS", 3, 1, 10);
        courseRepository.save(course);

        // S001 registers (gets enrolled)
        registrar.registerStudentForCourse("S001", "CS101");
        assertEquals(1, course.getEnrolled());

        // S002 registers (gets waitlisted)
        RegistrationResult s2Result = registrar.registerStudentForCourse("S002", "CS101");
        assertEquals(EnrollmentStatus.WAITLISTED, s2Result.getStatus());
        assertEquals(1, waitlistManager.getWaitlistSize("CS101"));

        // S001 drops
        registrar.dropCourse("S001", "CS101");

        // Verify S002 was promoted
        assertEquals(1, course.getEnrolled()); // S002 now enrolled
        assertEquals(0, waitlistManager.getWaitlistSize("CS101")); // Waitlist empty
        assertEquals(3, s2.getCurrentCredits()); // S002 has credits
    }

    @Test
    void testDropCourse_Waitlisted_RemovedFromWaitlist() {
        // Setup
        Student student = new Student("S001", "John Doe", "CS", 1);
        studentRepository.save(student);

        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        course.setEnrolled(30); // Full
        courseRepository.save(course);

        // Register (waitlisted)
        registrar.registerStudentForCourse("S001", "CS101");
        assertEquals(1, waitlistManager.getWaitlistSize("CS101"));

        // Drop
        boolean dropped = registrar.dropCourse("S001", "CS101");

        // Verify
        assertTrue(dropped);
        assertEquals(0, waitlistManager.getWaitlistSize("CS101")); // Removed from waitlist
        assertEquals(30, course.getEnrolled()); // Course count unchanged
    }

    @Test
    void testDropCourse_NotEnrolled_Failure() {
        Student student = new Student("S001", "John Doe", "CS", 1);
        studentRepository.save(student);

        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        courseRepository.save(course);

        // Try to drop without being enrolled
        boolean dropped = registrar.dropCourse("S001", "CS101");

        assertFalse(dropped);
    }

    // ========== Duplicate Enrollment Tests ==========

    @Test
    void testRegisterStudent_AlreadyEnrolled_Failure() {
        Student student = new Student("S001", "John Doe", "CS", 1);
        studentRepository.save(student);

        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        courseRepository.save(course);

        // First registration
        registrar.registerStudentForCourse("S001", "CS101");

        // Try to register again
        RegistrationResult result = registrar.registerStudentForCourse("S001", "CS101");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("already enrolled"));
        assertEquals(1, course.getEnrolled()); // Still only 1
    }

    // ========== Validation Tests ==========

    @Test
    void testRegisterStudent_StudentNotFound_Failure() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        courseRepository.save(course);

        RegistrationResult result = registrar.registerStudentForCourse("S999", "CS101");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Student not found"));
    }

    @Test
    void testRegisterStudent_CourseNotFound_Failure() {
        Student student = new Student("S001", "John Doe", "CS", 1);
        studentRepository.save(student);

        RegistrationResult result = registrar.registerStudentForCourse("S001", "CS999");

        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("Course not found"));
    }

    // ========== Complex Integration Scenarios ==========

    @Test
    void testComplexScenario_MultipleStudentsMultipleConstraints() {
        // Setup 3 students with different backgrounds
        Student s1 = new Student("S001", "Alice", "CS", 3);
        s1.addCompletedCourse("CS101");
        s1.addCompletedCourse("CS201");
        studentRepository.save(s1);

        Student s2 = new Student("S002", "Bob", "CS", 2);
        s2.addCompletedCourse("CS101"); // Missing CS201
        studentRepository.save(s2);

        Student s3 = new Student("S003", "Charlie", "CS", 3);
        s3.addCompletedCourse("CS101");
        s3.addCompletedCourse("CS201");
        studentRepository.save(s3);

        // Course with prerequisites and limited capacity
        Course course = new Course("CS301", "Algorithms", 4, 2, 5);
        course.addPrerequisite("CS101");
        course.addPrerequisite("CS201");
        courseRepository.save(course);

        // Alice registers - should succeed
        RegistrationResult r1 = registrar.registerStudentForCourse("S001", "CS301");
        assertTrue(r1.isSuccess());
        assertEquals(EnrollmentStatus.ENROLLED, r1.getStatus());

        // Bob registers - should fail (missing prerequisite)
        RegistrationResult r2 = registrar.registerStudentForCourse("S002", "CS301");
        assertFalse(r2.isSuccess());

        // Charlie registers - should succeed
        RegistrationResult r3 = registrar.registerStudentForCourse("S003", "CS301");
        assertTrue(r3.isSuccess());
        assertEquals(EnrollmentStatus.ENROLLED, r3.getStatus());

        // Verify final state
        assertEquals(2, course.getEnrolled());
        assertEquals(4, s1.getCurrentCredits());
        assertEquals(0, s2.getCurrentCredits()); // Not enrolled
        assertEquals(4, s3.getCurrentCredits());
    }

    @Test
    void testInvariant_EnrolledNeverExceedsCapacity() {
        Student student = new Student("S001", "John Doe", "CS", 1);
        studentRepository.save(student);

        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        courseRepository.save(course);

        // Register student
        registrar.registerStudentForCourse("S001", "CS101");

        // Invariant check
        assertTrue(course.getEnrolled() <= course.getCapacity());
        assertTrue(seatAllocator.isValidAllocation(course));
    }

    @Test
    void testInvariant_WaitlistNeverExceedsCapacity() {
        Course course = new Course("CS101", "Intro to CS", 3, 1, 3);
        course.setEnrolled(1); // At capacity
        courseRepository.save(course);

        for (int i = 1; i <= 5; i++) {
            Student student = new Student("S00" + i, "Student " + i, "CS", 1);
            studentRepository.save(student);

            registrar.registerStudentForCourse("S00" + i, "CS101");
        }

        // Check invariant
        assertTrue(waitlistManager.getWaitlistSize("CS101") <= course.getWaitlistCapacity());
    }

    // ========== Utility Method Tests ==========

    @Test
    void testCheckEligibility_Eligible_Valid() {
        Student student = new Student("S001", "John Doe", "CS", 2);
        student.addCompletedCourse("CS101");
        studentRepository.save(student);

        Course course = new Course("CS201", "Data Structures", 4, 25, 5);
        course.addPrerequisite("CS101");
        courseRepository.save(course);

        var result = registrar.checkEligibility("S001", "CS201");

        assertTrue(result.isValid());
    }

    @Test
    void testCheckEligibility_NotEligible_Invalid() {
        Student student = new Student("S001", "John Doe", "CS", 2);
        studentRepository.save(student);

        Course course = new Course("CS201", "Data Structures", 4, 25, 5);
        course.addPrerequisite("CS101");
        courseRepository.save(course);

        var result = registrar.checkEligibility("S001", "CS201");

        assertFalse(result.isValid());
    }

    @Test
    void testHasAvailableSeats_True() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        course.setEnrolled(15);
        courseRepository.save(course);

        assertTrue(registrar.hasAvailableSeats("CS101"));
    }

    @Test
    void testHasAvailableSeats_False() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        course.setEnrolled(30);
        courseRepository.save(course);

        assertFalse(registrar.hasAvailableSeats("CS101"));
    }

    @Test
    void testGetWaitlistPosition() {
        Student s1 = new Student("S001", "Alice", "CS", 1);
        Student s2 = new Student("S002", "Bob", "CS", 1);
        studentRepository.save(s1);
        studentRepository.save(s2);

        Course course = new Course("CS101", "Intro to CS", 3, 1, 10);
        courseRepository.save(course);

        registrar.registerStudentForCourse("S001", "CS101"); // Enrolled
        registrar.registerStudentForCourse("S002", "CS101"); // Waitlisted

        assertEquals(1, registrar.getWaitlistPosition("S002", "CS101"));
    }
}
