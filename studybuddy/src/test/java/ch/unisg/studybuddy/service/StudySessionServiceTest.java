package ch.unisg.studybuddy.service;

import ch.unisg.studybuddy.model.Course;
import ch.unisg.studybuddy.model.CoursePreference;
import ch.unisg.studybuddy.model.StudentProfile;
import ch.unisg.studybuddy.model.StudySession;
import ch.unisg.studybuddy.persistence.CoursePreferenceRepository;
import ch.unisg.studybuddy.persistence.CourseRepository;
import ch.unisg.studybuddy.persistence.StudentProfileRepository;
import ch.unisg.studybuddy.persistence.StudySessionRepository;
import ch.unisg.studybuddy.service.dto.ClashCheckResult;
import ch.unisg.studybuddy.service.dto.LoadCheckResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StudySessionService business logic methods.
 * Tests Business Logic 1 (Daily Load Check) and Business Logic 2 (Clash Detection).
 */
@SpringBootTest
@Transactional
class StudySessionServiceTest {

    @Autowired
    private StudySessionService studySessionService;

    @Autowired
    private StudySessionRepository studySessionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CoursePreferenceRepository coursePreferenceRepository;

    @Autowired
    private StudentProfileRepository studentProfileRepository;

    private StudentProfile testStudent;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        // Create test student
        testStudent = StudentProfile.builder()
                .name("Test Student")
                .email("test@unisg.ch")
                .locale("en")
                .build();
        testStudent = studentProfileRepository.save(testStudent);

        // Create test course with preference (120 min daily limit)
        testCourse = Course.builder()
                .title("Test Course")
                .term("Fall 2025")
                .instructor("Prof. Test")
                .studentProfile(testStudent)
                .build();
        testCourse = courseRepository.save(testCourse);

