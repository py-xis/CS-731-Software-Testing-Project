package edu.courseregistration.result;

/**
 * Result object for seat allocation operations.
 */
public class AllocationResult {
    private final boolean allocated;
    private final String message;
    private final boolean waitlisted;

    public AllocationResult(boolean allocated, boolean waitlisted, String message) {
        this.allocated = allocated;
        this.waitlisted = waitlisted;
        this.message = message;
    }

    public static AllocationResult success() {
        return new AllocationResult(true, false, "Seat allocated successfully");
    }

    public static AllocationResult waitlisted() {
        return new AllocationResult(false, true, "No seats available - added to waitlist");
    }

    public static AllocationResult failure(String message) {
        return new AllocationResult(false, false, message);
    }

    public boolean isAllocated() {
        return allocated;
    }

    public boolean isWaitlisted() {
        return waitlisted;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "AllocationResult{" +
                "allocated=" + allocated +
                ", waitlisted=" + waitlisted +
                ", message='" + message + '\'' +
                '}';
    }
}
