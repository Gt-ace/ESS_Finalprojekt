package ch.unisg.studybuddy.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Result of a course progress calculation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResult {
    
    /**
     * The course ID.
     */
    private Long courseId;
    
    /**
     * The course title.
     */
    private String courseTitle;
    
    /**
     * Total number of tasks for the course.
     */
    private long totalTasks;
    
    /**
     * Number of completed tasks.
     */
    private long completedTasks;
    
    /**
     * Number of pending tasks.
     */
    private long pendingTasks;
    
    /**
     * Completion percentage (0-100).
     */
    private double completionPercentage;
    
    public static ProgressResult calculate(Long courseId, String courseTitle, long total, long completed) {
        double percentage = total > 0 ? (completed * 100.0 / total) : 0.0;
        return ProgressResult.builder()
                .courseId(courseId)
                .courseTitle(courseTitle)
                .totalTasks(total)
                .completedTasks(completed)
                .pendingTasks(total - completed)
                .completionPercentage(Math.round(percentage * 100.0) / 100.0)
                .build();
    }
}

