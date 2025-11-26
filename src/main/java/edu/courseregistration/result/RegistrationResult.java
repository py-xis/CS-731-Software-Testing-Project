package edu.courseregistration.result;

import edu.courseregistration.model.EnrollmentStatus;

/**
 * Result object for registration operations.
 */
public class RegistrationResult {
    private final boolean success;
    private final String message;
    private final EnrollmentStatus status;
    private final String enrollmentId;

    public RegistrationResult(boolean success, String message, EnrollmentStatus status, String enrollmentId) {
        this.success = success;
        this.message = message;
        this.status = status;
        this.enrollmentId = enrollmentId;
    }

    public static RegistrationResult success(String enrollmentId, EnrollmentStatus status) {
        return new RegistrationResult(true, "Registration successful", status, enrollmentId);
    }

    public static RegistrationResult failure(String message) {
        return new RegistrationResult(false, message, null, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public String getEnrollmentId() {
        return enrollmentId;
    }

    @Override
    public String toString() {
        return "RegistrationResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", status=" + status +
                ", enrollmentId='" + enrollmentId + '\'' +
                '}';
    }
}
