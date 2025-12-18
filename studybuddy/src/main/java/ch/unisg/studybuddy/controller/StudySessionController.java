package ch.unisg.studybuddy.controller;

import ch.unisg.studybuddy.model.StudySession;
import ch.unisg.studybuddy.service.StudySessionService;
import ch.unisg.studybuddy.service.dto.ClashCheckResult;
import ch.unisg.studybuddy.service.dto.LoadCheckResult;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class StudySessionController {

    private final StudySessionService studySessionService;

    @GetMapping
    public ResponseEntity<List<StudySession>> getAllSessions() {
        return ResponseEntity.ok(studySessionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudySession> getSessionById(@PathVariable Long id) {
        return studySessionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<StudySession>> getSessionsByCourseId(@PathVariable Long courseId) {
        return ResponseEntity.ok(studySessionService.findByCourseId(courseId));
    }

    @GetMapping("/student/{studentId}/date/{date}")
    public ResponseEntity<List<StudySession>> getSessionsByStudentAndDate(
            @PathVariable Long studentId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(studySessionService.findByStudentIdAndDate(studentId, date));
    }

    @PostMapping("/course/{courseId}")
    public ResponseEntity<StudySession> createSession(
            @PathVariable Long courseId,
            @Valid @RequestBody StudySession session) {
        try {
            StudySession created = studySessionService.createSession(courseId, session);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudySession> updateSession(
            @PathVariable Long id,
            @Valid @RequestBody StudySession session) {
        return studySessionService.findById(id)
                .map(existing -> {
                    existing.setStartTime(session.getStartTime());
                    existing.setDurationMinutes(session.getDurationMinutes());
                    existing.setLocation(session.getLocation());
                    existing.setNotes(session.getNotes());
                    existing.setCompleted(session.getCompleted());
                    return ResponseEntity.ok(studySessionService.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable Long id) {
        if (studySessionService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        studySessionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ============ BUSINESS LOGIC ENDPOINTS ============

    /**
     * POST /api/sessions/check-load
     * Business Logic 1: Daily Load Check
     * 
     * Request body:
     * {
     *   "courseId": 1,
     *   "date": "2025-01-15",
     *   "proposedDurationMinutes": 60
     * }
     */
    @PostMapping("/check-load")
    public ResponseEntity<LoadCheckResult> checkLoad(@RequestBody LoadCheckRequest request) {
        LoadCheckResult result = studySessionService.checkDailyLoad(
                request.getCourseId(),
                request.getDate(),
                request.getProposedDurationMinutes()
        );
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/sessions/check-clash
     * Business Logic 2: Clash Detection
     */
    @PostMapping("/check-clash")
    public ResponseEntity<ClashCheckResult> checkClash(@RequestBody ClashCheckRequest request) {
        StudySession proposedSession = StudySession.builder()
                .id(request.getSessionId())
                .startTime(request.getStartTime())
                .durationMinutes(request.getDurationMinutes())
                .build();
        
        ClashCheckResult result = studySessionService.checkForClashes(
                request.getCourseId(),
                proposedSession
        );
        return ResponseEntity.ok(result);
    }

    // ============ REQUEST DTOs ============

    @lombok.Data
    public static class LoadCheckRequest {
        private Long courseId;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate date;
        private int proposedDurationMinutes;
    }

    @lombok.Data
    public static class ClashCheckRequest {
        private Long courseId;
        private Long sessionId; // Optional, for updates
        private java.time.LocalDateTime startTime;
        private int durationMinutes;
    }
}

