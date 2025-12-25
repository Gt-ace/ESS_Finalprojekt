package ch.unisg.studybuddy.controller;

import ch.unisg.studybuddy.model.Task;
import ch.unisg.studybuddy.service.TaskService;
import ch.unisg.studybuddy.service.dto.ProgressResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false, defaultValue = "false") boolean ordered) {
        
        if (ordered) {
            return ResponseEntity.ok(taskService.getTasksByPriority(courseId));
        }
        
        if (courseId != null) {
            return ResponseEntity.ok(taskService.findByCourseId(courseId));
        }
        
        return ResponseEntity.ok(taskService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return taskService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Task>> getTasksByCourseId(@PathVariable Long courseId) {
        return ResponseEntity.ok(taskService.findByCourseId(courseId));
    }

    @GetMapping("/prioritized")
    public ResponseEntity<List<Task>> getTasksByPriority(
            @RequestParam(required = false) Long courseId) {
        return ResponseEntity.ok(taskService.getTasksByPriority(courseId));
    }

    @GetMapping("/student/{studentId}/prioritized")
    public ResponseEntity<List<Task>> getPendingTasksForStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(taskService.getPendingTasksByStudentPrioritized(studentId));
    }

    @PostMapping("/course/{courseId}")
    public ResponseEntity<Task> createTask(
            @PathVariable Long courseId,
            @Valid @RequestBody Task task) {
        try {
            Task created = taskService.createTask(courseId, task);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody Task task) {
        return taskService.findById(id)
                .map(existing -> {
                    existing.setTitle(task.getTitle());
                    existing.setDescription(task.getDescription());
                    existing.setTaskType(task.getTaskType());
                    existing.setDueDate(task.getDueDate());
                    existing.setEstimatedEffortHours(task.getEstimatedEffortHours());
                    existing.setCompleted(task.getCompleted());
                    return ResponseEntity.ok(taskService.save(existing));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Task> markTaskComplete(@PathVariable Long id) {
        try {
            Task updated = taskService.markAsCompleted(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/incomplete")
    public ResponseEntity<Task> markTaskIncomplete(@PathVariable Long id) {
        try {
            Task updated = taskService.markAsIncomplete(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (taskService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        taskService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

