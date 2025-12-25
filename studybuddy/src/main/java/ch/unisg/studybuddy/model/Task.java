package ch.unisg.studybuddy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Task title is required")
    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TaskType taskType = TaskType.READING;

    @Column
    private LocalDate dueDate;

    @Min(value = 1, message = "Effort must be at least 1 hour")
    @Max(value = 100, message = "Effort cannot exceed 100 hours")
    @Column(nullable = false)
    @Builder.Default
    private Integer estimatedEffortHours = 1;

    @Column(nullable = false)
    @Builder.Default
    private Boolean completed = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnore
    private Course course;

    public double calculatePriorityScore() {
        double daysWeight = 0;
        
        if (dueDate != null) {
            long daysUntilDue = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
            if (daysUntilDue < 0) {
                daysWeight = 100 + Math.abs(daysUntilDue);
            } else {
                daysWeight = Math.max(0, 100 - daysUntilDue);
            }
        }
        
        double effortWeight = estimatedEffortHours * 2.0;
        
        return daysWeight + effortWeight;
    }

    public enum TaskType {
        READING,
        EXERCISE,
        PROJECT,
        EXAM_PREP,
        ASSIGNMENT,
        OTHER
    }
}