        CoursePreference preference = CoursePreference.builder()
                .course(testCourse)
                .preferredDailyWorkloadMinutes(120) // 2 hours limit
                .notificationsEnabled(true)
                .priorityLevel(3)
                .build();
        coursePreferenceRepository.save(preference);
    }

    // ==================== BUSINESS LOGIC 1: DAILY LOAD CHECK ====================

    @Test
    @DisplayName("Daily Load Check: Session within limit returns OK")
    void testDailyLoadCheck_WithinLimit_ReturnsOk() {
        LocalDate today = LocalDate.now();
        
        // Add a 60-minute session
        StudySession session = StudySession.builder()
                .course(testCourse)
                .startTime(today.atTime(9, 0))
                .durationMinutes(60)
                .build();
        studySessionRepository.save(session);
        
        // Check if adding another 30-minute session is OK (60 + 30 = 90 < 120)
        LoadCheckResult result = studySessionService.checkDailyLoad(testCourse.getId(), today, 30);
        
        assertFalse(result.isExceedsLimit());
        assertEquals(60, result.getCurrentMinutes());
        assertEquals(30, result.getProposedMinutes());
        assertEquals(90, result.getTotalMinutes());
        assertEquals(120, result.getDailyLimitMinutes());
        assertNull(result.getWarningMessage());
    }

    @Test
    @DisplayName("Daily Load Check: Session exceeds limit returns warning")
    void testDailyLoadCheck_ExceedsLimit_ReturnsWarning() {
        LocalDate today = LocalDate.now();
        
        // Add a 90-minute session
        StudySession session = StudySession.builder()
                .course(testCourse)
                .startTime(today.atTime(9, 0))
                .durationMinutes(90)
                .build();
        studySessionRepository.save(session);
        
        // Check if adding another 60-minute session exceeds limit (90 + 60 = 150 > 120)
        LoadCheckResult result = studySessionService.checkDailyLoad(testCourse.getId(), today, 60);
        
        assertTrue(result.isExceedsLimit());
        assertEquals(90, result.getCurrentMinutes());
        assertEquals(60, result.getProposedMinutes());
        assertEquals(150, result.getTotalMinutes());
        assertEquals(120, result.getDailyLimitMinutes());
        assertNotNull(result.getWarningMessage());
        assertTrue(result.getWarningMessage().contains("exceed"));
    }

    @Test
    @DisplayName("Daily Load Check: Empty day accepts any session within limit")
    void testDailyLoadCheck_EmptyDay_AcceptsSession() {
        LocalDate today = LocalDate.now();
        
        // No existing sessions, propose a 100-minute session (< 120)
        LoadCheckResult result = studySessionService.checkDailyLoad(testCourse.getId(), today, 100);
        
        assertFalse(result.isExceedsLimit());
        assertEquals(0, result.getCurrentMinutes());
        assertEquals(100, result.getProposedMinutes());
        assertEquals(100, result.getTotalMinutes());
    }

    @Test
    @DisplayName("Daily Load Check: Session exactly at limit is OK")
    void testDailyLoadCheck_ExactlyAtLimit_ReturnsOk() {
        LocalDate today = LocalDate.now();
        
        // Add a 60-minute session
        StudySession session = StudySession.builder()
                .course(testCourse)
                .startTime(today.atTime(9, 0))
                .durationMinutes(60)
                .build();
        studySessionRepository.save(session);
        
        // Check if adding exactly 60 more minutes is OK (60 + 60 = 120 = limit)
        LoadCheckResult result = studySessionService.checkDailyLoad(testCourse.getId(), today, 60);
        
        assertFalse(result.isExceedsLimit());
        assertEquals(120, result.getTotalMinutes());
    }

    // ==================== BUSINESS LOGIC 2: CLASH DETECTION ====================

    @Test
    @DisplayName("Clash Detection: Non-overlapping sessions return no clash")
    void testClashDetection_NoOverlap_ReturnsNoClash() {
        LocalDateTime session1Start = LocalDateTime.now().withHour(9).withMinute(0);
        
        // Create existing session: 9:00 - 10:00
        StudySession existingSession = StudySession.builder()
                .course(testCourse)
                .startTime(session1Start)
                .durationMinutes(60)
                .build();
        studySessionRepository.save(existingSession);
        
        // Propose session: 10:30 - 11:30 (no overlap)
        StudySession proposedSession = StudySession.builder()
                .startTime(session1Start.plusMinutes(90))
                .durationMinutes(60)
                .build();
        
        ClashCheckResult result = studySessionService.checkForClashes(testCourse.getId(), proposedSession);
        
        assertFalse(result.isHasClash());
        assertTrue(result.getClashingSessions().isEmpty());
        assertNull(result.getWarningMessage());
    }

    @Test
    @DisplayName("Clash Detection: Overlapping sessions return clash")
    void testClashDetection_Overlap_ReturnsClash() {
        LocalDateTime session1Start = LocalDateTime.now().withHour(9).withMinute(0);
        
        // Create existing session: 9:00 - 10:00
        StudySession existingSession = StudySession.builder()
                .course(testCourse)
                .startTime(session1Start)
                .durationMinutes(60)
                .build();
        studySessionRepository.save(existingSession);
        
        // Propose session: 9:30 - 10:30 (overlaps with existing)
        StudySession proposedSession = StudySession.builder()
                .startTime(session1Start.plusMinutes(30))
                .durationMinutes(60)
                .build();
        
        ClashCheckResult result = studySessionService.checkForClashes(testCourse.getId(), proposedSession);
        
        assertTrue(result.isHasClash());
        assertEquals(1, result.getClashingSessions().size());
        assertNotNull(result.getWarningMessage());
        assertTrue(result.getWarningMessage().contains("conflicts"));
    }

    @Test
    @DisplayName("Clash Detection: Session completely inside another returns clash")
    void testClashDetection_CompletelyInside_ReturnsClash() {
        LocalDateTime session1Start = LocalDateTime.now().withHour(9).withMinute(0);
        
        // Create existing session: 9:00 - 12:00
        StudySession existingSession = StudySession.builder()
                .course(testCourse)
                .startTime(session1Start)
                .durationMinutes(180)
                .build();
        studySessionRepository.save(existingSession);
        
        // Propose session: 10:00 - 11:00 (completely inside existing)
        StudySession proposedSession = StudySession.builder()
                .startTime(session1Start.plusHours(1))
                .durationMinutes(60)
                .build();
        
        ClashCheckResult result = studySessionService.checkForClashes(testCourse.getId(), proposedSession);
        
        assertTrue(result.isHasClash());
        assertEquals(1, result.getClashingSessions().size());
    }

    @Test
    @DisplayName("Clash Detection: Adjacent sessions (end = start) return no clash")
    void testClashDetection_AdjacentSessions_NoClash() {
        LocalDateTime session1Start = LocalDateTime.now().withHour(9).withMinute(0);
        
        // Create existing session: 9:00 - 10:00
        StudySession existingSession = StudySession.builder()
                .course(testCourse)
                .startTime(session1Start)
                .durationMinutes(60)
                .build();
        studySessionRepository.save(existingSession);
        
        // Propose session: 10:00 - 11:00 (starts exactly when other ends)
        StudySession proposedSession = StudySession.builder()
                .startTime(session1Start.plusHours(1))
                .durationMinutes(60)
                .build();
        
        ClashCheckResult result = studySessionService.checkForClashes(testCourse.getId(), proposedSession);
        
        assertFalse(result.isHasClash());
    }

    @Test
    @DisplayName("Clash Detection: Multiple clashing sessions detected")
    void testClashDetection_MultipleClashes_DetectsAll() {
        LocalDateTime baseTime = LocalDateTime.now().withHour(9).withMinute(0);
        
        // Create session 1: 9:00 - 10:00
        StudySession session1 = StudySession.builder()
                .course(testCourse)
                .startTime(baseTime)
                .durationMinutes(60)
                .build();
        studySessionRepository.save(session1);
        
        // Create session 2: 9:30 - 10:30
        StudySession session2 = StudySession.builder()
                .course(testCourse)
                .startTime(baseTime.plusMinutes(30))
                .durationMinutes(60)
                .build();
        studySessionRepository.save(session2);
        
        // Propose session: 9:15 - 10:15 (overlaps with both)
        StudySession proposedSession = StudySession.builder()
                .startTime(baseTime.plusMinutes(15))
                .durationMinutes(60)
                .build();
        
        ClashCheckResult result = studySessionService.checkForClashes(testCourse.getId(), proposedSession);
        
        assertTrue(result.isHasClash());
        assertEquals(2, result.getClashingSessions().size());
    }
}

