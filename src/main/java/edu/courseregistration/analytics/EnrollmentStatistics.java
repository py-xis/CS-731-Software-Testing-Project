package edu.courseregistration.analytics;

import edu.courseregistration.model.Course;
import edu.courseregistration.model.Enrollment;
import edu.courseregistration.model.EnrollmentStatus;
import edu.courseregistration.repository.CourseRepository;
import edu.courseregistration.repository.EnrollmentRepository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Analytics component for enrollment statistics and reporting.
 * Provides aggregation and calculation methods for enrollment data analysis.
 */
public class EnrollmentStatistics {
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollmentStatistics(CourseRepository courseRepository, EnrollmentRepository enrollmentRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    /**
     * Calculates the fill rate (percentage of seats filled) for a specific course.
     *
     * @param courseId the course ID
     * @return fill rate as percentage (0.0 to 100.0), or -1.0 if course not found
     */
    public double calculateFillRate(String courseId) {
        Optional<Course> courseOpt = courseRepository.findById(courseId);
        if (!courseOpt.isPresent()) {
            return -1.0;
        }

        Course course = courseOpt.get();
        if (course.getCapacity() == 0) {
            return 0.0;
        }

        return (course.getEnrolled() * 100.0) / course.getCapacity();
    }

    /**
     * Gets the top N most popular courses by enrollment count.
     *
     * @param topN number of courses to return
     * @return list of courses sorted by enrollment (descending)
     */
    public List<Course> getMostPopularCourses(int topN) {
        if (topN <= 0) {
            return new ArrayList<>();
        }

        return courseRepository.findAll().stream()
                .sorted((c1, c2) -> Integer.compare(c2.getEnrolled(), c1.getEnrolled()))
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * Calculates average enrollment across all courses.
     *
     * @return average number of students enrolled per course, or 0.0 if no courses
     */
    public double getAverageEnrollmentPerCourse() {
        List<Course> courses = courseRepository.findAll();
        if (courses.isEmpty()) {
            return 0.0;
        }

        int totalEnrolled = courses.stream()
                .mapToInt(Course::getEnrolled)
                .sum();

        return (double) totalEnrolled / courses.size();
    }

    /**
     * Gets enrollment counts grouped by department (first 2-4 letters of course
     * ID).
     * Example: CS101 -> CS, MATH200 -> MATH
     *
     * @return map of department code to enrollment count
     */
    public Map<String, Integer> getEnrollmentByDepartment() {
        List<Course> courses = courseRepository.findAll();
        Map<String, Integer> departmentEnrollment = new HashMap<>();

        for (Course course : courses) {
            String department = extractDepartment(course.getCourseId());
            departmentEnrollment.put(department,
                    departmentEnrollment.getOrDefault(department, 0) + course.getEnrolled());
        }

        return departmentEnrollment;
    }

    /**
     * Counts total number of active enrollments (ENROLLED status).
     *
     * @return count of active enrollments
     */
    public long getTotalActiveEnrollments() {
        return enrollmentRepository.findAll().stream()
                .filter(Enrollment::isEnrolled)
                .count();
    }

    /**
     * Counts total number of waitlisted students across all courses.
     *
     * @return count of waitlisted enrollments
     */
    public long getTotalWaitlistedStudents() {
        return enrollmentRepository.findAll().stream()
                .filter(Enrollment::isWaitlisted)
                .count();
    }

    /**
     * Finds courses that are at or above a certain capacity threshold.
     *
     * @param thresholdPercentage threshold as percentage (0-100)
     * @return list of courses meeting the threshold
     */
    public List<Course> getCoursesAboveCapacityThreshold(double thresholdPercentage) {
        if (thresholdPercentage < 0 || thresholdPercentage > 100) {
            return new ArrayList<>();
        }

        return courseRepository.findAll().stream()
                .filter(course -> calculateFillRate(course.getCourseId()) >= thresholdPercentage)
                .collect(Collectors.toList());
    }

    /**
     * Calculates average class size (enrolled students) for courses above minimum
     * enrollment.
     *
     * @param minEnrollment minimum enrollment to include in average
     * @return average class size, or 0.0 if no courses meet criteria
     */
    public double getAverageClassSize(int minEnrollment) {
        if (minEnrollment < 0) {
            return 0.0;
        }

        List<Course> courses = courseRepository.findAll().stream()
                .filter(course -> course.getEnrolled() >= minEnrollment)
                .collect(Collectors.toList());

        if (courses.isEmpty()) {
            return 0.0;
        }

        int totalEnrolled = courses.stream()
                .mapToInt(Course::getEnrolled)
                .sum();

        return (double) totalEnrolled / courses.size();
    }

    /**
     * Gets total capacity across all courses.
     *
     * @return sum of all course capacities
     */
    public int getTotalSystemCapacity() {
        return courseRepository.findAll().stream()
                .mapToInt(Course::getCapacity)
                .sum();
    }

    /**
     * Gets total enrolled students across all courses.
     *
     * @return sum of enrolled counts
     */
    public int getTotalEnrolledStudents() {
        return courseRepository.findAll().stream()
                .mapToInt(Course::getEnrolled)
                .sum();
    }

    /**
     * Calculates overall system utilization rate.
     *
     * @return utilization percentage, or 0.0 if no capacity
     */
    public double getSystemUtilizationRate() {
        int totalCapacity = getTotalSystemCapacity();
        if (totalCapacity == 0) {
            return 0.0;
        }

        int totalEnrolled = getTotalEnrolledStudents();
        return (totalEnrolled * 100.0) / totalCapacity;
    }

    /**
     * Gets courses that are completely full.
     *
     * @return list of full courses
     */
    public List<Course> getFullCourses() {
        return courseRepository.findAll().stream()
                .filter(Course::isFull)
                .collect(Collectors.toList());
    }

    /**
     * Gets count of courses by enrollment status category.
     *
     * @return map with categories: "empty", "low", "medium", "high", "full"
     */
    public Map<String, Long> getCoursesByEnrollmentLevel() {
        Map<String, Long> levels = new HashMap<>();
        levels.put("empty", 0L);
        levels.put("low", 0L);
        levels.put("medium", 0L);
        levels.put("high", 0L);
        levels.put("full", 0L);

        for (Course course : courseRepository.findAll()) {
            double fillRate = calculateFillRate(course.getCourseId());
            String level;

            if (fillRate == 0) {
                level = "empty";
            } else if (fillRate < 25) {
                level = "low";
            } else if (fillRate < 50) {
                level = "medium";
            } else if (fillRate < 100) {
                level = "high";
            } else {
                level = "full";
            }

            levels.put(level, levels.get(level) + 1);
        }

        return levels;
    }

    /**
     * Extracts department code from course ID.
     * Assumes format like "CS101", "MATH200", etc.
     *
     * @param courseId the course ID
     * @return department code
     */
    private String extractDepartment(String courseId) {
        if (courseId == null || courseId.isEmpty()) {
            return "UNKNOWN";
        }

        // Find where numbers start
        int i = 0;
        while (i < courseId.length() && !Character.isDigit(courseId.charAt(i))) {
            i++;
        }

        if (i == 0) {
            return "UNKNOWN";
        }

        return courseId.substring(0, i);
    }
}
