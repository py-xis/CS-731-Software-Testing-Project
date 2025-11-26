package edu.courseregistration.analytics;

import edu.courseregistration.model.Course;
import edu.courseregistration.model.Enrollment;
import edu.courseregistration.model.EnrollmentStatus;
import edu.courseregistration.repository.CourseRepository;
import edu.courseregistration.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Optimized unit tests for EnrollmentStatistics - Core tests only.
 * Focused on killing unique mutations with minimal redundancy.
 */
class EnrollmentStatisticsTest {
    private EnrollmentStatistics statistics;
    private CourseRepository courseRepository;
    private EnrollmentRepository enrollmentRepository;

    @BeforeEach
    void setUp() {
        courseRepository = new CourseRepository();
        enrollmentRepository = new EnrollmentRepository();
        statistics = new EnrollmentStatistics(courseRepository, enrollmentRepository);
    }

    @Test
    void testCalculateFillRate_FullCourse_Returns100() {
        Course course = new Course("CS101", "Intro", 3, 30, 10);
        course.setEnrolled(30);
        courseRepository.save(course);

        double fillRate = statistics.calculateFillRate("CS101");

        assertEquals(100.0, fillRate, 0.01);
    }

    @Test
    void testGetEnrollmentByDepartment_MultipleDepartments_GroupsCorrectly() {
        courseRepository.save(createCourse("CS101", 30));
        courseRepository.save(createCourse("CS201", 25));
        courseRepository.save(createCourse("MATH200", 40));
        courseRepository.save(createCourse("MATH300", 35));

        Map<String, Integer> byDept = statistics.getEnrollmentByDepartment();

        assertEquals(55, byDept.get("CS"));
        assertEquals(75, byDept.get("MATH"));
    }

    @Test
    void testGetCoursesByLevel_CategorizesCorrectly() {
        courseRepository.save(createCourseWithFillRate("CS101", 30, 0));
        courseRepository.save(createCourseWithFillRate("CS201", 40, 20));
        courseRepository.save(createCourseWithFillRate("CS301", 50, 40));
        courseRepository.save(createCourseWithFillRate("CS401", 60, 75));
        courseRepository.save(createCourseWithFillRate("CS501", 30, 100));

        Map<String, Long> levels = statistics.getCoursesByEnrollmentLevel();

        assertEquals(1L, levels.get("empty"));
        assertEquals(1L, levels.get("low"));
        assertEquals(1L, levels.get("medium"));
        assertEquals(1L, levels.get("high"));
        assertEquals(1L, levels.get("full"));
    }

    @Test
    void testGetAverageEnrollment_MultipleCourses_ReturnsAverage() {
        courseRepository.save(createCourse("CS101", 30));
        courseRepository.save(createCourse("CS201", 20));
        courseRepository.save(createCourse("CS301", 40));

        double avg = statistics.getAverageEnrollmentPerCourse();

        assertEquals(30.0, avg, 0.01);
    }

    @Test
    void testGetSystemUtilization_CalculatesCorrectly() {
        Course c1 = new Course("CS101", "C1", 3, 100, 10);
        c1.setEnrolled(75);
        Course c2 = new Course("CS201", "C2", 4, 100, 10);
        c2.setEnrolled(50);
        courseRepository.save(c1);
        courseRepository.save(c2);

        double utilization = statistics.getSystemUtilizationRate();

        assertEquals(62.5, utilization, 0.01);
    }

    @Test
    void testGetMostPopularCourses_ReturnsTopN() {
        courseRepository.save(createCourse("CS101", 30));
        courseRepository.save(createCourse("CS201", 25));
        courseRepository.save(createCourse("CS301", 35));
        courseRepository.save(createCourse("CS401", 20));

        List<Course> top2 = statistics.getMostPopularCourses(2);

        assertEquals(2, top2.size());
        assertEquals("CS301", top2.get(0).getCourseId());
        assertEquals("CS101", top2.get(1).getCourseId());
    }

    @Test
    void testGetFullCourses_ReturnsOnlyFull() {
        courseRepository.save(createCourseWithFillRate("CS101", 30, 100));
        courseRepository.save(createCourseWithFillRate("CS201", 40, 90));
        courseRepository.save(createCourseWithFillRate("CS301", 50, 100));

        List<Course> full = statistics.getFullCourses();

        assertEquals(2, full.size());
        assertTrue(full.stream().allMatch(Course::isFull));
    }

    @Test
    void testGetCoursesAboveThreshold_FiltersCorrectly() {
        courseRepository.save(createCourseWithFillRate("CS101", 30, 90));
        courseRepository.save(createCourseWithFillRate("CS201", 40, 60));
        courseRepository.save(createCourseWithFillRate("CS301", 50, 50));

        List<Course> above75 = statistics.getCoursesAboveCapacityThreshold(75.0);

        assertEquals(1, above75.size());
        assertEquals("CS101", above75.get(0).getCourseId());
    }

    @Test
    void testGetAverageClassSize_WithMinimum_FiltersCorrectly() {
        courseRepository.save(createCourse("CS101", 30));
        courseRepository.save(createCourse("CS201", 40));
        courseRepository.save(createCourse("CS301", 10));

        double avg = statistics.getAverageClassSize(20);

        assertEquals(35.0, avg, 0.01);
    }

    @Test
    void testGetTotalActiveEnrollments_CountsOnlyEnrolled() {
        enrollmentRepository.save(new Enrollment("E001", "S001", "CS101", EnrollmentStatus.ENROLLED));
        enrollmentRepository.save(new Enrollment("E002", "S002", "CS101", EnrollmentStatus.ENROLLED));
        enrollmentRepository.save(new Enrollment("E003", "S003", "CS101", EnrollmentStatus.WAITLISTED));
        enrollmentRepository.save(new Enrollment("E004", "S004", "CS201", EnrollmentStatus.DROPPED));

        long count = statistics.getTotalActiveEnrollments();

        assertEquals(2, count);
    }

    // Helper methods
    private Course createCourse(String courseId, int enrolled) {
        Course course = new Course(courseId, "Course " + courseId, 3, 100, 10);
        course.setEnrolled(enrolled);
        return course;
    }

    private Course createCourseWithFillRate(String courseId, int capacity, double fillRatePercent) {
        int enrolled = (int) (capacity * fillRatePercent / 100.0);
        Course course = new Course(courseId, "Course " + courseId, 3, capacity, 10);
        course.setEnrolled(enrolled);
        return course;
    }
}
