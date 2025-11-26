package edu.courseregistration.model;

/**
 * Represents the status of a student's enrollment in a course.
 */
public enum EnrollmentStatus {
    /**
     * Student is successfully enrolled in the course
     */
    ENROLLED,
    
    /**
     * Student is on the waitlist for the course
     */
    WAITLISTED,
    
    /**
     * Student has dropped the course
     */
    DROPPED
}
