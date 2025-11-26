package edu.courseregistration.repository;

import edu.courseregistration.model.Course;

import java.util.*;

/**
 * Repository for managing Course entities.
 * In-memory implementation for simplicity.
 */
public class CourseRepository {
    private final Map<String, Course> courses;

    public CourseRepository() {
        this.courses = new HashMap<>();
    }

    /**
     * Saves a course to the repository.
     *
     * @param course the course to save
     * @return the saved course
     */
    public Course save(Course course) {
        courses.put(course.getCourseId(), course);
        return course;
    }

    /**
     * Finds a course by ID.
     *
     * @param courseId the course ID
     * @return Optional containing the course if found
     */
    public Optional<Course> findById(String courseId) {
        return Optional.ofNullable(courses.get(courseId));
    }

    /**
     * Finds all courses.
     *
     * @return list of all courses
     */
    public List<Course> findAll() {
        return new ArrayList<>(courses.values());
    }

    /**
     * Checks if a course exists.
     *
     * @param courseId the course ID
     * @return true if course exists
     */
    public boolean exists(String courseId) {
        return courses.containsKey(courseId);
    }

    /**
     * Gets the total count of courses.
     *
     * @return number of courses
     */
    public int count() {
        return courses.size();
    }

    /**
     * Finds courses with available seats.
     *
     * @return list of courses with available seats
     */
    public List<Course> findCoursesWithAvailableSeats() {
        List<Course> availableCourses = new ArrayList<>();
        for (Course course : courses.values()) {
            if (course.hasAvailableSeats()) {
                availableCourses.add(course);
            }
        }
        return availableCourses;
    }

    /**
     * Clears all courses (for testing).
     */
    public void clear() {
        courses.clear();
    }
}
