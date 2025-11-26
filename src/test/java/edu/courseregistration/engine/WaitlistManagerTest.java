package edu.courseregistration.engine;

import edu.courseregistration.model.Course;
import edu.courseregistration.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WaitlistManager.
 * Tests queue operations, Optional handling, and boundary conditions.
 */
class WaitlistManagerTest {
    private WaitlistManager waitlistManager;
    private SeatAllocator seatAllocator;
    private EnrollmentRepository enrollmentRepository;

    @BeforeEach
    void setUp() {
        enrollmentRepository = new EnrollmentRepository();
        seatAllocator = new SeatAllocator(enrollmentRepository);
        waitlistManager = new WaitlistManager(seatAllocator);
    }

    // ========== addToWaitlist Tests ==========

    @Test
    void testAddToWaitlist_HasSpace_Success() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);

        boolean result = waitlistManager.addToWaitlist("S001", course);

        assertTrue(result);
        assertEquals(1, waitlistManager.getWaitlistSize("CS101"));
    }

    @Test
    void testAddToWaitlist_MultipleStudents_Success() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);

        waitlistManager.addToWaitlist("S001", course);
        waitlistManager.addToWaitlist("S002", course);
        waitlistManager.addToWaitlist("S003", course);

        assertEquals(3, waitlistManager.getWaitlistSize("CS101"));
    }

    @Test
    void testAddToWaitlist_AtCapacity_Failure() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 5);

        // Fill waitlist to capacity
        for (int i = 1; i <= 5; i++) {
            waitlistManager.addToWaitlist("S00" + i, course);
        }

        // Try to add one more - should fail
        boolean result = waitlistManager.addToWaitlist("S006", course);

        assertFalse(result);
        assertEquals(5, waitlistManager.getWaitlistSize("CS101"));
    }

    @Test
    void testAddToWaitlist_OneBelowCapacity_Success() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 5);

        // Add 4 students (one below capacity)
        for (int i = 1; i <= 4; i++) {
            waitlistManager.addToWaitlist("S00" + i, course);
        }

        // This should succeed
        boolean result = waitlistManager.addToWaitlist("S005", course);

        assertTrue(result);
        assertEquals(5, waitlistManager.getWaitlistSize("CS101"));
    }

    @Test
    void testAddToWaitlist_Duplicate_Failure() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);

        waitlistManager.addToWaitlist("S001", course);
        boolean result = waitlistManager.addToWaitlist("S001", course); // Duplicate

        assertFalse(result);
        assertEquals(1, waitlistManager.getWaitlistSize("CS101")); // Only added once
    }

    @Test
    void testAddToWaitlist_ZeroCapacity_Failure() {
        Course course = new Course("CS999", "Special", 3, 30, 0);

        boolean result = waitlistManager.addToWaitlist("S001", course);

        assertFalse(result);
        assertEquals(0, waitlistManager.getWaitlistSize("CS999"));
    }

    // ========== removeFromWaitlist Tests ==========

    @Test
    void testRemoveFromWaitlist_Exists_Success() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        waitlistManager.addToWaitlist("S001", course);

        boolean result = waitlistManager.removeFromWaitlist("S001", "CS101");

        assertTrue(result);
        assertEquals(0, waitlistManager.getWaitlistSize("CS101"));
    }

    @Test
    void testRemoveFromWaitlist_NotExists_Failure() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);

        boolean result = waitlistManager.removeFromWaitlist("S999", "CS101");

        assertFalse(result);
    }

    @Test
    void testRemoveFromWaitlist_EmptyList_Failure() {
        boolean result = waitlistManager.removeFromWaitlist("S001", "CS999");

        assertFalse(result);
    }

    @Test
    void testRemoveFromWaitlist_MultipleStudents_Success() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        waitlistManager.addToWaitlist("S001", course);
        waitlistManager.addToWaitlist("S002", course);
        waitlistManager.addToWaitlist("S003", course);

        waitlistManager.removeFromWaitlist("S002", "CS101"); // Remove middle

        assertEquals(2, waitlistManager.getWaitlistSize("CS101"));
        assertFalse(waitlistManager.isOnWaitlist("S002", "CS101"));
    }

    // ========== promoteFromWaitlist Tests (Optional Handling) ==========

    @Test
    void testPromoteFromWaitlist_HasWaitlistAndSeats_Success() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        course.setEnrolled(25); // Has available seats

        waitlistManager.addToWaitlist("S001", course);
        waitlistManager.addToWaitlist("S002", course);

        Optional<String> result = waitlistManager.promoteFromWaitlist(course);

        assertTrue(result.isPresent());
        assertEquals("S001", result.get()); // FIFO - first in, first out
        assertEquals(1, waitlistManager.getWaitlistSize("CS101")); // One removed
    }

    @Test
    void testPromoteFromWaitlist_NoWaitlist_EmptyOptional() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        course.setEnrolled(25); // Has available seats but no waitlist

        Optional<String> result = waitlistManager.promoteFromWaitlist(course);

        assertFalse(result.isPresent()); // Empty because no one on waitlist
    }

    @Test
    void testPromoteFromWaitlist_NoSeats_EmptyOptional() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        course.setEnrolled(30); // At capacity - no seats

        waitlistManager.addToWaitlist("S001", course);

        Optional<String> result = waitlistManager.promoteFromWaitlist(course);

        assertFalse(result.isPresent()); // Cannot promote without seats
        assertEquals(1, waitlistManager.getWaitlistSize("CS101")); // Still on waitlist
    }

    @Test
    void testPromoteFromWaitlist_EmptyWaitlist_EmptyOptional() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        course.setEnrolled(25);

        Optional<String> result = waitlistManager.promoteFromWaitlist(course);

        assertFalse(result.isPresent());
    }

    @Test
    void testPromoteFromWaitlist_FIFOOrdering_CorrectOrder() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        course.setEnrolled(28); // 2 seats available

        waitlistManager.addToWaitlist("S001", course);
        waitlistManager.addToWaitlist("S002", course);
        waitlistManager.addToWaitlist("S003", course);

        // First promotion
        Optional<String> first = waitlistManager.promoteFromWaitlist(course);
        assertEquals("S001", first.get());

        // Second promotion
        Optional<String> second = waitlistManager.promoteFromWaitlist(course);
        assertEquals("S002", second.get());

        assertEquals(1, waitlistManager.getWaitlistSize("CS101")); // S003 remains
    }

    // ========== getWaitlistPosition Tests (Loop and Counter) ==========

    @Test
    void testGetWaitlistPosition_FirstPosition() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        waitlistManager.addToWaitlist("S001", course);
        waitlistManager.addToWaitlist("S002", course);

        assertEquals(1, waitlistManager.getWaitlistPosition("S001", "CS101"));
    }

    @Test
    void testGetWaitlistPosition_MiddlePosition() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        waitlistManager.addToWaitlist("S001", course);
        waitlistManager.addToWaitlist("S002", course);
        waitlistManager.addToWaitlist("S003", course);

        assertEquals(2, waitlistManager.getWaitlistPosition("S002", "CS101"));
    }

    @Test
    void testGetWaitlistPosition_LastPosition() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        waitlistManager.addToWaitlist("S001", course);
        waitlistManager.addToWaitlist("S002", course);
        waitlistManager.addToWaitlist("S003", course);

        assertEquals(3, waitlistManager.getWaitlistPosition("S003", "CS101"));
    }

    @Test
    void testGetWaitlistPosition_NotOnList_NegativeOne() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        waitlistManager.addToWaitlist("S001", course);

        assertEquals(-1, waitlistManager.getWaitlistPosition("S999", "CS101"));
    }

    @Test
    void testGetWaitlistPosition_EmptyList_NegativeOne() {
        assertEquals(-1, waitlistManager.getWaitlistPosition("S001", "CS999"));
    }

    // ========== getWaitlistSize Tests ==========

    @Test
    void testGetWaitlistSize_Empty() {
        assertEquals(0, waitlistManager.getWaitlistSize("CS101"));
    }

    @Test
    void testGetWaitlistSize_Multiple() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        waitlistManager.addToWaitlist("S001", course);
        waitlistManager.addToWaitlist("S002", course);
        waitlistManager.addToWaitlist("S003", course);

        assertEquals(3, waitlistManager.getWaitlistSize("CS101"));
    }

    @Test
    void testGetWaitlistSize_AfterRemoval() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        waitlistManager.addToWaitlist("S001", course);
        waitlistManager.addToWaitlist("S002", course);
        waitlistManager.removeFromWaitlist("S001", "CS101");

        assertEquals(1, waitlistManager.getWaitlistSize("CS101"));
    }

    // ========== isWaitlistFull Tests (Boundary) ==========

    @Test
    void testIsWaitlistFull_NotFull_False() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 5);
        waitlistManager.addToWaitlist("S001", course);

        assertFalse(waitlistManager.isWaitlistFull(course));
    }

    @Test
    void testIsWaitlistFull_OneBelowCapacity_False() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 5);
        for (int i = 1; i <= 4; i++) {
            waitlistManager.addToWaitlist("S00" + i, course);
        }

        assertFalse(waitlistManager.isWaitlistFull(course)); // 4 >= 5 is false
    }

    @Test
    void testIsWaitlistFull_AtCapacity_True() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 5);
        for (int i = 1; i <= 5; i++) {
            waitlistManager.addToWaitlist("S00" + i, course);
        }

        assertTrue(waitlistManager.isWaitlistFull(course)); // 5 >= 5
    }

    @Test
    void testIsWaitlistFull_Empty_False() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 5);

        assertFalse(waitlistManager.isWaitlistFull(course));
    }

    // ========== isOnWaitlist Tests ==========

    @Test
    void testIsOnWaitlist_Exists_True() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        waitlistManager.addToWaitlist("S001", course);

        assertTrue(waitlistManager.isOnWaitlist("S001", "CS101"));
    }

    @Test
    void testIsOnWaitlist_NotExists_False() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        waitlistManager.addToWaitlist("S001", course);

        assertFalse(waitlistManager.isOnWaitlist("S999", "CS101"));
    }

    @Test
    void testIsOnWaitlist_EmptyList_False() {
        assertFalse(waitlistManager.isOnWaitlist("S001", "CS999"));
    }

    // ========== clearWaitlist Tests ==========

    @Test
    void testClearWaitlist_RemovesAll() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        waitlistManager.addToWaitlist("S001", course);
        waitlistManager.addToWaitlist("S002", course);

        waitlistManager.clearWaitlist("CS101");

        assertEquals(0, waitlistManager.getWaitlistSize("CS101"));
    }

    @Test
    void testClearWaitlist_NonExistent_NoError() {
        waitlistManager.clearWaitlist("CS999"); // Should not throw error
        assertEquals(0, waitlistManager.getWaitlistSize("CS999"));
    }

    // ========== getWaitlistedStudents Tests ==========

    @Test
    void testGetWaitlistedStudents_CorrectOrder() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);
        waitlistManager.addToWaitlist("S001", course);
        waitlistManager.addToWaitlist("S002", course);
        waitlistManager.addToWaitlist("S003", course);

        List<String> students = waitlistManager.getWaitlistedStudents("CS101");

        assertEquals(3, students.size());
        assertEquals("S001", students.get(0)); // FIFO order
        assertEquals("S002", students.get(1));
        assertEquals("S003", students.get(2));
    }

    @Test
    void testGetWaitlistedStudents_Empty() {
        List<String> students = waitlistManager.getWaitlistedStudents("CS999");

        assertNotNull(students);
        assertEquals(0, students.size());
    }

    // ========== hasWaitlistSpace Tests ==========

    @Test
    void testHasWaitlistSpace_HasSpace_True() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 5);
        waitlistManager.addToWaitlist("S001", course);

        assertTrue(waitlistManager.hasWaitlistSpace(course)); // 1 < 5
    }

    @Test
    void testHasWaitlistSpace_OneBelowCapacity_True() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 5);
        for (int i = 1; i <= 4; i++) {
            waitlistManager.addToWaitlist("S00" + i, course);
        }

        assertTrue(waitlistManager.hasWaitlistSpace(course)); // 4 < 5
    }

    @Test
    void testHasWaitlistSpace_AtCapacity_False() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 5);
        for (int i = 1; i <= 5; i++) {
            waitlistManager.addToWaitlist("S00" + i, course);
        }

        assertFalse(waitlistManager.hasWaitlistSpace(course)); // 5 < 5 is false
    }

    @Test
    void testHasWaitlistSpace_Empty_True() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 5);

        assertTrue(waitlistManager.hasWaitlistSpace(course)); // 0 < 5
    }
}
