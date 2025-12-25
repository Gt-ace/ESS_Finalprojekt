package ch.unisg.studybuddy.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressResult {
    
    private Long courseId;
    private String courseTitle;
    private long totalTasks;
    private long completedTasks;
    private long pendingTasks;
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

