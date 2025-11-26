package edu.courseregistration.repository;

import edu.courseregistration.engine.SeatAllocator;
import edu.courseregistration.engine.WaitlistManager;
import edu.courseregistration.model.Course;
import edu.courseregistration.model.Enrollment;
import edu.courseregistration.model.EnrollmentStatus;
import edu.courseregistration.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Optimized tests for StudentRepository focusing on cascade deletion logic.
 */
class StudentRepositoryTest {
    private StudentRepository studentRepository;
    private EnrollmentRepository enrollmentRepository;
    private CourseRepository courseRepository;
    private WaitlistManager waitlistManager;

    @BeforeEach
    void setUp() {
        studentRepository = new StudentRepository();
        enrollmentRepository = new EnrollmentRepository();
        courseRepository = new CourseRepository();
        SeatAllocator seatAllocator = new SeatAllocator(enrollmentRepository);
        waitlistManager = new WaitlistManager(seatAllocator);

        studentRepository.setEnrollmentRepository(enrollmentRepository);
        studentRepository.setCourseRepository(courseRepository);
        studentRepository.setWaitlistManager(waitlistManager);
    }

    @Test
    void testSave_NewStudent_Success() {
        Student student = new Student("S001", "John", "CS", 1);

        Student saved = studentRepository.save(student);

        assertNotNull(saved);
        assertEquals("S001", saved.getStudentId());
    }

    @Test
    void testFindById_Exists_ReturnsStudent() {
        Student student = new Student("S001", "John", "CS", 1);
        studentRepository.save(student);

        Optional<Student> found = studentRepository.findById("S001");

        assertTrue(found.isPresent());
    }

    @Test
    void testDelete_ExistingStudent_Success() {
        Student student = new Student("S001", "John", "CS", 1);
        studentRepository.save(student);

        boolean deleted = studentRepository.delete("S001");

        assertTrue(deleted);
    }

    // Critical cascade deletion tests
    @Test
    void testDelete_WithEnrolledCourse_DecrementsCount() {
        Student student = new Student("S001", "John", "CS", 1);
        Course course = new Course("CS101", "Intro", 3, 30, 10);
        course.setEnrolled(15);

        studentRepository.save(student);
        courseRepository.save(course);
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED));

        studentRepository.delete("S001");

        assertEquals(14, course.getEnrolled());
    }

    @Test
    void testDelete_WithWaitlistedEnrollment_NoCountChange() {
        Student student = new Student("S001", "John", "CS", 1);
        Course course = new Course("CS101", "Intro", 3, 30, 10);
        course.setEnrolled(15);

        studentRepository.save(student);
        courseRepository.save(course);
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.WAITLISTED));

        studentRepository.delete("S001");

        assertEquals(15, course.getEnrolled());
    }

    @Test
    void testDelete_WithDroppedEnrollment_NoCountChange() {
        Student student = new Student("S001", "John", "CS", 1);
        Course course = new Course("CS101", "Intro", 3, 30, 10);
        course.setEnrolled(15);

        studentRepository.save(student);
        courseRepository.save(course);
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.DROPPED));

        studentRepository.delete("S001");

        assertEquals(15, course.getEnrolled());
    }

    @Test
    void testDelete_WithMultipleEnrollments_HandlesAll() {
        Student student = new Student("S001", "John", "CS", 1);
        Course c1 = new Course("CS101", "C1", 3, 30, 10);
        Course c2 = new Course("CS201", "C2", 4, 40, 10);
        c1.setEnrolled(15);
        c2.setEnrolled(20);

        studentRepository.save(student);
        courseRepository.save(c1);
        courseRepository.save(c2);
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED));
        enrollmentRepository.save(new Enrollment("E002", "S001", "CS201", EnrollmentStatus.ENROLLED));

        studentRepository.delete("S001");

        assertEquals(14, c1.getEnrolled());
        assertEquals(19, c2.getEnrolled());
    }

    @Test
    void testDelete_RemovesEnrollmentRecords() {
        Student student = new Student("S001", "John", "CS", 1);
        Course course = new Course("CS101", "Intro", 3, 30, 10);

        studentRepository.save(student);
        courseRepository.save(course);
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED));

        studentRepository.delete("S001");

        assertTrue(enrollmentRepository.findByStudentId("S001").isEmpty());
    }

    @Test
    void testDelete_RemovesStudentFromWaitlists() {
        Student student = new Student("S001", "John", "CS", 1);
        Course course = new Course("CS101", "Intro", 3, 30, 10);
        course.setEnrolled(30);

        studentRepository.save(student);
        courseRepository.save(course);
        waitlistManager.addToWaitlist("S001", course);

        studentRepository.delete("S001");

        assertFalse(waitlistManager.isOnWaitlist("S001", "CS101"));
    }

    @Test
    void testDelete_CourseNotFound_HandlesGracefully() {
        Student student = new Student("S001", "John", "CS", 1);

        studentRepository.save(student);
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS999", EnrollmentStatus.ENROLLED));

        boolean deleted = studentRepository.delete("S001");

        assertTrue(deleted);
    }

    @Test
    void testDelete_NonExistentStudent_ReturnsFalse() {
        boolean deleted = studentRepository.delete("S999");

        assertFalse(deleted);
    }

    @Test
    void testFindAll_ReturnsAllStudents() {
        studentRepository.save(new Student("S001", "John", "CS", 1));
        studentRepository.save(new Student("S002", "Jane", "CS", 2));

        assertEquals(2, studentRepository.findAll().size());
    }
}
