package edu.courseregistration.engine;

import edu.courseregistration.model.Course;
import edu.courseregistration.model.Enrollment;
import edu.courseregistration.model.Student;
import edu.courseregistration.repository.CourseRepository;
import edu.courseregistration.repository.StudentRepository;
import edu.courseregistration.result.ValidationResult;

import java.util.List;
import java.util.Set;

/**
 * Engine for validating course prerequisites and corequisites.
 * Contains complex boolean logic ideal for mutation testing.
 */
public class PrerequisiteEngine {
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public PrerequisiteEngine(CourseRepository courseRepository, StudentRepository studentRepository) {
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    /**
     * Checks if a student has completed all prerequisites for a course.
     * Complex boolean logic with multiple conditions.
     *
     * @param student the student attempting to enroll
     * @param course  the course to check prerequisites for
     * @return ValidationResult indicating whether prerequisites are satisfied
     */
    public ValidationResult checkPrerequisites(Student student, Course course) {
        // If course has no prerequisites, validation passes
        if (course.getPrerequisites() == null || course.getPrerequisites().isEmpty()) {
            return ValidationResult.success("No prerequisites required");
        }

        Set<String> completedCourses = student.getCompletedCourses();
        List<String> requiredPrereqs = course.getPrerequisites();

        // Check each prerequisite
        for (String prereqId : requiredPrereqs) {
            if (!completedCourses.contains(prereqId)) {
                return ValidationResult.failure("Missing prerequisite: " + prereqId);
            }
        }

        return ValidationResult.success("All prerequisites satisfied");
    }

    /**
     * Checks if a student is currently enrolled in or has completed corequisites.
     * More complex logic with current enrollments consideration.
     *
     * @param student            the student attempting to enroll
     * @param course             the course to check corequisites for
     * @param currentEnrollments the student's current enrollments this semester
     * @return ValidationResult indicating whether corequisites are satisfied
     */
    public ValidationResult checkCorequisites(Student student, Course course,
            List<Enrollment> currentEnrollments) {
        // If course has no corequisites, validation passes
        if (course.getCorequisites() == null || course.getCorequisites().isEmpty()) {
            return ValidationResult.success("No corequisites required");
        }

        Set<String> completedCourses = student.getCompletedCourses();
        List<String> requiredCoreqs = course.getCorequisites();

        // Check each corequisite
        for (String coreqId : requiredCoreqs) {
            // Check if already completed
            if (completedCourses.contains(coreqId)) {
                continue;
            }

            // Check if currently enrolled
            boolean enrolledInCoreq = false;
            for (Enrollment enrollment : currentEnrollments) {
                if (enrollment.getCourseId().equals(coreqId) && enrollment.isEnrolled()) {
                    enrolledInCoreq = true;
                    break;
                }
            }

            if (!enrolledInCoreq) {
                return ValidationResult.failure("Missing corequisite: " + coreqId);
            }
        }

        return ValidationResult.success("All corequisites satisfied");
    }

    /**
     * Validates if a student meets all academic requirements for a course.
     * Combines multiple validation conditions.
     *
     * @param student            the student
     * @param course             the course
     * @param currentEnrollments the student's current enrollments
     * @return ValidationResult with overall validation status
     */
    public ValidationResult validateAllRequirements(Student student, Course course,
            List<Enrollment> currentEnrollments) {
        // Check prerequisites first
        ValidationResult prereqResult = checkPrerequisites(student, course);
        if (!prereqResult.isValid()) {
            return prereqResult;
        }

        // Check corequisites
        ValidationResult coreqResult = checkCorequisites(student, course, currentEnrollments);
        if (!coreqResult.isValid()) {
            return coreqResult;
        }

        return ValidationResult.success("All requirements satisfied");
    }

    /**
     * Checks if student has the minimum semester requirement for advanced courses.
     * Contains boundary conditions ideal for mutation testing.
     *
     * @param student     the student
     * @param course      the course
     * @param minSemester minimum semester required
     * @return true if student meets semester requirement
     */
    public boolean checkSemesterRequirement(Student student, Course course, int minSemester) {
        // Boundary condition: >= vs >
        return student.getSemester() >= minSemester;
    }

    /**
     * Validates credit limit for a student.
     * Contains arithmetic operations and comparisons.
     *
     * @param student           the student
     * @param additionalCredits credits to add
     * @param maxCredits        maximum allowed credits
     * @return ValidationResult indicating if credit limit is satisfied
     */
    public ValidationResult checkCreditLimit(Student student, int additionalCredits, int maxCredits) {
        int totalCredits = student.getCurrentCredits() + additionalCredits;

        // Boundary condition: <= vs <
        if (totalCredits <= maxCredits) {
            return ValidationResult.success("Credit limit satisfied");
        }

        return ValidationResult.failure("Credit limit exceeded: " + totalCredits + " > " + maxCredits);
    }

    /**
     * Complex validation with multiple AND/OR conditions.
     * Ideal for mutation operators that change boolean connectives.
     *
     * @param student     the student
     * @param course      the course
     * @param minGPA      minimum GPA required
     * @param minSemester minimum semester required
     * @return true if student meets complex requirements
     */
    public boolean checkAdvancedCourseEligibility(Student student, Course course,
            double minGPA, int minSemester) {
        // Complex boolean expression: (semester >= min) AND (GPA >= min OR has prereq)
        boolean meetsBasicRequirement = student.getSemester() >= minSemester;

        if (!meetsBasicRequirement) {
            return false;
        }

        // For advanced courses, either high semester count OR completed prerequisites
        boolean hasHighSemester = student.getSemester() > minSemester + 2;
        boolean hasAllPrereqs = checkPrerequisites(student, course).isValid();

        // Boolean logic mutation target: || vs &&
        return hasHighSemester || hasAllPrereqs;
    }
}
