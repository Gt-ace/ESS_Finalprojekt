package ch.unisg.studybuddy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

/**
 * Represents study preferences for a specific course.
 * Has a one-to-one relationship with Course.
 */
@Entity
@Table(name = "course_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoursePreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Preferred maximum daily workload in minutes for this course.
     */
    @Min(value = 0, message = "Preferred daily workload must be non-negative")
    @Column(nullable = false)
    @Builder.Default
    private Integer preferredDailyWorkloadMinutes = 120;

    /**
     * Whether to receive notifications for this course.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean notificationsEnabled = true;

    /**
     * Priority level for this course (1 = highest, 5 = lowest).
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer priorityLevel = 3;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", unique = true)
    @JsonIgnore
    private Course course;
}

