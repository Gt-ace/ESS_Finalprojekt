package ch.unisg.studybuddy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

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

    @Min(value = 0, message = "Preferred daily workload must be non-negative")
    @Column(nullable = false)
    @Builder.Default
    private Integer preferredDailyWorkloadMinutes = 120;

    @Column(nullable = false)
    @Builder.Default
    private Boolean notificationsEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer priorityLevel = 3;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", unique = true)
    @JsonIgnore
    private Course course;
}

