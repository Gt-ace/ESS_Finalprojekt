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
        testStudent = StudentProfile.builder()
                .name("Test Student")
                .email("test@unisg.ch")
                .locale("en")
                .build();
        testStudent = studentProfileRepository.save(testStudent);

        testCourse = Course.builder()
                .title("Test Course")
                .term("Fall 2025")
                .instructor("Prof. Test")
                .studentProfile(testStudent)
                .build();
        testCourse = courseRepository.save(testCourse);

        CoursePreference preference = CoursePreference.builder()
                .course(testCourse)
                .preferredDailyWorkloadMinutes(120)
                .notificationsEnabled(true)
                .priorityLevel(3)
                .build();
        coursePreferenceRepository.save(preference);
    }

    @Test
    @DisplayName("Session within limit returns OK")
    void testDailyLoadCheck_WithinLimit_ReturnsOk() {
        LocalDate today = LocalDate.now();
        
        StudySession session = StudySession.builder()
                .course(testCourse)
                .startTime(today.atTime(9, 0))
                .durationMinutes(60)
                .build();
        studySessionRepository.save(session);
        
        LoadCheckResult result = studySessionService.checkDailyLoad(testCourse.getId(), today, 30);
        
        assertFalse(result.isExceedsLimit());
        assertEquals(60, result.getCurrentMinutes());
        assertEquals(30, result.getProposedMinutes());
        assertEquals(90, result.getTotalMinutes());
        assertEquals(120, result.getDailyLimitMinutes());
        assertNull(result.getWarningMessage());
    }

    @Test
    @DisplayName("Session exceeds limit returns warning")
    void testDailyLoadCheck_ExceedsLimit_ReturnsWarning() {
        LocalDate today = LocalDate.now();
        
        StudySession session = StudySession.builder()
                .course(testCourse)
                .startTime(today.atTime(9, 0))
                .durationMinutes(90)
                .build();
        studySessionRepository.save(session);
        
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
    @DisplayName("Empty day accepts session")
    void testDailyLoadCheck_EmptyDay_AcceptsSession() {
        LocalDate today = LocalDate.now();
        
        LoadCheckResult result = studySessionService.checkDailyLoad(testCourse.getId(), today, 100);
        
        assertFalse(result.isExceedsLimit());
        assertEquals(0, result.getCurrentMinutes());
        assertEquals(100, result.getProposedMinutes());
        assertEquals(100, result.getTotalMinutes());
    }

    @Test
    @DisplayName("Session exactly at limit is OK")
    void testDailyLoadCheck_ExactlyAtLimit_ReturnsOk() {
        LocalDate today = LocalDate.now();
        
        StudySession session = StudySession.builder()
                .course(testCourse)
                .startTime(today.atTime(9, 0))
                .durationMinutes(60)
                .build();
        studySessionRepository.save(session);
        
        LoadCheckResult result = studySessionService.checkDailyLoad(testCourse.getId(), today, 60);
        
        assertFalse(result.isExceedsLimit());
        assertEquals(120, result.getTotalMinutes());
    }

    @Test
    @DisplayName("Non-overlapping sessions return no clash")
    void testClashDetection_NoOverlap_ReturnsNoClash() {
        LocalDateTime session1Start = LocalDateTime.now().withHour(9).withMinute(0);
        
        StudySession existingSession = StudySession.builder()
                .course(testCourse)
                .startTime(session1Start)
                .durationMinutes(60)
                .build();
        studySessionRepository.save(existingSession);
        
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
    @DisplayName("Overlapping sessions return clash")
    void testClashDetection_Overlap_ReturnsClash() {
        LocalDateTime session1Start = LocalDateTime.now().withHour(9).withMinute(0);
        
        StudySession existingSession = StudySession.builder()
                .course(testCourse)
                .startTime(session1Start)
                .durationMinutes(60)
                .build();
        studySessionRepository.save(existingSession);
        
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
    @DisplayName("Session inside another returns clash")
    void testClashDetection_CompletelyInside_ReturnsClash() {
        LocalDateTime session1Start = LocalDateTime.now().withHour(9).withMinute(0);
        
        StudySession existingSession = StudySession.builder()
                .course(testCourse)
                .startTime(session1Start)
                .durationMinutes(180)
                .build();
        studySessionRepository.save(existingSession);
        
        StudySession proposedSession = StudySession.builder()
                .startTime(session1Start.plusHours(1))
                .durationMinutes(60)
                .build();
        
        ClashCheckResult result = studySessionService.checkForClashes(testCourse.getId(), proposedSession);
        
        assertTrue(result.isHasClash());
        assertEquals(1, result.getClashingSessions().size());
    }

    @Test
    @DisplayName("Adjacent sessions return no clash")
    void testClashDetection_AdjacentSessions_NoClash() {
        LocalDateTime session1Start = LocalDateTime.now().withHour(9).withMinute(0);
        
        StudySession existingSession = StudySession.builder()
                .course(testCourse)
                .startTime(session1Start)
                .durationMinutes(60)
                .build();
        studySessionRepository.save(existingSession);
        
        StudySession proposedSession = StudySession.builder()
                .startTime(session1Start.plusHours(1))
                .durationMinutes(60)
                .build();
        
        ClashCheckResult result = studySessionService.checkForClashes(testCourse.getId(), proposedSession);
        
        assertFalse(result.isHasClash());
    }

    @Test
    @DisplayName("Multiple clashing sessions detected")
    void testClashDetection_MultipleClashes_DetectsAll() {
        LocalDateTime baseTime = LocalDateTime.now().withHour(9).withMinute(0);
        
        StudySession session1 = StudySession.builder()
                .course(testCourse)
                .startTime(baseTime)
                .durationMinutes(60)
                .build();
        studySessionRepository.save(session1);
        
        StudySession session2 = StudySession.builder()
                .course(testCourse)
                .startTime(baseTime.plusMinutes(30))
                .durationMinutes(60)
                .build();
        studySessionRepository.save(session2);
        
        StudySession proposedSession = StudySession.builder()
                .startTime(baseTime.plusMinutes(15))
                .durationMinutes(60)
                .build();
        
        ClashCheckResult result = studySessionService.checkForClashes(testCourse.getId(), proposedSession);
        
        assertTrue(result.isHasClash());
        assertEquals(2, result.getClashingSessions().size());
    }
}

