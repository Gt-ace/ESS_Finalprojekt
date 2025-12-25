package ch.unisg.studybuddy.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "courses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Course title is required")
    @Column(nullable = false)
    private String title;

    @Column(length = 50)
    private String term;

    @Column(length = 100)
    private String instructor;

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_profile_id")
    @JsonIgnore
    private StudentProfile studentProfile;

    @OneToOne(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private CoursePreference coursePreference;

    @OneToOne(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    private CourseNote courseNote;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StudySession> studySessions = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();

    public void setCoursePreference(CoursePreference preference) {
        if (preference == null) {
            if (this.coursePreference != null) {
                this.coursePreference.setCourse(null);
            }
        } else {
            preference.setCourse(this);
        }
        this.coursePreference = preference;
    }

    public void setCourseNote(CourseNote note) {
        if (note == null) {
            if (this.courseNote != null) {
                this.courseNote.setCourse(null);
            }
        } else {
            note.setCourse(this);
        }
        this.courseNote = note;
    }

    public void addStudySession(StudySession session) {
        studySessions.add(session);
        session.setCourse(this);
    }

    public void removeStudySession(StudySession session) {
        studySessions.remove(session);
        session.setCourse(null);
    }

    public void addTask(Task task) {
        tasks.add(task);
        task.setCourse(this);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        task.setCourse(null);
    }
}

