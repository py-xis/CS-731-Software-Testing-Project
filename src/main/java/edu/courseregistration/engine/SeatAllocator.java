package edu.courseregistration.engine;

import edu.courseregistration.model.Course;
import edu.courseregistration.repository.EnrollmentRepository;
import edu.courseregistration.result.AllocationResult;

/**
 * Engine for managing seat allocation in courses.
 * Contains increment/decrement operations and boundary conditions.
 */
public class SeatAllocator {
    private final EnrollmentRepository enrollmentRepository;

    public SeatAllocator(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    /**
     * Attempts to allocate a seat in a course.
     * Contains increment operations and boundary comparisons.
     *
     * @param course the course to allocate seat in
     * @return AllocationResult indicating success or waitlist
     */
    public AllocationResult allocateSeat(Course course) {
        // Boundary condition: < vs <=
        if (course.getEnrolled() < course.getCapacity()) {
            // Increment operation - mutation target
            course.incrementEnrolled();
            return AllocationResult.success();
        }

        return AllocationResult.waitlisted();
    }

    /**
     * Releases a seat in a course when a student drops.
     * Contains decrement operation.
     *
     * @param course the course to release seat from
     * @return true if seat was released successfully
     */
    public boolean releaseSeat(Course course) {
        // Boundary condition: > vs >=
        if (course.getEnrolled() > 0) {
            // Decrement operation - mutation target
            course.decrementEnrolled();
            return true;
        }
        return false;
    }

    /**
     * Checks if seats are available in a course.
     * Simple comparison with mutation opportunities.
     *
     * @param course the course to check
     * @return true if seats are available
     */
    public boolean hasAvailableSeats(Course course) {
        // Relational operator mutation: < vs <= vs != vs ==
        return course.getEnrolled() < course.getCapacity();
    }

    /**
     * Gets the number of available seats.
     * Arithmetic operation for mutation testing.
     *
     * @param course the course
     * @return number of available seats
     */
    public int getAvailableSeatsCount(Course course) {
        // Arithmetic mutation: - vs + vs * vs /
        int available = course.getCapacity() - course.getEnrolled();

        // Boundary condition: < vs <=
        if (available < 0) {
            return 0;
        }

        return available;
    }

    /**
     * Checks if course is at capacity.
     * Boundary condition for mutation.
     *
     * @param course the course
     * @return true if course is full
     */
    public boolean isAtCapacity(Course course) {
        // Relational operator mutation: >= vs > vs ==
        return course.getEnrolled() >= course.getCapacity();
    }

    /**
     * Checks if course has exactly one seat remaining.
     * Specific boundary condition.
     *
     * @param course the course
     * @return true if only one seat left
     */
    public boolean hasOneSeatRemaining(Course course) {
        // Arithmetic and comparison mutation
        return (course.getCapacity() - course.getEnrolled()) == 1;
    }

    /**
     * Calculates utilization percentage.
     * Contains multiple arithmetic operations.
     *
     * @param course the course
     * @return utilization percentage (0-100)
     */
    public double getUtilizationPercentage(Course course) {
        if (course.getCapacity() == 0) {
            return 0.0;
        }

        // Arithmetic mutations: *, /, +, -
        return ((double) course.getEnrolled() / course.getCapacity()) * 100.0;
    }

    /**
     * Validates if allocation is within bounds.
     * Multiple boundary checks.
     *
     * @param course the course
     * @return true if enrolled count is valid
     */
    public boolean isValidAllocation(Course course) {
        // Multiple boundary conditions with AND
        return course.getEnrolled() >= 0 && course.getEnrolled() <= course.getCapacity();
    }

    /**
     * Checks if course can accommodate a specific number of students.
     * Arithmetic comparison for mutations.
     *
     * @param course           the course
     * @param numberOfStudents number of students to accommodate
     * @return true if enough seats available
     */
    public boolean canAccommodate(Course course, int numberOfStudents) {
        int availableSeats = course.getCapacity() - course.getEnrolled();
        // Comparison mutation: >= vs > vs ==
        return availableSeats >= numberOfStudents;
    }
}
