package ch.unisg.studybuddy.service;

import ch.unisg.studybuddy.model.Course;
import ch.unisg.studybuddy.model.Task;
import ch.unisg.studybuddy.persistence.CourseRepository;
import ch.unisg.studybuddy.persistence.TaskRepository;
import ch.unisg.studybuddy.service.dto.ProgressResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final CourseRepository courseRepository;

    @Override
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Override
    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    @Override
    public List<Task> findByCourseId(Long courseId) {
        return taskRepository.findByCourseId(courseId);
    }

    @Override
    public Task save(Task task) {
        return taskRepository.save(task);
    }

    @Override
    public Task createTask(Long courseId, Task task) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));
        
        task.setCourse(course);
        return taskRepository.save(task);
    }

    @Override
    public void deleteById(Long id) {
        taskRepository.deleteById(id);
    }

    @Override
    public Task markAsCompleted(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));
        task.setCompleted(true);
        return taskRepository.save(task);
    }

    @Override
    public Task markAsIncomplete(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with id: " + taskId));
        task.setCompleted(false);
        return taskRepository.save(task);
    }

    @Override
    public ProgressResult calculateProgress(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));
        
        long totalTasks = taskRepository.countByCourseId(courseId);
        long completedTasks = taskRepository.countCompletedByCourseId(courseId);
        
        return ProgressResult.calculate(courseId, course.getTitle(), totalTasks, completedTasks);
    }

    @Override
    public List<Task> getTasksByPriority(Long courseId) {
        List<Task> tasks;
        
        if (courseId != null) {
            tasks = taskRepository.findByCourseIdAndCompleted(courseId, false);
        } else {
            tasks = taskRepository.findAll().stream()
                    .filter(t -> !t.getCompleted())
                    .collect(Collectors.toList());
        }
        
        return tasks.stream()
                .sorted(Comparator.comparingDouble(Task::calculatePriorityScore).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public List<Task> getPendingTasksByStudentPrioritized(Long studentId) {
        List<Task> pendingTasks = taskRepository.findPendingTasksByStudentId(studentId);
        
        return pendingTasks.stream()
                .sorted(Comparator.comparingDouble(Task::calculatePriorityScore).reversed())
                .collect(Collectors.toList());
    }
}

