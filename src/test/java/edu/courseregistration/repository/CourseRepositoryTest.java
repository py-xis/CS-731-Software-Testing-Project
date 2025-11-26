package edu.courseregistration.repository;

import edu.courseregistration.model.Course;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Optimized tests for CourseRepository - Core CRUD only.
 */
class CourseRepositoryTest {
    private CourseRepository repository;

    @BeforeEach
    void setUp() {
        repository = new CourseRepository();
    }

    @Test
    void testSave_NewCourse_Success() {
        Course course = new Course("CS101", "Intro to CS", 3, 30, 10);

        Course saved = repository.save(course);

        assertNotNull(saved);
        assertEquals("CS101", saved.getCourseId());
    }

    @Test
    void testFindById_Exists_ReturnsCourse() {
        Course course = new Course("CS101", "Intro", 3, 30, 10);
        repository.save(course);

        Optional<Course> found = repository.findById("CS101");

        assertTrue(found.isPresent());
        assertEquals("CS101", found.get().getCourseId());
    }

    @Test
    void testFindById_NotExists_ReturnsEmpty() {
        Optional<Course> found = repository.findById("CS999");

        assertFalse(found.isPresent());
    }

    @Test
    void testFindAll_ReturnsAllCourses() {
        repository.save(new Course("CS101", "C1", 3, 30, 10));
        repository.save(new Course("CS201", "C2", 4, 40, 10));

        List<Course> all = repository.findAll();

        assertEquals(2, all.size());
    }

    @Test
    void testFindCoursesWithAvailableSeats_ReturnsOnlyAvailable() {
        Course full = new Course("CS101", "Full", 3, 30, 10);
        full.setEnrolled(30);
        Course available = new Course("CS201", "Available", 4, 40, 10);
        available.setEnrolled(20);

        repository.save(full);
        repository.save(available);

        List<Course> withSeats = repository.findCoursesWithAvailableSeats();

        assertEquals(1, withSeats.size());
        assertEquals("CS201", withSeats.get(0).getCourseId());
    }
}
