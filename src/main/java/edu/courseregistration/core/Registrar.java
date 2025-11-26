package edu.courseregistration.core;

import edu.courseregistration.engine.PrerequisiteEngine;
import edu.courseregistration.engine.SeatAllocator;
import edu.courseregistration.engine.WaitlistManager;
import edu.courseregistration.model.Course;
import edu.courseregistration.model.Enrollment;
import edu.courseregistration.model.EnrollmentStatus;
import edu.courseregistration.model.Student;
import edu.courseregistration.repository.CourseRepository;
import edu.courseregistration.repository.EnrollmentRepository;
import edu.courseregistration.repository.StudentRepository;
import edu.courseregistration.result.AllocationResult;
import edu.courseregistration.result.RegistrationResult;
import edu.courseregistration.result.ValidationResult;

import java.util.List;
import java.util.Optional;

/**
 * Registrar is the main integration layer that orchestrates all components.
 * This class is the primary target for integration-level mutation testing.
 */
public class Registrar {
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PrerequisiteEngine prerequisiteEngine;
    private final SeatAllocator seatAllocator;
    private final WaitlistManager waitlistManager;

    /**
     * Constructor with all dependencies.
     */
    public Registrar(StudentRepository studentRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            PrerequisiteEngine prerequisiteEngine,
            SeatAllocator seatAllocator,
            WaitlistManager waitlistManager) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.prerequisiteEngine = prerequisiteEngine;
        this.seatAllocator = seatAllocator;
        this.waitlistManager = waitlistManager;
    }

    /**
     * Main registration method - orchestrates all components.
     * Integration mutation target: method calls, parameter passing, return
     * handling.
     *
     * @param studentId the student ID
     * @param courseId  the course ID
     * @return RegistrationResult with enrollment status
     */
    public RegistrationResult registerStudentForCourse(String studentId, String courseId) {
        // Step 1: Validate student exists
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (!studentOpt.isPresent()) {
            return RegistrationResult.failure("Student not found: " + studentId);
        }
        Student student = studentOpt.get();

        // Step 2: Validate course exists
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (!courseOpt.isPresent()) {
            return RegistrationResult.failure("Course not found: " + courseId);
        }
        Course course = courseOpt.get();

        // Step 3: Check for duplicate enrollment
        if (enrollmentRepository.isStudentEnrolled(studentId, courseId)) {
            return RegistrationResult.failure("Student already enrolled in this course");
        }

        // Step 4: Check prerequisites - integration call
        List<Enrollment> currentEnrollments = enrollmentRepository.findActiveEnrollmentsByStudentId(studentId);
        ValidationResult prereqResult = prerequisiteEngine.validateAllRequirements(student, course, currentEnrollments);

        if (!prereqResult.isValid()) {
            return RegistrationResult.failure("Prerequisites not met: " + prereqResult.getMessage());
        }

        // Step 5: Attempt seat allocation - integration call
        AllocationResult allocationResult = seatAllocator.allocateSeat(course);

        // Step 6: Create enrollment based on allocation result
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setCourseId(courseId);

        if (allocationResult.isAllocated()) {
            // Successfully allocated seat
            enrollment.setStatus(EnrollmentStatus.ENROLLED);
            enrollmentRepository.save(enrollment);
            courseRepository.save(course); // Save updated enrollment count

            // Update student credits
            student.addCredits(course.getCredits());
            studentRepository.save(student);

            return RegistrationResult.success(enrollment.getEnrollmentId(), EnrollmentStatus.ENROLLED);
        } else if (allocationResult.isWaitlisted()) {
            // Add to waitlist - integration call
            boolean addedToWaitlist = waitlistManager.addToWaitlist(studentId, course);

            if (addedToWaitlist) {
                enrollment.setStatus(EnrollmentStatus.WAITLISTED);
                enrollmentRepository.save(enrollment);
                return RegistrationResult.success(enrollment.getEnrollmentId(), EnrollmentStatus.WAITLISTED);
            } else {
                return RegistrationResult.failure("Course full and waitlist is at capacity");
            }
        }

        return RegistrationResult.failure("Unable to register for course");
    }

    /**
     * Drop course operation.
     * Integration mutation target: reverse operations and waitlist promotion.
     *
     * @param studentId the student ID
     * @param courseId  the course ID
     * @return true if dropped successfully
     */
    public boolean dropCourse(String studentId, String courseId) {
        // Find enrollment
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByStudentAndCourse(studentId, courseId);

        if (!enrollmentOpt.isPresent()) {
            return false;
        }

        Enrollment enrollment = enrollmentOpt.get();

        // Can only drop if enrolled or waitlisted
        if (enrollment.isDropped()) {
            return false;
        }

        // Get course and student
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (!courseOpt.isPresent() || !studentOpt.isPresent()) {
            return false;
        }

        Course course = courseOpt.get();
        Student student = studentOpt.get();

        // If student was enrolled (not waitlisted), release seat
        if (enrollment.isEnrolled()) {
            // Release seat - integration call
            seatAllocator.releaseSeat(course);
            courseRepository.save(course);

            // Update student credits
            student.removeCredits(course.getCredits());
            studentRepository.save(student);

            // Try to promote from waitlist - integration call
            Optional<String> promotedStudentOpt = waitlistManager.promoteFromWaitlist(course);

            if (promotedStudentOpt.isPresent()) {
                // Promote waitlisted student
                String promotedStudentId = promotedStudentOpt.get();
                promoteStudentFromWaitlist(promotedStudentId, courseId);
            }
        } else if (enrollment.isWaitlisted()) {
            // Remove from waitlist - integration call
            waitlistManager.removeFromWaitlist(studentId, courseId);
        }

        // Mark enrollment as dropped
        enrollment.drop();
        enrollmentRepository.save(enrollment);

        return true;
    }

    /**
     * Promotes a student from waitlist when a seat becomes available.
     * Integration helper method.
     *
     * @param studentId the student ID
     * @param courseId  the course ID
     * @return true if promoted successfully
     */
    private boolean promoteStudentFromWaitlist(String studentId, String courseId) {
        // Find waitlisted enrollment
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findByStudentAndCourse(studentId, courseId);

        if (!enrollmentOpt.isPresent()) {
            return false;
        }

        Enrollment enrollment = enrollmentOpt.get();

        if (!enrollment.isWaitlisted()) {
            return false;
        }

        // Get course and student
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        Optional<Student> studentOpt = studentRepository.findById(studentId);

        if (!courseOpt.isPresent() || !studentOpt.isPresent()) {
            return false;
        }

        Course course = courseOpt.get();
        Student student = studentOpt.get();

        // Allocate seat - integration call
        AllocationResult result = seatAllocator.allocateSeat(course);

        if (result.isAllocated()) {
            // Update enrollment status
            enrollment.enroll();
            enrollmentRepository.save(enrollment);

            // Update course
            courseRepository.save(course);

            // Update student credits
            student.addCredits(course.getCredits());
            studentRepository.save(student);

            return true;
        }

        return false;
    }

    /**
     * Gets waitlist position for a student.
     *
     * @param studentId the student ID
     * @param courseId  the course ID
     * @return waitlist position or -1 if not waitlisted
     */
    public int getWaitlistPosition(String studentId, String courseId) {
        return waitlistManager.getWaitlistPosition(studentId, courseId);
    }

    /**
     * Checks if a student can register for a course.
     *
     * @param studentId the student ID
     * @param courseId  the course ID
     * @return ValidationResult with eligibility status
     */
    public ValidationResult checkEligibility(String studentId, String courseId) {
        // Validate student exists
        Optional<Student> studentOpt = studentRepository.findById(studentId);
        if (!studentOpt.isPresent()) {
            return ValidationResult.failure("Student not found");
        }

        // Validate course exists
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (!courseOpt.isPresent()) {
            return ValidationResult.failure("Course not found");
        }

        Student student = studentOpt.get();
        Course course = courseOpt.get();

        // Check for duplicate enrollment
        if (enrollmentRepository.isStudentEnrolled(studentId, courseId)) {
            return ValidationResult.failure("Already enrolled");
        }

        // Check prerequisites
        List<Enrollment> currentEnrollments = enrollmentRepository.findActiveEnrollmentsByStudentId(studentId);
        return prerequisiteEngine.validateAllRequirements(student, course, currentEnrollments);
    }

    /**
     * Gets all enrollments for a student.
     *
     * @param studentId the student ID
     * @return list of enrollments
     */
    public List<Enrollment> getStudentEnrollments(String studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    /**
     * Gets all enrollments for a course.
     *
     * @param courseId the course ID
     * @return list of enrollments
     */
    public List<Enrollment> getCourseEnrollments(String courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    /**
     * Checks if a course has available seats.
     *
     * @param courseId the course ID
     * @return true if seats available
     */
    public boolean hasAvailableSeats(String courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (!courseOpt.isPresent()) {
            return false;
        }
        return seatAllocator.hasAvailableSeats(courseOpt.get());
    }
}
