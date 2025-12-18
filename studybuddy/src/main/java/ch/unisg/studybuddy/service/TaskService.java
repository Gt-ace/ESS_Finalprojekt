package ch.unisg.studybuddy.service;

import ch.unisg.studybuddy.model.Task;
import ch.unisg.studybuddy.service.dto.ProgressResult;

import java.util.List;
import java.util.Optional;

public interface TaskService {
    
    List<Task> findAll();
    
    Optional<Task> findById(Long id);
    
    List<Task> findByCourseId(Long courseId);
    
    Task save(Task task);
    
    Task createTask(Long courseId, Task task);
    
    void deleteById(Long id);
    
    Task markAsCompleted(Long taskId);
    
    Task markAsIncomplete(Long taskId);
    
    /**
     * BUSINESS LOGIC 3: Progress Roll-up
     * Aggregates completed tasks per course and computes a completion percentage.
     * 
     * @param courseId The course ID
     * @return ProgressResult with completion percentage and task counts
     */
    ProgressResult calculateProgress(Long courseId);
    
    /**
     * BUSINESS LOGIC 4: Task Prioritization
     * Calculates a priority score based on due date proximity and estimated effort,
     * then returns tasks ordered by this score (highest priority first).
     * 
     * @param courseId The course ID (optional, null for all courses)
     * @return List of tasks ordered by priority score (descending)
     */
    List<Task> getTasksByPriority(Long courseId);
    
    /**
     * Gets all pending tasks for a student ordered by priority.
     * 
     * @param studentId The student ID
     * @return List of pending tasks ordered by priority
     */
    List<Task> getPendingTasksByStudentPrioritized(Long studentId);
}

