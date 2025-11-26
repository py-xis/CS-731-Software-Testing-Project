package edu.courseregistration.repository;

import edu.courseregistration.engine.WaitlistManager;
import edu.courseregistration.model.Course;
import edu.courseregistration.model.Enrollment;
import edu.courseregistration.model.Student;

import java.util.*;

/**
 * Repository for managing Student entities.
 * In-memory implementation for simplicity.
 */
public class StudentRepository {
    private final Map<String, Student> students;
    private EnrollmentRepository enrollmentRepository;
    private WaitlistManager waitlistManager;
    private CourseRepository courseRepository;

    public StudentRepository() {
        this.students = new HashMap<>();
    }

    /**
     * Sets the enrollment repository for cascade operations.
     * This is set after construction to avoid circular dependencies.
     *
     * @param enrollmentRepository the enrollment repository
     */
    public void setEnrollmentRepository(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    /**
     * Sets the waitlist manager for cascade operations.
     * This is set after construction to avoid circular dependencies.
     *
     * @param waitlistManager the waitlist manager
     */
    public void setWaitlistManager(WaitlistManager waitlistManager) {
        this.waitlistManager = waitlistManager;
    }

    /**
     * Sets the course repository for cascade operations.
     * This is set after construction to avoid circular dependencies.
     *
     * @param courseRepository the course repository
     */
    public void setCourseRepository(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /**
     * Saves a student to the repository.
     *
     * @param student the student to save
     * @return the saved student
     */
    public Student save(Student student) {
        students.put(student.getStudentId(), student);
        return student;
    }

    /**
     * Finds a student by ID.
     *
     * @param studentId the student ID
     * @return Optional containing the student if found
     */
    public Optional<Student> findById(String studentId) {
        return Optional.ofNullable(students.get(studentId));
    }

    /**
     * Finds all students.
     *
     * @return list of all students
     */
    public List<Student> findAll() {
        return new ArrayList<>(students.values());
    }

    /**
     * Checks if a student exists.
     *
     * @param studentId the student ID
     * @return true if student exists
     */
    public boolean exists(String studentId) {
        return students.containsKey(studentId);
    }

    /**
     * Deletes a student and cascades deletion to all associated data.
     * Removes the student from:
     * - All enrollments (and updates course enrollment counts)
     * - All waitlists
     *
     * @param studentId the student ID
     * @return true if deleted successfully
     */
    public boolean delete(String studentId) {
        // Check if student exists
        if (!students.containsKey(studentId)) {
            return false;
        }

        // Cascade delete enrollments if repository is set
        if (enrollmentRepository != null && courseRepository != null) {
            List<Enrollment> studentEnrollments = enrollmentRepository.findByStudentId(studentId);

            for (Enrollment enrollment : studentEnrollments) {
                // If student was enrolled (not just waitlisted), decrement course count
                if (enrollment.isEnrolled()) {
                    Optional<Course> courseOpt = courseRepository.findById(enrollment.getCourseId());
                    if (courseOpt.isPresent()) {
                        Course course = courseOpt.get();
                        course.decrementEnrolled();
                        courseRepository.save(course);
                    }
                }

                // Delete the enrollment record
                enrollmentRepository.delete(enrollment.getEnrollmentId());
            }
        }

        // Cascade delete from waitlists if manager is set
        if (waitlistManager != null && courseRepository != null) {
            // Get all courses to check waitlists
            List<Course> allCourses = courseRepository.findAll();
            for (Course course : allCourses) {
                // Remove student from waitlist if present
                waitlistManager.removeFromWaitlist(studentId, course.getCourseId());
            }
        }

        // Finally, remove the student
        return students.remove(studentId) != null;
    }

    /**
     * Gets the total count of students.
     *
     * @return number of students
     */
    public int count() {
        return students.size();
    }

    /**
     * Clears all students (for testing).
     */
    public void clear() {
        students.clear();
    }
}
