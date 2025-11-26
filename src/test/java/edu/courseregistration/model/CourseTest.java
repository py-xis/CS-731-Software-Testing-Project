package edu.courseregistration.model;

import org.junit.jupiter.api.Test;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Minimal tests for Course model to kill no-coverage mutations.
 */
class CourseTest {

    @Test
    void testGetCourseName_ReturnsName() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);

        assertEquals("Intro to CS", course.getCourseName());
    }

    @Test
    void testConstructor_WithPrerequisites_SetsPrerequisites() {
        Course course = new Course("CS101", "Intro", 3, 30, 10,
                Arrays.asList("CS100"), Arrays.asList("MATH100"));

        assertEquals(1, course.getPrerequisites().size());
        assertTrue(course.getPrerequisites().contains("CS100"));
        assertEquals(1, course.getCorequisites().size());
        assertTrue(course.getCorequisites().contains("MATH100"));
    }

    @Test
    void testAddCorequisite_DuplicatePrevention() {
        Course course = new Course("CS101", "Intro", 3, 30, 10);

        course.addCorequisite("MATH100");
        course.addCorequisite("MATH100"); // Try to add duplicate

        assertEquals(1, course.getCorequisites().size());
        assertTrue(course.getCorequisites().contains("MATH100"));
    }

    @Test
    void testDecrementEnrolled_AtZero_RemainsZero() {
        Course course = new Course("CS101", "Intro", 3, 30, 10);
        course.setEnrolled(0);

        course.decrementEnrolled();

        assertEquals(0, course.getEnrolled()); // Should not go negative
    }

    @Test
    void testDecrementEnrolled_AtOne_BecomesZero() {
        Course course = new Course("CS101", "Intro", 3, 30, 10);
        course.setEnrolled(1);

        course.decrementEnrolled();

        assertEquals(0, course.getEnrolled());
    }

    @Test
    void testIncrementEnrolled_IncreasesCount() {
        Course course = new Course("CS101", "Intro", 3, 30, 10);
        course.setEnrolled(5);

        course.incrementEnrolled();

        assertEquals(6, course.getEnrolled());
    }

    @Test
    void testEquals_SameId_ReturnsTrue() {
        Course course1 = new Course("CS101", "Intro", 3, 30, 10);
        Course course2 = new Course("CS101", "Different Name", 5, 50, 20);

        assertTrue(course1.equals(course2)); // Same courseId
    }

    @Test
    void testEquals_DifferentId_ReturnsFalse() {
        Course course1 = new Course("CS101", "Intro", 3, 30, 10);
        Course course2 = new Course("CS102", "Intro", 3, 30, 10);

        assertFalse(course1.equals(course2));
    }

    @Test
    void testEquals_Null_ReturnsFalse() {
        Course course = new Course("CS101", "Intro", 3, 30, 10);

        assertFalse(course.equals(null));
    }

    @Test
    void testGetAvailableSeats_CalculatesCorrectly() {
        Course course = new Course("CS101", "Intro", 3, 30, 10);
        course.setEnrolled(15);

        assertEquals(15, course.getAvailableSeats()); // 30 - 15 = 15
    }

    @Test
    void testSetPrerequisites_Null_SetsEmptyList() {
        Course course = new Course("CS101", "Intro", 3, 30, 10);

        course.setPrerequisites(null);

        assertEquals(0, course.getPrerequisites().size());
    }

    @Test
    void testSetCorequisites_Null_SetsEmptyList() {
        Course course = new Course("CS101", "Intro", 3, 30, 10);

        course.setCorequisites(null);

        assertEquals(0, course.getCorequisites().size());
    }
}
