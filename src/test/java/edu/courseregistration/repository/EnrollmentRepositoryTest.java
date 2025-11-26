package edu.courseregistration.repository;

import edu.courseregistration.model.Enrollment;
import edu.courseregistration.model.EnrollmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Optimized tests for EnrollmentRepository - Core functionality.
 */
class EnrollmentRepositoryTest {
    private EnrollmentRepository enrollmentRepository;

    @BeforeEach
    void setUp() {
        enrollmentRepository = new EnrollmentRepository();
    }

    @Test
    void testSave_NewEnrollmentWithoutId_GeneratesId() {
        Enrollment enrollment = new Enrollment(null, "S001", "CS101", EnrollmentStatus.ENROLLED);

        Enrollment saved = enrollmentRepository.save(enrollment);

        assertNotNull(saved.getEnrollmentId());
        assertTrue(saved.getEnrollmentId().startsWith("ENR"));
        // Verify format is ENR followed by 6 digits
        assertTrue(saved.getEnrollmentId().matches("ENR\\d{6}"));

        // Test sequential generation
        Enrollment enrollment2 = new Enrollment(null, "S002", "CS201", EnrollmentStatus.ENROLLED);
        Enrollment saved2 = enrollmentRepository.save(enrollment2);

        // Extract numbers and verify they're different and sequential
        int id1 = Integer.parseInt(saved.getEnrollmentId().substring(3));
        int id2 = Integer.parseInt(saved2.getEnrollmentId().substring(3));
        assertEquals(id1 + 1, id2, "IDs should be sequential");
    }

    @Test
    void testSave_EnrollmentWithExistingId_KeepsId() {
        Enrollment enrollment = new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED);

        Enrollment saved = enrollmentRepository.save(enrollment);

        assertEquals("E001", saved.getEnrollmentId());
    }

    @Test
    void testFindById_Exists_ReturnsEnrollment() {
        Enrollment enrollment = new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED);
        enrollmentRepository.save(enrollment);

        Optional<Enrollment> found = enrollmentRepository.findById("E001");

        assertTrue(found.isPresent());
        assertEquals("S001", found.get().getStudentId());
    }

    @Test
    void testFindByStudentId_MultipleEnrollments_ReturnsAll() {
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED));
        enrollmentRepository.save(new Enrollment("E002", "S001", "CS201", EnrollmentStatus.ENROLLED));
        enrollmentRepository.save(new Enrollment("E003", "S002", "CS101", EnrollmentStatus.ENROLLED));

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId("S001");

        assertEquals(2, enrollments.size());
    }

    @Test
    void testFindByCourseId_MultipleEnrollments_ReturnsAll() {
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED));
        enrollmentRepository.save(new Enrollment("E002", "S002", "CS101", EnrollmentStatus.WAITLISTED));
        enrollmentRepository.save(new Enrollment("E003", "S003", "CS201", EnrollmentStatus.ENROLLED));

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId("CS101");

        assertEquals(2, enrollments.size());
        assertTrue(enrollments.stream().allMatch(e -> e.getCourseId().equals("CS101")));
    }

    @Test
    void testFindActiveEnrollments_OnlyEnrolled_ReturnsActive() {
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED));
        enrollmentRepository.save(new Enrollment("E002", "S001", "CS201", EnrollmentStatus.WAITLISTED));
        enrollmentRepository.save(new Enrollment("E003", "S001", "CS301", EnrollmentStatus.DROPPED));

        List<Enrollment> active = enrollmentRepository.findActiveEnrollmentsByStudentId("S001");

        assertEquals(1, active.size());
        assertEquals("CS101", active.get(0).getCourseId());
    }

    @Test
    void testFindByStudentAndCourse_Exists_ReturnsEnrollment() {
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED));
        enrollmentRepository.save(new Enrollment("E002", "S002", "CS101", EnrollmentStatus.ENROLLED));

        Optional<Enrollment> found = enrollmentRepository.findByStudentAndCourse("S001", "CS101");

        assertTrue(found.isPresent());
        assertEquals("E001", found.get().getEnrollmentId());
    }

    @Test
    void testFindByStudentAndCourse_DroppedExcluded_ReturnsEmpty() {
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.DROPPED));

        Optional<Enrollment> found = enrollmentRepository.findByStudentAndCourse("S001", "CS101");

        assertFalse(found.isPresent());
    }

    @Test
    void testIsStudentEnrolled_Enrolled_ReturnsTrue() {
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED));

        assertTrue(enrollmentRepository.isStudentEnrolled("S001", "CS101"));
    }

    @Test
    void testIsStudentEnrolled_Waitlisted_ReturnsFalse() {
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.WAITLISTED));

        assertFalse(enrollmentRepository.isStudentEnrolled("S001", "CS101"));
    }

    @Test
    void testCountByCourseAndStatus_MultipleMatches_ReturnsCount() {
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED));
        enrollmentRepository.save(new Enrollment("E002", "S002", "CS101", EnrollmentStatus.ENROLLED));
        enrollmentRepository.save(new Enrollment("E003", "S003", "CS101", EnrollmentStatus.WAITLISTED));

        long count = enrollmentRepository.countByCourseAndStatus("CS101", EnrollmentStatus.ENROLLED);

        assertEquals(2, count);
    }

    @Test
    void testCountByCourseAndStatus_DifferentStatus_Separates() {
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED));
        enrollmentRepository.save(new Enrollment("E002", "S002", "CS101", EnrollmentStatus.WAITLISTED));
        enrollmentRepository.save(new Enrollment("E003", "S003", "CS101", EnrollmentStatus.DROPPED));

        assertEquals(1, enrollmentRepository.countByCourseAndStatus("CS101", EnrollmentStatus.ENROLLED));
        assertEquals(1, enrollmentRepository.countByCourseAndStatus("CS101", EnrollmentStatus.WAITLISTED));
    }

    @Test
    void testDelete_Exists_ReturnsTrue() {
        Enrollment enrollment = new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED);
        enrollmentRepository.save(enrollment);

        boolean deleted = enrollmentRepository.delete("E001");

        assertTrue(deleted);
        assertFalse(enrollmentRepository.findById("E001").isPresent());
    }

    @Test
    void testFindAll_MultipleEnrollments_ReturnsAll() {
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED));
        enrollmentRepository.save(new Enrollment("E002", "S002", "CS201", EnrollmentStatus.WAITLISTED));

        List<Enrollment> all = enrollmentRepository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void testCount_MultipleEnrollments_ReturnsCount() {
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED));
        enrollmentRepository.save(new Enrollment("E002", "S002", "CS201", EnrollmentStatus.ENROLLED));
        enrollmentRepository.save(new Enrollment("E003", "S003", "CS301", EnrollmentStatus.WAITLISTED));

        assertEquals(3, enrollmentRepository.count());
    }

    @Test
    void testClear_RemovesAllEnrollments() {
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED));
        enrollmentRepository.save(new Enrollment("E002", "S002", "CS201", EnrollmentStatus.ENROLLED));

        enrollmentRepository.clear();

        assertEquals(0, enrollmentRepository.count());
        assertTrue(enrollmentRepository.findAll().isEmpty());
    }
}
