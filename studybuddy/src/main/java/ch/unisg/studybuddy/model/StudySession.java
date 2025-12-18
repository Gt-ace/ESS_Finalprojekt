package ch.unisg.studybuddy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Represents a planned or completed study session.
 * Has a many-to-one relationship with Course.
 */
@Entity
@Table(name = "study_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudySession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Start time is required")
    @Column(nullable = false)
    private LocalDateTime startTime;

    /**
     * Duration of the study session in minutes.
     */
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(length = 200)
    private String location;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false)
    @Builder.Default
    private Boolean completed = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "course_id", nullable = false)
    @JsonIgnore
    private Course course;

    /**
     * Returns the end time of this study session.
     */
    public LocalDateTime getEndTime() {
        return startTime != null ? startTime.plusMinutes(durationMinutes) : null;
    }

    /**
     * Checks if this session overlaps with another session.
     */
    public boolean overlapsWith(StudySession other) {
        if (other == null || this.startTime == null || other.startTime == null) {
            return false;
        }
        LocalDateTime thisEnd = this.getEndTime();
        LocalDateTime otherEnd = other.getEndTime();
        
        return this.startTime.isBefore(otherEnd) && thisEnd.isAfter(other.startTime);
    }
}

