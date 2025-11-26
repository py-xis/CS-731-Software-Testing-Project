package edu.courseregistration.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a course in the registration system.
 */
public class Course {
    private String courseId;
    private String courseName;
    private int credits;
    private int capacity;
    private int enrolled;
    private int waitlistCapacity;
    private List<String> prerequisites;
    private List<String> corequisites;

    /**
     * Default constructor
     */
    public Course() {
        this.prerequisites = new ArrayList<>();
        this.corequisites = new ArrayList<>();
        this.enrolled = 0;
    }

    /**
     * Constructor with basic fields
     */
    public Course(String courseId, String courseName, int credits, int capacity, int waitlistCapacity) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credits = credits;
        this.capacity = capacity;
        this.waitlistCapacity = waitlistCapacity;
        this.enrolled = 0;
        this.prerequisites = new ArrayList<>();
        this.corequisites = new ArrayList<>();
    }

    /**
     * Full constructor
     */
    public Course(String courseId, String courseName, int credits, int capacity,
            int waitlistCapacity, List<String> prerequisites, List<String> corequisites) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credits = credits;
        this.capacity = capacity;
        this.waitlistCapacity = waitlistCapacity;
        this.enrolled = 0;
        this.prerequisites = prerequisites != null ? new ArrayList<>(prerequisites) : new ArrayList<>();
        this.corequisites = corequisites != null ? new ArrayList<>(corequisites) : new ArrayList<>();
    }

    // Getters
    public String getCourseId() {
        return courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public int getCredits() {
        return credits;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getEnrolled() {
        return enrolled;
    }

    public int getWaitlistCapacity() {
        return waitlistCapacity;
    }

    public List<String> getPrerequisites() {
        return new ArrayList<>(prerequisites);
    }

    public List<String> getCorequisites() {
        return new ArrayList<>(corequisites);
    }

    // Setters
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setEnrolled(int enrolled) {
        this.enrolled = enrolled;
    }

    public void setWaitlistCapacity(int waitlistCapacity) {
        this.waitlistCapacity = waitlistCapacity;
    }

    public void setPrerequisites(List<String> prerequisites) {
        this.prerequisites = prerequisites != null ? new ArrayList<>(prerequisites) : new ArrayList<>();
    }

    public void setCorequisites(List<String> corequisites) {
        this.corequisites = corequisites != null ? new ArrayList<>(corequisites) : new ArrayList<>();
    }

    // Business methods
    public int getAvailableSeats() {
        return capacity - enrolled;
    }

    public boolean hasAvailableSeats() {
        return enrolled < capacity;
    }

    public boolean isFull() {
        return enrolled >= capacity;
    }

    public void incrementEnrolled() {
        this.enrolled++;
    }

    public void decrementEnrolled() {
        if (this.enrolled > 0) {
            this.enrolled--;
        }
    }

    public void addPrerequisite(String courseId) {
        if (!prerequisites.contains(courseId)) {
            prerequisites.add(courseId);
        }
    }

    public void addCorequisite(String courseId) {
        if (!corequisites.contains(courseId)) {
            corequisites.add(courseId);
        }
    }

    public boolean hasPrerequisites() {
        return !prerequisites.isEmpty();
    }

    public boolean hasCorequisites() {
        return !corequisites.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Course course = (Course) o;
        return Objects.equals(courseId, course.courseId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(courseId);
    }

    @Override
    public String toString() {
        return "Course{" +
                "courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", credits=" + credits +
                ", capacity=" + capacity +
                ", enrolled=" + enrolled +
                ", waitlistCapacity=" + waitlistCapacity +
                ", prerequisites=" + prerequisites.size() +
                ", corequisites=" + corequisites.size() +
                '}';
    }
}
