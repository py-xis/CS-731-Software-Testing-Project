package edu.courseregistration.engine;

import edu.courseregistration.model.Course;
import edu.courseregistration.model.Enrollment;
import edu.courseregistration.model.EnrollmentStatus;
import edu.courseregistration.model.Student;
import edu.courseregistration.repository.CourseRepository;
import edu.courseregistration.repository.StudentRepository;
import edu.courseregistration.result.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PrerequisiteEngine.
 * Tests all validation logic including boundary conditions.
 */
class PrerequisiteEngineTest {
    private PrerequisiteEngine prerequisiteEngine;
    private CourseRepository courseRepository;
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        courseRepository = new CourseRepository();
        studentRepository = new StudentRepository();
        prerequisiteEngine = new PrerequisiteEngine(courseRepository, studentRepository);
    }

    // ========== checkPrerequisites Tests ==========

    @Test
    void testCheckPrerequisites_NoPrerequsites_Success() {
        // Course with no prerequisites
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        Student student = new Student("S001", "John Doe", "CS", 1);

        ValidationResult result = prerequisiteEngine.checkPrerequisites(student, course);

        assertTrue(result.isValid());
        assertEquals("No prerequisites required", result.getMessage());
    }

    @Test
    void testCheckPrerequisites_EmptyPrerequisiteList_Success() {
        // Course with empty prerequisite list
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        course.setPrerequisites(new ArrayList<>());
        Student student = new Student("S001", "John Doe", "CS", 1);

        ValidationResult result = prerequisiteEngine.checkPrerequisites(student, course);

        assertTrue(result.isValid());
    }

    @Test
    void testCheckPrerequisites_SinglePrereqSatisfied_Success() {
        // Course with one prerequisite
        Course course = new Course("CS201", "Data Structures", 4, 25, 5);
        course.addPrerequisite("CS101");

        // Student who completed the prerequisite
        Student student = new Student("S001", "John Doe", "CS", 2);
        student.addCompletedCourse("CS101");

        ValidationResult result = prerequisiteEngine.checkPrerequisites(student, course);

        assertTrue(result.isValid());
        assertEquals("All prerequisites satisfied", result.getMessage());
    }

    @Test
    void testCheckPrerequisites_SinglePrereqMissing_Failure() {
        // Course with one prerequisite
        Course course = new Course("CS201", "Data Structures", 4, 25, 5);
        course.addPrerequisite("CS101");

        // Student who hasn't completed the prerequisite
        Student student = new Student("S001", "John Doe", "CS", 2);

        ValidationResult result = prerequisiteEngine.checkPrerequisites(student, course);

        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("CS101"));
    }

    @Test
    void testCheckPrerequisites_MultiplePrereqsAllSatisfied_Success() {
        // Course with multiple prerequisites
        Course course = new Course("CS301", "Algorithms", 4, 20, 5);
        course.addPrerequisite("CS101");
        course.addPrerequisite("CS201");
        course.addPrerequisite("MATH200");

        // Student who completed all prerequisites
        Student student = new Student("S001", "John Doe", "CS", 3);
        student.addCompletedCourse("CS101");
        student.addCompletedCourse("CS201");
        student.addCompletedCourse("MATH200");

        ValidationResult result = prerequisiteEngine.checkPrerequisites(student, course);

        assertTrue(result.isValid());
    }

    @Test
    void testCheckPrerequisites_MultiplePrereqsOneMissing_Failure() {
        // Course with multiple prerequisites
        Course course = new Course("CS301", "Algorithms", 4, 20, 5);
        course.addPrerequisite("CS101");
        course.addPrerequisite("CS201");
        course.addPrerequisite("MATH200");

        // Student missing one prerequisite
        Student student = new Student("S001", "John Doe", "CS", 3);
        student.addCompletedCourse("CS101");
        student.addCompletedCourse("CS201");
        // Missing MATH200

        ValidationResult result = prerequisiteEngine.checkPrerequisites(student, course);

        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("MATH200"));
    }

    // ========== checkCorequisites Tests ==========

    @Test
    void testCheckCorequisites_NoCorequisites_Success() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        Student student = new Student("S001", "John Doe", "CS", 1);
        List<Enrollment> currentEnrollments = new ArrayList<>();

        ValidationResult result = prerequisiteEngine.checkCorequisites(student, course, currentEnrollments);

        assertTrue(result.isValid());
        assertEquals("No corequisites required", result.getMessage());
    }

    @Test
    void testCheckCorequisites_CoreqAlreadyCompleted_Success() {
        // Course with corequisite
        Course course = new Course("CS250", "Lab", 1, 30, 5);
        course.addCorequisite("CS201");

        // Student who already completed the corequisite
        Student student = new Student("S001", "John Doe", "CS", 3);
        student.addCompletedCourse("CS201");

        List<Enrollment> currentEnrollments = new ArrayList<>();

        ValidationResult result = prerequisiteEngine.checkCorequisites(student, course, currentEnrollments);

        assertTrue(result.isValid());
    }

    @Test
    void testCheckCorequisites_CoreqCurrentlyEnrolled_Success() {
        // Course with corequisite
        Course course = new Course("CS250", "Lab", 1, 30, 5);
        course.addCorequisite("CS201");

        // Student currently enrolled in corequisite
        Student student = new Student("S001", "John Doe", "CS", 2);

        Enrollment enrollment = new Enrollment("E001", "S001", "CS201", EnrollmentStatus.ENROLLED);
        List<Enrollment> currentEnrollments = Arrays.asList(enrollment);

        ValidationResult result = prerequisiteEngine.checkCorequisites(student, course, currentEnrollments);

        assertTrue(result.isValid());
    }

    @Test
    void testCheckCorequisites_CoreqNotMet_Failure() {
        // Course with corequisite
        Course course = new Course("CS250", "Lab", 1, 30, 5);
        course.addCorequisite("CS201");

        // Student not enrolled and hasn't completed
        Student student = new Student("S001", "John Doe", "CS", 2);
        List<Enrollment> currentEnrollments = new ArrayList<>();

        ValidationResult result = prerequisiteEngine.checkCorequisites(student, course, currentEnrollments);

        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("CS201"));
    }

    @Test
    void testCheckCorequisites_CoreqWaitlisted_Failure() {
        // Course with corequisite
        Course course = new Course("CS250", "Lab", 1, 30, 5);
        course.addCorequisite("CS201");

        // Student waitlisted (not enrolled) in corequisite
        Student student = new Student("S001", "John Doe", "CS", 2);

        Enrollment enrollment = new Enrollment("E001", "S001", "CS201", EnrollmentStatus.WAITLISTED);
        List<Enrollment> currentEnrollments = Arrays.asList(enrollment);

        ValidationResult result = prerequisiteEngine.checkCorequisites(student, course, currentEnrollments);

        assertFalse(result.isValid());
    }

    // ========== validateAllRequirements Tests ==========

    @Test
    void testValidateAllRequirements_BothSatisfied_Success() {
        Course course = new Course("CS301", "Algorithms", 4, 20, 5);
        course.addPrerequisite("CS201");
        course.addCorequisite("MATH300");

        Student student = new Student("S001", "John Doe", "CS", 3);
        student.addCompletedCourse("CS201");

        Enrollment mathEnrollment = new Enrollment("E001", "S001", "MATH300", EnrollmentStatus.ENROLLED);
        List<Enrollment> currentEnrollments = Arrays.asList(mathEnrollment);

        ValidationResult result = prerequisiteEngine.validateAllRequirements(student, course, currentEnrollments);

        assertTrue(result.isValid());
    }

    @Test
    void testValidateAllRequirements_PrereqFails_Failure() {
        Course course = new Course("CS301", "Algorithms", 4, 20, 5);
        course.addPrerequisite("CS201");

        Student student = new Student("S001", "John Doe", "CS", 3);
        // Missing prerequisite

        List<Enrollment> currentEnrollments = new ArrayList<>();

        ValidationResult result = prerequisiteEngine.validateAllRequirements(student, course, currentEnrollments);

        assertFalse(result.isValid());
    }

    @Test
    void testValidateAllRequirements_CoreqFails_Failure() {
        Course course = new Course("CS301", "Algorithms", 4, 20, 5);
        course.addPrerequisite("CS201");
        course.addCorequisite("MATH300");

        Student student = new Student("S001", "John Doe", "CS", 3);
        student.addCompletedCourse("CS201"); // Prerequisite OK

        List<Enrollment> currentEnrollments = new ArrayList<>(); // Missing corequisite

        ValidationResult result = prerequisiteEngine.validateAllRequirements(student, course, currentEnrollments);

        assertFalse(result.isValid());
        assertTrue(result.getMessage().contains("MATH300"));
    }

    // ========== checkSemesterRequirement Tests (Boundary Conditions) ==========

    @Test
    void testCheckSemesterRequirement_ExactlyAtMinimum_Success() {
        Student student = new Student("S001", "John Doe", "CS", 3);
        Course course = new Course("CS301", "Advanced Course", 4, 20, 5);
        int minSemester = 3;

        boolean result = prerequisiteEngine.checkSemesterRequirement(student, course, minSemester);

        assertTrue(result); // >= allows exact match
    }

    @Test
    void testCheckSemesterRequirement_AboveMinimum_Success() {
        Student student = new Student("S001", "John Doe", "CS", 4);
        Course course = new Course("CS301", "Advanced Course", 4, 20, 5);
        int minSemester = 3;

        boolean result = prerequisiteEngine.checkSemesterRequirement(student, course, minSemester);

        assertTrue(result);
    }

    @Test
    void testCheckSemesterRequirement_BelowMinimum_Failure() {
        Student student = new Student("S001", "John Doe", "CS", 2);
        Course course = new Course("CS301", "Advanced Course", 4, 20, 5);
        int minSemester = 3;

        boolean result = prerequisiteEngine.checkSemesterRequirement(student, course, minSemester);

        assertFalse(result);
    }

    @Test
    void testCheckSemesterRequirement_Boundary_OneBelow_Failure() {
        Student student = new Student("S001", "John Doe", "CS", 2);
        Course course = new Course("CS301", "Advanced Course", 4, 20, 5);
        int minSemester = 3;

        boolean result = prerequisiteEngine.checkSemesterRequirement(student, course, minSemester);

        assertFalse(result); // Boundary: 2 < 3
    }

    // ========== checkCreditLimit Tests ==========

    @Test
    void testCheckCreditLimit_UnderLimit_Success() {
        Student student = new Student("S001", "John Doe", "CS", 3);
        student.setCurrentCredits(15);

        ValidationResult result = prerequisiteEngine.checkCreditLimit(student, 3, 20);

        assertTrue(result.isValid()); // 15 + 3 = 18 <= 20
    }

    @Test
    void testCheckCreditLimit_ExactlyAtLimit_Success() {
        Student student = new Student("S001", "John Doe", "CS", 3);
        student.setCurrentCredits(17);

        ValidationResult result = prerequisiteEngine.checkCreditLimit(student, 3, 20);

        assertTrue(result.isValid()); // 17 + 3 = 20 <= 20
    }

    @Test
    void testCheckCreditLimit_OverLimit_Failure() {
        Student student = new Student("S001", "John Doe", "CS", 3);
        student.setCurrentCredits(18);

        ValidationResult result = prerequisiteEngine.checkCreditLimit(student, 3, 20);

        assertFalse(result.isValid()); // 18 + 3 = 21 > 20
    }

    @Test
    void testCheckCreditLimit_Boundary_OneOverLimit_Failure() {
        Student student = new Student("S001", "John Doe", "CS", 3);
        student.setCurrentCredits(17);

        ValidationResult result = prerequisiteEngine.checkCreditLimit(student, 4, 20);

        assertFalse(result.isValid()); // 17 + 4 = 21 > 20
    }

    // ========== checkAdvancedCourseEligibility Tests (Complex Boolean Logic)
    // ==========

    @Test
    void testCheckAdvancedEligibility_MeetsBasicAndHasPrereqs_Success() {
        Course course = new Course("CS401", "Advanced Topics", 4, 15, 3);
        course.addPrerequisite("CS301");

        Student student = new Student("S001", "John Doe", "CS", 5);
        student.addCompletedCourse("CS301");

        boolean result = prerequisiteEngine.checkAdvancedCourseEligibility(student, course, 3.5, 5);

        assertTrue(result); // semester >= 5 AND has prereqs
    }

    @Test
    void testCheckAdvancedEligibility_HighSemesterNoPrereqs_Success() {
        Course course = new Course("CS401", "Advanced Topics", 4, 15, 3);
        course.addPrerequisite("CS301");

        Student student = new Student("S001", "John Doe", "CS", 8);
        // No prerequisites completed but high semester (8 > 5+2)

        boolean result = prerequisiteEngine.checkAdvancedCourseEligibility(student, course, 3.5, 5);

        assertTrue(result); // High semester compensates
    }

    @Test
    void testCheckAdvancedEligibility_BelowMinSemester_Failure() {
        Course course = new Course("CS401", "Advanced Topics", 4, 15, 3);

        Student student = new Student("S001", "John Doe", "CS", 3);

        boolean result = prerequisiteEngine.checkAdvancedCourseEligibility(student, course, 3.5, 5);

        assertFalse(result); // semester < 5
    }

    @Test
    void testCheckAdvancedEligibility_ExactMinSemesterNoPrereqs_Failure() {
        Course course = new Course("CS401", "Advanced Topics", 4, 15, 3);
        course.addPrerequisite("CS301");

        Student student = new Student("S001", "John Doe", "CS", 5);
        // semester == 5, but no prereqs and not high semester

        boolean result = prerequisiteEngine.checkAdvancedCourseEligibility(student, course, 3.5, 5);

        assertFalse(result); // semester not > 7, no prereqs
    }

    @Test
    void testCheckAdvancedEligibility_BoundaryHighSemester_Success() {
        Course course = new Course("CS401", "Advanced Topics", 4, 15, 3);
        course.addPrerequisite("CS301");

        Student student = new Student("S001", "John Doe", "CS", 8);
        // semester 8 > (5 + 2) = 7

        boolean result = prerequisiteEngine.checkAdvancedCourseEligibility(student, course, 3.5, 5);

        assertTrue(result);
    }
}
