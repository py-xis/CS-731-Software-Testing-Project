package edu.courseregistration.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a student in the course registration system.
 */
public class Student {
    private String studentId;
    private String name;
    private String program;
    private int semester;
    private Set<String> completedCourses;
    private int currentCredits;

    /**
     * Default constructor
     */
    public Student() {
        this.completedCourses = new HashSet<>();
        this.currentCredits = 0;
    }

    /**
     * Constructor with all fields
     */
    public Student(String studentId, String name, String program, int semester) {
        this.studentId = studentId;
        this.name = name;
        this.program = program;
        this.semester = semester;
        this.completedCourses = new HashSet<>();
        this.currentCredits = 0;
    }

    /**
     * Full constructor
     */
    public Student(String studentId, String name, String program, int semester,
            Set<String> completedCourses, int currentCredits) {
        this.studentId = studentId;
        this.name = name;
        this.program = program;
        this.semester = semester;
        this.completedCourses = completedCourses != null ? new HashSet<>(completedCourses) : new HashSet<>();
        this.currentCredits = currentCredits;
    }

    // Getters
    public String getStudentId() {
        return studentId;
    }

    public String getName() {
        return name;
    }

    public String getProgram() {
        return program;
    }

    public int getSemester() {
        return semester;
    }

    public Set<String> getCompletedCourses() {
        return new HashSet<>(completedCourses);
    }

    public int getCurrentCredits() {
        return currentCredits;
    }

    // Setters
    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public void setSemester(int semester) {
        this.semester = semester;
    }

    public void setCompletedCourses(Set<String> completedCourses) {
        this.completedCourses = completedCourses != null ? new HashSet<>(completedCourses) : new HashSet<>();
    }

    public void setCurrentCredits(int currentCredits) {
        this.currentCredits = currentCredits;
    }

    // Business methods
    public void addCompletedCourse(String courseId) {
        this.completedCourses.add(courseId);
    }

    public void addCredits(int credits) {
        this.currentCredits += credits;
    }

    public void removeCredits(int credits) {
        this.currentCredits -= credits;
        if (this.currentCredits < 0) {
            this.currentCredits = 0;
        }
    }

    public boolean hasCompletedCourse(String courseId) {
        return completedCourses.contains(courseId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Student student = (Student) o;
        return Objects.equals(studentId, student.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }

    @Override
    public String toString() {
        return "Student{" +
                "studentId='" + studentId + '\'' +
                ", name='" + name + '\'' +
                ", program='" + program + '\'' +
                ", semester=" + semester +
                ", completedCourses=" + completedCourses.size() +
                ", currentCredits=" + currentCredits +
                '}';
    }
}
