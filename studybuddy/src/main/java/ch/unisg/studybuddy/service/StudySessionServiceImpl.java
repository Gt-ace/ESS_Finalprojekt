package ch.unisg.studybuddy.service;

import ch.unisg.studybuddy.model.Course;
import ch.unisg.studybuddy.model.CoursePreference;
import ch.unisg.studybuddy.model.StudySession;
import ch.unisg.studybuddy.persistence.CoursePreferenceRepository;
import ch.unisg.studybuddy.persistence.CourseRepository;
import ch.unisg.studybuddy.persistence.StudySessionRepository;
import ch.unisg.studybuddy.service.dto.ClashCheckResult;
import ch.unisg.studybuddy.service.dto.LoadCheckResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudySessionServiceImpl implements StudySessionService {

    private final StudySessionRepository studySessionRepository;
    private final CourseRepository courseRepository;
    private final CoursePreferenceRepository coursePreferenceRepository;

    @Override
    public List<StudySession> findAll() {
        return studySessionRepository.findAll();
    }

    @Override
    public Optional<StudySession> findById(Long id) {
        return studySessionRepository.findById(id);
    }

    @Override
    public List<StudySession> findByCourseId(Long courseId) {
        return studySessionRepository.findByCourseId(courseId);
    }

    @Override
    public List<StudySession> findByStudentIdAndDate(Long studentId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        return studySessionRepository.findByStudentIdAndDate(studentId, startOfDay, endOfDay);
    }

    @Override
    public StudySession save(StudySession session) {
        return studySessionRepository.save(session);
    }

    @Override
    public StudySession createSession(Long courseId, StudySession session) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));
        
        session.setCourse(course);
        return studySessionRepository.save(session);
    }

    @Override
    public void deleteById(Long id) {
        studySessionRepository.deleteById(id);
    }

    /**
     * BUSINESS LOGIC 1: Daily Load Check
     * Sums study session durations per day and warns if a new session exceeds
     * the preferred workload defined in CoursePreference.
     */
    @Override
    public LoadCheckResult checkDailyLoad(Long courseId, LocalDate date, int proposedDurationMinutes) {
        // Get the course preference for daily limit
        CoursePreference preference = coursePreferenceRepository.findByCourseId(courseId)
                .orElse(CoursePreference.builder()
                        .preferredDailyWorkloadMinutes(120) // Default 2 hours
                        .build());
        
        int dailyLimit = preference.getPreferredDailyWorkloadMinutes();
        
        // Calculate current total for the day
        int currentTotal = getTotalMinutesForCourseOnDate(courseId, date);
        
        // Check if adding the proposed session would exceed the limit
        int newTotal = currentTotal + proposedDurationMinutes;
        
        if (newTotal > dailyLimit) {
            return LoadCheckResult.warning(currentTotal, proposedDurationMinutes, dailyLimit);
        }
        
        return LoadCheckResult.ok(currentTotal, proposedDurationMinutes, dailyLimit);
    }

    /**
     * BUSINESS LOGIC 2: Clash Detection
     * Detects and flags overlapping StudySessions for the same course.
     */
    @Override
    public ClashCheckResult checkForClashes(Long courseId, StudySession proposedSession) {
        if (proposedSession.getStartTime() == null || proposedSession.getDurationMinutes() == null) {
            return ClashCheckResult.noClash();
        }
        
        // Get all sessions for this course on the same day
        LocalDate sessionDate = proposedSession.getStartTime().toLocalDate();
        LocalDateTime startOfDay = sessionDate.atStartOfDay();
        LocalDateTime endOfDay = sessionDate.atTime(LocalTime.MAX);
        
        List<StudySession> existingSessions = studySessionRepository
                .findByCourseIdAndTimeRange(courseId, startOfDay, endOfDay);
        
        // Find clashing sessions
        List<StudySession> clashingSessions = new ArrayList<>();
        for (StudySession existing : existingSessions) {
            // Skip if it's the same session (for updates)
            if (proposedSession.getId() != null && proposedSession.getId().equals(existing.getId())) {
                continue;
            }
            
            if (proposedSession.overlapsWith(existing)) {
                clashingSessions.add(existing);
            }
        }
        
        if (clashingSessions.isEmpty()) {
            return ClashCheckResult.noClash();
        }
        
        return ClashCheckResult.withClashes(clashingSessions);
    }

    @Override
    public int getTotalMinutesForCourseOnDate(Long courseId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        List<StudySession> sessions = studySessionRepository
                .findByCourseIdAndDate(courseId, startOfDay, endOfDay);
        
        return sessions.stream()
                .mapToInt(StudySession::getDurationMinutes)
                .sum();
    }
}

