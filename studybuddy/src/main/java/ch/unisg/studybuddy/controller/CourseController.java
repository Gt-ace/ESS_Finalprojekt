package ch.unisg.studybuddy.controller;

import ch.unisg.studybuddy.model.Course;
import ch.unisg.studybuddy.model.CourseNote;
import ch.unisg.studybuddy.model.CoursePreference;
import ch.unisg.studybuddy.service.CourseService;
import ch.unisg.studybuddy.service.TaskService;
import ch.unisg.studybuddy.service.dto.ProgressResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return courseService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<Course>> getCoursesByStudentId(@PathVariable Long studentId) {
        return ResponseEntity.ok(courseService.findByStudentProfileId(studentId));
    }

    @PostMapping("/student/{studentId}")
    public ResponseEntity<Course> createCourse(
            @PathVariable Long studentId,
            @Valid @RequestBody Course course) {
        try {
            Course created = courseService.createCourse(studentId, course);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody Course course) {
        return courseService.findById(id)
                .map(existing -> {
                    existing.setTitle(course.getTitle());
                    existing.setTerm(course.getTerm());
                    existing.setInstructor(course.getInstructor());
                    existing.setDescription(course.getDescription());
                    return ResponseEntity.ok(courseService.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        if (courseService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        courseService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{courseId}/preference")
    public ResponseEntity<CoursePreference> getPreference(@PathVariable Long courseId) {
        return courseService.findPreferenceByCourseId(courseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{courseId}/preference")
    public ResponseEntity<CoursePreference> updatePreference(
            @PathVariable Long courseId,
            @Valid @RequestBody CoursePreference preference) {
        try {
            CoursePreference saved = courseService.savePreference(courseId, preference);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{courseId}/note")
    public ResponseEntity<CourseNote> getNote(@PathVariable Long courseId) {
        return courseService.findNoteByCourseId(courseId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{courseId}/note")
    public ResponseEntity<CourseNote> updateNote(
            @PathVariable Long courseId,
            @Valid @RequestBody CourseNote note) {
        try {
            CourseNote saved = courseService.saveNote(courseId, note);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<ProgressResult> getCourseProgress(@PathVariable Long id) {
        try {
            ProgressResult progress = taskService.calculateProgress(id);
            return ResponseEntity.ok(progress);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

