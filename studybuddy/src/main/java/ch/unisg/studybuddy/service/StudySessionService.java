package ch.unisg.studybuddy.service;

import ch.unisg.studybuddy.model.StudySession;
import ch.unisg.studybuddy.service.dto.ClashCheckResult;
import ch.unisg.studybuddy.service.dto.LoadCheckResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StudySessionService {
    
    List<StudySession> findAll();
    
    Optional<StudySession> findById(Long id);
    
    List<StudySession> findByCourseId(Long courseId);
    
    List<StudySession> findByStudentIdAndDate(Long studentId, LocalDate date);
    
    StudySession save(StudySession session);
    
    StudySession createSession(Long courseId, StudySession session);
    
    void deleteById(Long id);
    
    /**
     * BUSINESS LOGIC 1: Daily Load Check
     * Checks if adding a new session would exceed the preferred daily workload for a course.
     * 
     * @param courseId The course ID
     * @param date The date to check
     * @param proposedDurationMinutes The duration of the proposed session
     * @return LoadCheckResult with warning if workload would be exceeded
     */
    LoadCheckResult checkDailyLoad(Long courseId, LocalDate date, int proposedDurationMinutes);
    
    /**
     * BUSINESS LOGIC 2: Clash Detection
     * Detects if a proposed session would overlap with existing sessions.
     * 
     * @param courseId The course ID
     * @param proposedSession The proposed study session
     * @return ClashCheckResult with list of clashing sessions
     */
    ClashCheckResult checkForClashes(Long courseId, StudySession proposedSession);
    
    /**
     * Gets total study minutes for a course on a specific date.
     */
    int getTotalMinutesForCourseOnDate(Long courseId, LocalDate date);
}

