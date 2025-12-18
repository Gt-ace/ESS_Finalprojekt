package ch.unisg.studybuddy.persistence;

import ch.unisg.studybuddy.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    List<Task> findByCourseId(Long courseId);
    
    List<Task> findByCourseIdAndCompleted(Long courseId, Boolean completed);
    
    @Query("SELECT t FROM Task t WHERE t.course.id = :courseId ORDER BY t.dueDate ASC NULLS LAST")
    List<Task> findByCourseIdOrderByDueDate(@Param("courseId") Long courseId);
    
    @Query("SELECT t FROM Task t WHERE t.course.studentProfile.id = :studentId AND t.completed = false")
    List<Task> findPendingTasksByStudentId(@Param("studentId") Long studentId);
    
    @Query("SELECT t FROM Task t WHERE t.dueDate <= :date AND t.completed = false")
    List<Task> findOverdueTasks(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.course.id = :courseId AND t.completed = true")
    long countCompletedByCourseId(@Param("courseId") Long courseId);
    
    @Query("SELECT COUNT(t) FROM Task t WHERE t.course.id = :courseId")
    long countByCourseId(@Param("courseId") Long courseId);
}

