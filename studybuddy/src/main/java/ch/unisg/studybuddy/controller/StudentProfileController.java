package ch.unisg.studybuddy.controller;

import ch.unisg.studybuddy.model.StudentProfile;
import ch.unisg.studybuddy.service.StudentProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentProfileController {

    private final StudentProfileService studentProfileService;

    @GetMapping
    public ResponseEntity<List<StudentProfile>> getAllStudents() {
        return ResponseEntity.ok(studentProfileService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentProfile> getStudentById(@PathVariable Long id) {
        return studentProfileService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<StudentProfile> getStudentByEmail(@PathVariable String email) {
        return studentProfileService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StudentProfile> createStudent(@Valid @RequestBody StudentProfile student) {
        if (studentProfileService.existsByEmail(student.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        StudentProfile created = studentProfileService.save(student);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentProfile> updateStudent(
            @PathVariable Long id,
            @Valid @RequestBody StudentProfile student) {
        return studentProfileService.findById(id)
                .map(existing -> {
                    existing.setName(student.getName());
                    existing.setEmail(student.getEmail());
                    existing.setLocale(student.getLocale());
                    existing.setSettings(student.getSettings());
                    return ResponseEntity.ok(studentProfileService.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        if (studentProfileService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        studentProfileService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

