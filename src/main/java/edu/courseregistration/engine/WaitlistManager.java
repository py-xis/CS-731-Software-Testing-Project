package edu.courseregistration.engine;

import edu.courseregistration.model.Course;

import java.util.*;

/**
 * Manages waitlist operations for courses.
 * Contains queue operations and state management.
 */
public class WaitlistManager {
    // Course ID -> Queue of student IDs
    private final Map<String, Queue<String>> waitlists;
    private final SeatAllocator seatAllocator;

    public WaitlistManager(SeatAllocator seatAllocator) {
        this.waitlists = new HashMap<>();
        this.seatAllocator = seatAllocator;
    }

    /**
     * Adds a student to the waitlist for a course.
     * Contains size checks and boundary conditions.
     *
     * @param studentId the student ID
     * @param course    the course
     * @return true if added successfully
     */
    public boolean addToWaitlist(String studentId, Course course) {
        String courseId = course.getCourseId();

        // Initialize queue if it doesn't exist
        if (!waitlists.containsKey(courseId)) {
            waitlists.put(courseId, new LinkedList<>());
        }

        Queue<String> waitlist = waitlists.get(courseId);

        // Check waitlist capacity - boundary condition
        if (waitlist.size() >= course.getWaitlistCapacity()) {
            return false;
        }

        // Don't add duplicates
        if (waitlist.contains(studentId)) {
            return false;
        }

        waitlist.add(studentId);
        return true;
    }

    /**
     * Removes a student from the waitlist.
     *
     * @param studentId the student ID
     * @param courseId  the course ID
     * @return true if removed successfully
     */
    public boolean removeFromWaitlist(String studentId, String courseId) {
        if (!waitlists.containsKey(courseId)) {
            return false;
        }

        Queue<String> waitlist = waitlists.get(courseId);
        return waitlist.remove(studentId);
    }

    /**
     * Promotes the next student from waitlist when a seat becomes available.
     * Contains Optional handling and null checks.
     *
     * @param course the course
     * @return Optional containing student ID if promotion successful
     */
    public Optional<String> promoteFromWaitlist(Course course) {
        String courseId = course.getCourseId();

        // Check if waitlist exists and is not empty
        if (!waitlists.containsKey(courseId)) {
            return Optional.empty();
        }

        Queue<String> waitlist = waitlists.get(courseId);

        // Boundary condition: isEmpty vs size check
        if (waitlist.isEmpty()) {
            return Optional.empty();
        }

        // Check if seat is available
        if (!seatAllocator.hasAvailableSeats(course)) {
            return Optional.empty();
        }

        // Poll from queue (removes and returns head)
        String studentId = waitlist.poll();

        // Return mutation: null vs empty vs value
        return Optional.ofNullable(studentId);
    }

    /**
     * Gets the current position of a student in the waitlist.
     * Contains iteration and counting logic.
     *
     * @param studentId the student ID
     * @param courseId  the course ID
     * @return position (1-based), or -1 if not found
     */
    public int getWaitlistPosition(String studentId, String courseId) {
        if (!waitlists.containsKey(courseId)) {
            return -1;
        }

        Queue<String> waitlist = waitlists.get(courseId);
        int position = 1;

        for (String id : waitlist) {
            if (id.equals(studentId)) {
                return position;
            }
            // Increment mutation: ++ to --
            position++;
        }

        return -1;
    }

    /**
     * Gets the size of the waitlist for a course.
     *
     * @param courseId the course ID
     * @return size of waitlist
     */
    public int getWaitlistSize(String courseId) {
        if (!waitlists.containsKey(courseId)) {
            return 0;
        }

        return waitlists.get(courseId).size();
    }

    /**
     * Checks if waitlist is full for a course.
     * Boundary condition for mutation.
     *
     * @param course the course
     * @return true if waitlist is at capacity
     */
    public boolean isWaitlistFull(Course course) {
        String courseId = course.getCourseId();
        int currentSize = getWaitlistSize(courseId);

        // Comparison mutation: >= vs > vs ==
        return currentSize >= course.getWaitlistCapacity();
    }

    /**
     * Checks if a student is on the waitlist.
     *
     * @param studentId the student ID
     * @param courseId  the course ID
     * @return true if student is waitlisted
     */
    public boolean isOnWaitlist(String studentId, String courseId) {
        if (!waitlists.containsKey(courseId)) {
            return false;
        }

        return waitlists.get(courseId).contains(studentId);
    }

    /**
     * Clears the entire waitlist for a course.
     *
     * @param courseId the course ID
     */
    public void clearWaitlist(String courseId) {
        if (waitlists.containsKey(courseId)) {
            waitlists.get(courseId).clear();
        }
    }

    /**
     * Gets all students on waitlist for a course.
     *
     * @param courseId the course ID
     * @return list of student IDs in waitlist order
     */
    public List<String> getWaitlistedStudents(String courseId) {
        if (!waitlists.containsKey(courseId)) {
            return new ArrayList<>();
        }

        return new ArrayList<>(waitlists.get(courseId));
    }

    /**
     * Checks if there's space in the waitlist.
     * Arithmetic and comparison mutations.
     *
     * @param course the course
     * @return true if waitlist has space
     */
    public boolean hasWaitlistSpace(Course course) {
        int currentSize = getWaitlistSize(course.getCourseId());
        // Comparison mutation: < vs <= vs !=
        return currentSize < course.getWaitlistCapacity();
    }
}
