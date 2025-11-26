package edu.courseregistration.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents an enrollment record linking a student to a course.
 */
public class Enrollment {
    private String enrollmentId;
    private String studentId;
    private String courseId;
    private EnrollmentStatus status;
    private LocalDate enrolledDate;

    /**
     * Default constructor
     */
    public Enrollment() {
        this.enrolledDate = LocalDate.now();
        this.status = EnrollmentStatus.ENROLLED;
    }

    /**
     * Constructor with required fields
     */
    public Enrollment(String enrollmentId, String studentId, String courseId, EnrollmentStatus status) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.status = status;
        this.enrolledDate = LocalDate.now();
    }

    /**
     * Full constructor
     */
    public Enrollment(String enrollmentId, String studentId, String courseId,
            EnrollmentStatus status, LocalDate enrolledDate) {
        this.enrollmentId = enrollmentId;
        this.studentId = studentId;
        this.courseId = courseId;
        this.status = status;
        this.enrolledDate = enrolledDate;
    }

    // Getters
    public String getEnrollmentId() {
        return enrollmentId;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getCourseId() {
        return courseId;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public LocalDate getEnrolledDate() {
        return enrolledDate;
    }

    // Setters
    public void setEnrollmentId(String enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }

    public void setEnrolledDate(LocalDate enrolledDate) {
        this.enrolledDate = enrolledDate;
    }

    // Business methods
    public boolean isEnrolled() {
        return status == EnrollmentStatus.ENROLLED;
    }

    public boolean isWaitlisted() {
        return status == EnrollmentStatus.WAITLISTED;
    }

    public boolean isDropped() {
        return status == EnrollmentStatus.DROPPED;
    }

    public void enroll() {
        this.status = EnrollmentStatus.ENROLLED;
    }

    public void waitlist() {
        this.status = EnrollmentStatus.WAITLISTED;
    }

    public void drop() {
        this.status = EnrollmentStatus.DROPPED;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Enrollment that = (Enrollment) o;
        return Objects.equals(enrollmentId, that.enrollmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enrollmentId);
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "enrollmentId='" + enrollmentId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", courseId='" + courseId + '\'' +
                ", status=" + status +
                ", enrolledDate=" + enrolledDate +
                '}';
    }
}
