package ch.unisg.studybuddy.persistence;

import ch.unisg.studybuddy.model.StudySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StudySessionRepository extends JpaRepository<StudySession, Long> {
    
    List<StudySession> findByCourseId(Long courseId);
    
    @Query("SELECT s FROM StudySession s WHERE s.course.id = :courseId " +
           "AND s.startTime >= :startOfDay AND s.startTime < :endOfDay")
    List<StudySession> findByCourseIdAndDate(
            @Param("courseId") Long courseId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);
    
    @Query("SELECT s FROM StudySession s WHERE s.course.studentProfile.id = :studentId " +
           "AND s.startTime >= :startOfDay AND s.startTime < :endOfDay")
    List<StudySession> findByStudentIdAndDate(
            @Param("studentId") Long studentId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);
    
    @Query("SELECT s FROM StudySession s WHERE s.course.id = :courseId " +
           "AND s.startTime >= :rangeStart AND s.startTime < :rangeEnd")
    List<StudySession> findByCourseIdAndTimeRange(
            @Param("courseId") Long courseId,
            @Param("rangeStart") LocalDateTime rangeStart,
            @Param("rangeEnd") LocalDateTime rangeEnd);
}

