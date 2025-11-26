package edu.courseregistration.engine;

import edu.courseregistration.model.Course;
import edu.courseregistration.repository.EnrollmentRepository;
import edu.courseregistration.result.AllocationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SeatAllocatorTest {

    private SeatAllocator seatAllocator;
    private EnrollmentRepository enrollmentRepository;

    @BeforeEach
    void setUp() {
        enrollmentRepository = new EnrollmentRepository();
        seatAllocator = new SeatAllocator(enrollmentRepository);
    }

    // Allocation Tests
    @Test
    void testAllocateSeat_HasSpace_Success() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(5);

        AllocationResult result = seatAllocator.allocateSeat(course);

        assertTrue(result.isAllocated());
        assertFalse(result.isWaitlisted());
        assertEquals(6, course.getEnrolled());
    }

    @Test
    void testAllocateSeat_AtCapacity_Waitlisted() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(10);

        AllocationResult result = seatAllocator.allocateSeat(course);

        assertFalse(result.isAllocated());
        assertTrue(result.isWaitlisted());
        assertEquals(10, course.getEnrolled());
    }

    @Test
    void testAllocateSeat_ZeroCapacity_Waitlisted() {
        Course course = new Course("CS101", "Intro", 3, 0, 5);

        AllocationResult result = seatAllocator.allocateSeat(course);

        assertFalse(result.isAllocated());
        assertTrue(result.isWaitlisted());
        assertEquals(0, course.getEnrolled());
    }

    @Test
    void testAllocateSeat_OverCapacity_Waitlisted() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(15);

        AllocationResult result = seatAllocator.allocateSeat(course);

        assertFalse(result.isAllocated());
        assertTrue(result.isWaitlisted());
        assertEquals(15, course.getEnrolled());
    }

    @Test
    void testAllocateSeat_OneBelowCapacity_Success() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(9);

        AllocationResult result = seatAllocator.allocateSeat(course);

        assertTrue(result.isAllocated());
        assertFalse(result.isWaitlisted());
        assertEquals(10, course.getEnrolled());
    }

    @Test
    void testAllocateSeat_ExactlyAtCapacity_Waitlisted() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(10);

        AllocationResult result = seatAllocator.allocateSeat(course);

        assertFalse(result.isAllocated());
        assertTrue(result.isWaitlisted());
        assertEquals(10, course.getEnrolled());
    }

    // Release Seat Tests
    @Test
    void testReleaseSeat_HasEnrolled_Success() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(5);

        boolean result = seatAllocator.releaseSeat(course);

        assertTrue(result);
        assertEquals(4, course.getEnrolled());
    }

    @Test
    void testReleaseSeat_ZeroEnrolled_Failure() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(0);

        boolean result = seatAllocator.releaseSeat(course);

        assertFalse(result);
        assertEquals(0, course.getEnrolled());
    }

    @Test
    void testReleaseSeat_OneEnrolled_Success() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(1);

        boolean result = seatAllocator.releaseSeat(course);

        assertTrue(result);
        assertEquals(0, course.getEnrolled());
    }

    @Test
    void testReleaseSeat_FullCapacity_Success() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(10);

        boolean result = seatAllocator.releaseSeat(course);

        assertTrue(result);
        assertEquals(9, course.getEnrolled());
    }

    @Test
    void testReleaseSeat_NegativeEnrolled_Failure() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(-1);

        boolean result = seatAllocator.releaseSeat(course);

        assertFalse(result);
        assertEquals(-1, course.getEnrolled());
    }

    // Capacity Check Tests
    @Test
    void testIsAtCapacity_NotFull_False() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(5);

        assertFalse(seatAllocator.isAtCapacity(course));
    }

    @Test
    void testIsAtCapacity_ExactlyAtCapacity_True() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(10);

        assertTrue(seatAllocator.isAtCapacity(course));
    }

    @Test
    void testIsAtCapacity_OverCapacity_True() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(15);

        assertTrue(seatAllocator.isAtCapacity(course));
    }

    @Test
    void testIsAtCapacity_OneBelowCapacity_False() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(9);

        assertFalse(seatAllocator.isAtCapacity(course));
    }

    @Test
    void testIsAtCapacity_ZeroCapacity_True() {
        Course course = new Course("CS101", "Intro", 3, 0, 5);
        course.setEnrolled(0);

        assertTrue(seatAllocator.isAtCapacity(course));
    }

    @Test
    void testIsAtCapacity_ZeroEnrolled_False() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(0);

        assertFalse(seatAllocator.isAtCapacity(course));
    }

    // Available Seats Tests
    @Test
    void testGetAvailableSeatsCount_HasSpace_ReturnsCorrect() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(6);

        int available = seatAllocator.getAvailableSeatsCount(course);

        assertEquals(4, available);
    }

    @Test
    void testGetAvailableSeatsCount_AtCapacity_ReturnsZero() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(10);

        int available = seatAllocator.getAvailableSeatsCount(course);

        assertEquals(0, available);
    }

    @Test
    void testGetAvailableSeatsCount_OverCapacity_ReturnsZero() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(15);

        int available = seatAllocator.getAvailableSeatsCount(course);

        assertEquals(0, available);
    }

    @Test
    void testGetAvailableSeatsCount_Empty_ReturnsCapacity() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(0);

        int available = seatAllocator.getAvailableSeatsCount(course);

        assertEquals(10, available);
    }

    @Test
    void testGetAvailableSeatsCount_ZeroCapacity_ReturnsZero() {
        Course course = new Course("CS101", "Intro", 3, 0, 5);
        course.setEnrolled(0);

        int available = seatAllocator.getAvailableSeatsCount(course);

        assertEquals(0, available);
    }

    // Has Available Seats Tests
    @Test
    void testHasAvailableSeats_HasSpace_True() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(5);

        assertTrue(seatAllocator.hasAvailableSeats(course));
    }

    @Test
    void testHasAvailableSeats_AtCapacity_False() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(10);

        assertFalse(seatAllocator.hasAvailableSeats(course));
    }

    @Test
    void testHasAvailableSeats_OneSeatLeft_True() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(9);

        assertTrue(seatAllocator.hasAvailableSeats(course));
    }

    @Test
    void testHasAvailableSeats_OverCapacity_False() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(15);

        assertFalse(seatAllocator.hasAvailableSeats(course));
    }

    // Validation Tests
    @Test
    void testIsValidAllocation_BelowCapacity_Valid() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(5);

        assertTrue(seatAllocator.isValidAllocation(course));
    }

    @Test
    void testIsValidAllocation_AtCapacity_Valid() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(10);

        assertTrue(seatAllocator.isValidAllocation(course));
    }

    @Test
    void testIsValidAllocation_OverCapacity_Invalid() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(15);

        assertFalse(seatAllocator.isValidAllocation(course));
    }

    @Test
    void testIsValidAllocation_Negative_Invalid() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(-1);

        assertFalse(seatAllocator.isValidAllocation(course));
    }

    @Test
    void testIsValidAllocation_Zero_Valid() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(0);

        assertTrue(seatAllocator.isValidAllocation(course));
    }

    // Boundary Tests
    @Test
    void testAllocateSeat_MultipleSequential_CorrectCounting() {
        Course course = new Course("CS101", "Intro", 3, 3, 5);
        course.setEnrolled(0);

        AllocationResult r1 = seatAllocator.allocateSeat(course);
        assertTrue(r1.isAllocated());
        assertEquals(1, course.getEnrolled());

        AllocationResult r2 = seatAllocator.allocateSeat(course);
        assertTrue(r2.isAllocated());
        assertEquals(2, course.getEnrolled());

        AllocationResult r3 = seatAllocator.allocateSeat(course);
        assertTrue(r3.isAllocated());
        assertEquals(3, course.getEnrolled());

        AllocationResult r4 = seatAllocator.allocateSeat(course);
        assertTrue(r4.isWaitlisted());
        assertEquals(3, course.getEnrolled());
    }

    @Test
    void testReleaseSeat_MultipleSequential_CorrectCounting() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(3);

        assertTrue(seatAllocator.releaseSeat(course));
        assertEquals(2, course.getEnrolled());

        assertTrue(seatAllocator.releaseSeat(course));
        assertEquals(1, course.getEnrolled());

        assertTrue(seatAllocator.releaseSeat(course));
        assertEquals(0, course.getEnrolled());

        assertFalse(seatAllocator.releaseSeat(course));
        assertEquals(0, course.getEnrolled());
    }

    // New tests to kill surviving mutations
    @Test
    void testGetUtilizationPercentage_ZeroCapacity_ReturnsZero() {
        Course course = new Course("CS101", "Intro", 3, 0, 5);
        course.setEnrolled(0);

        double utilization = seatAllocator.getUtilizationPercentage(course);

        assertEquals(0.0, utilization, 0.01);
    }

    @Test
    void testGetUtilizationPercentage_FiftyPercent_ReturnsCorrect() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(5);

        double utilization = seatAllocator.getUtilizationPercentage(course);

        assertEquals(50.0, utilization, 0.01);
    }

    @Test
    void testGetUtilizationPercentage_FullCapacity_Returns100() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(10);

        double utilization = seatAllocator.getUtilizationPercentage(course);

        assertEquals(100.0, utilization, 0.01);
    }

    @Test
    void testGetUtilizationPercentage_OneStudent_ReturnsCorrect() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(1);

        double utilization = seatAllocator.getUtilizationPercentage(course);

        assertEquals(10.0, utilization, 0.01);
    }

    @Test
    void testGetAvailableSeatsCount_NegativeResult_ReturnsZero() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(15); // Over capacity

        int available = seatAllocator.getAvailableSeatsCount(course);

        assertEquals(0, available); // Should return 0, not negative
    }

    @Test
    void testHasOneSeatRemaining_ExactlyOne_True() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(9);

        assertTrue(seatAllocator.hasOneSeatRemaining(course));
    }

    @Test
    void testHasOneSeatRemaining_TwoSeats_False() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(8);

        assertFalse(seatAllocator.hasOneSeatRemaining(course));
    }

    @Test
    void testCanAccommodate_EnoughSeats_True() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(5);

        assertTrue(seatAllocator.canAccommodate(course, 3));
    }

    @Test
    void testCanAccommodate_ExactlyEnoughSeats_True() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(5);

        assertTrue(seatAllocator.canAccommodate(course, 5)); // Exactly 5 seats available
    }

    @Test
    void testCanAccommodate_NotEnoughSeats_False() {
        Course course = new Course("CS101", "Intro", 3, 10, 5);
        course.setEnrolled(5);

        assertFalse(seatAllocator.canAccommodate(course, 6)); // Only 5 available, need 6
    }
}
