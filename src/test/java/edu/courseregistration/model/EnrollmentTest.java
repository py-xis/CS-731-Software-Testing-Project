package edu.courseregistration.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Minimal tests for Enrollment model to kill no-coverage mutations.
 * Only testing equals(), toString(), and isWaitlisted() for mutation coverage.
 */
class EnrollmentTest {

    @Test
    void testEquals_SameObject_ReturnsTrue() {
        Enrollment e1 = new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED);

        assertTrue(e1.equals(e1));
    }

    @Test
    void testEquals_Null_ReturnsFalse() {
        Enrollment e1 = new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED);

        assertFalse(e1.equals(null));
    }

    @Test
    void testEquals_DifferentClass_ReturnsFalse() {
        Enrollment e1 = new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED);

        assertFalse(e1.equals("string"));
    }

    @Test
    void testIsWaitlisted_WaitlistedStatus_ReturnsTrue() {
        Enrollment e1 = new Enrollment("E001", "S001", "CS101", EnrollmentStatus.WAITLISTED);

        assertTrue(e1.isWaitlisted());
    }

    @Test
    void testToString_ContainsFields() {
        Enrollment e1 = new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED);

        String str = e1.toString();

        assertTrue(str.contains("E001"));
        assertTrue(str.contains("S001"));
        assertTrue(str.contains("CS101"));
    }
}
