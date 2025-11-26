package edu.courseregistration.repository;

import edu.courseregistration.model.Enrollment;
import edu.courseregistration.model.EnrollmentStatus;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Repository for managing Enrollment entities.
 * In-memory implementation for simplicity.
 */
public class EnrollmentRepository {
    private final Map<String, Enrollment> enrollments;
    private int enrollmentCounter;

    public EnrollmentRepository() {
        this.enrollments = new HashMap<>();
        this.enrollmentCounter = 1;
    }

    /**
     * Saves an enrollment to the repository.
     * Generates ID if not present.
     *
     * @param enrollment the enrollment to save
     * @return the saved enrollment
     */
    public Enrollment save(Enrollment enrollment) {
        if (enrollment.getEnrollmentId() == null || enrollment.getEnrollmentId().isEmpty()) {
            enrollment.setEnrollmentId(generateEnrollmentId());
        }
        enrollments.put(enrollment.getEnrollmentId(), enrollment);
        return enrollment;
    }

    /**
     * Finds an enrollment by ID.
     *
     * @param enrollmentId the enrollment ID
     * @return Optional containing the enrollment if found
     */
    public Optional<Enrollment> findById(String enrollmentId) {
        return Optional.ofNullable(enrollments.get(enrollmentId));
    }

    /**
     * Finds all enrollments for a student.
     *
     * @param studentId the student ID
     * @return list of enrollments
     */
    public List<Enrollment> findByStudentId(String studentId) {
        return enrollments.values().stream()
                .filter(e -> e.getStudentId().equals(studentId))
                .collect(Collectors.toList());
    }

    /**
     * Finds all enrollments for a course.
     *
     * @param courseId the course ID
     * @return list of enrollments
     */
    public List<Enrollment> findByCourseId(String courseId) {
        return enrollments.values().stream()
                .filter(e -> e.getCourseId().equals(courseId))
                .collect(Collectors.toList());
    }

    /**
     * Finds active enrollments for a student.
     *
     * @param studentId the student ID
     * @return list of active (ENROLLED) enrollments
     */
    public List<Enrollment> findActiveEnrollmentsByStudentId(String studentId) {
        return enrollments.values().stream()
                .filter(e -> e.getStudentId().equals(studentId))
                .filter(Enrollment::isEnrolled)
                .collect(Collectors.toList());
    }

    /**
     * Finds an enrollment by student and course.
     *
     * @param studentId the student ID
     * @param courseId  the course ID
     * @return Optional containing the enrollment if found
     */
    public Optional<Enrollment> findByStudentAndCourse(String studentId, String courseId) {
        return enrollments.values().stream()
                .filter(e -> e.getStudentId().equals(studentId) && e.getCourseId().equals(courseId))
                .filter(e -> !e.isDropped()) // Exclude dropped enrollments
                .findFirst();
    }

    /**
     * Checks if a student is enrolled in a course.
     *
     * @param studentId the student ID
     * @param courseId  the course ID
     * @return true if enrolled
     */
    public boolean isStudentEnrolled(String studentId, String courseId) {
        return enrollments.values().stream()
                .anyMatch(e -> e.getStudentId().equals(studentId)
                        && e.getCourseId().equals(courseId)
                        && e.isEnrolled());
    }

    /**
     * Counts enrollments for a course with specific status.
     *
     * @param courseId the course ID
     * @param status   the enrollment status
     * @return count of enrollments
     */
    public long countByCourseAndStatus(String courseId, EnrollmentStatus status) {
        return enrollments.values().stream()
                .filter(e -> e.getCourseId().equals(courseId) && e.getStatus() == status)
                .count();
    }

    /**
     * Deletes an enrollment.
     *
     * @param enrollmentId the enrollment ID
     * @return true if deleted successfully
     */
    public boolean delete(String enrollmentId) {
        return enrollments.remove(enrollmentId) != null;
    }

    /**
     * Finds all enrollments.
     *
     * @return list of all enrollments
     */
    public List<Enrollment> findAll() {
        return new ArrayList<>(enrollments.values());
    }

    /**
     * Gets the total count of enrollments.
     *
     * @return number of enrollments
     */
    public int count() {
        return enrollments.size();
    }

    /**
     * Clears all enrollments (for testing).
     */
    public void clear() {
        enrollments.clear();
        enrollmentCounter = 1;
    }

    /**
     * Generates a unique enrollment ID.
     *
     * @return enrollment ID
     */
    private String generateEnrollmentId() {
        return "ENR" + String.format("%06d", enrollmentCounter++);
    }
}
