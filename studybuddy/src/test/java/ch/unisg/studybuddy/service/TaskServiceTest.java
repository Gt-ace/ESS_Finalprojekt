package ch.unisg.studybuddy.service;

import ch.unisg.studybuddy.model.Course;
import ch.unisg.studybuddy.model.StudentProfile;
import ch.unisg.studybuddy.model.Task;
import ch.unisg.studybuddy.persistence.CourseRepository;
import ch.unisg.studybuddy.persistence.StudentProfileRepository;
import ch.unisg.studybuddy.persistence.TaskRepository;
import ch.unisg.studybuddy.service.dto.ProgressResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TaskService business logic methods.
 * Tests Business Logic 3 (Progress Roll-up) and Business Logic 4 (Task Prioritization).
 */
@SpringBootTest
@Transactional
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CourseRepository courseRepository;

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

        // Create test course
        testCourse = Course.builder()
                .title("Test Course")
                .term("Fall 2025")
                .instructor("Prof. Test")
                .studentProfile(testStudent)
                .build();
        testCourse = courseRepository.save(testCourse);
    }

    // ==================== BUSINESS LOGIC 3: PROGRESS ROLL-UP ====================

    @Test
    @DisplayName("Progress Roll-up: Empty course shows 0% completion")
    void testProgressRollup_EmptyCourse_ReturnsZeroPercent() {
        ProgressResult result = taskService.calculateProgress(testCourse.getId());
        
        assertEquals(testCourse.getId(), result.getCourseId());
        assertEquals(testCourse.getTitle(), result.getCourseTitle());
        assertEquals(0, result.getTotalTasks());
        assertEquals(0, result.getCompletedTasks());
        assertEquals(0, result.getPendingTasks());
        assertEquals(0.0, result.getCompletionPercentage());
    }

    @Test
    @DisplayName("Progress Roll-up: All tasks completed shows 100%")
    void testProgressRollup_AllCompleted_Returns100Percent() {
        // Create 3 completed tasks
        for (int i = 0; i < 3; i++) {
            Task task = Task.builder()
                    .title("Task " + i)
                    .course(testCourse)
                    .completed(true)
                    .estimatedEffortHours(2)
                    .build();
            taskRepository.save(task);
        }
        
        ProgressResult result = taskService.calculateProgress(testCourse.getId());
        
        assertEquals(3, result.getTotalTasks());
        assertEquals(3, result.getCompletedTasks());
        assertEquals(0, result.getPendingTasks());
        assertEquals(100.0, result.getCompletionPercentage());
    }

    @Test
    @DisplayName("Progress Roll-up: Mixed completion shows correct percentage")
    void testProgressRollup_MixedCompletion_ReturnsCorrectPercentage() {
        // Create 2 completed tasks
        for (int i = 0; i < 2; i++) {
            Task task = Task.builder()
                    .title("Completed Task " + i)
                    .course(testCourse)
                    .completed(true)
                    .estimatedEffortHours(1)
                    .build();
            taskRepository.save(task);
        }
        
        // Create 2 incomplete tasks
        for (int i = 0; i < 2; i++) {
            Task task = Task.builder()
                    .title("Pending Task " + i)
                    .course(testCourse)
                    .completed(false)
                    .estimatedEffortHours(1)
                    .build();
            taskRepository.save(task);
        }
        
        ProgressResult result = taskService.calculateProgress(testCourse.getId());
        
        assertEquals(4, result.getTotalTasks());
        assertEquals(2, result.getCompletedTasks());
        assertEquals(2, result.getPendingTasks());
        assertEquals(50.0, result.getCompletionPercentage());
    }

    @Test
    @DisplayName("Progress Roll-up: No completed tasks shows 0%")
    void testProgressRollup_NoneCompleted_ReturnsZeroPercent() {
        // Create 3 incomplete tasks
        for (int i = 0; i < 3; i++) {
            Task task = Task.builder()
                    .title("Task " + i)
                    .course(testCourse)
                    .completed(false)
                    .estimatedEffortHours(1)
                    .build();
            taskRepository.save(task);
        }
        
        ProgressResult result = taskService.calculateProgress(testCourse.getId());
        
        assertEquals(3, result.getTotalTasks());
        assertEquals(0, result.getCompletedTasks());
        assertEquals(3, result.getPendingTasks());
        assertEquals(0.0, result.getCompletionPercentage());
    }

    @Test
    @DisplayName("Progress Roll-up: One of three completed shows 33.33%")
    void testProgressRollup_OneOfThree_ReturnsCorrectPercentage() {
        // Create 1 completed task
        Task completed = Task.builder()
                .title("Completed Task")
                .course(testCourse)
                .completed(true)
                .estimatedEffortHours(1)
                .build();
        taskRepository.save(completed);
        
        // Create 2 incomplete tasks
        for (int i = 0; i < 2; i++) {
            Task task = Task.builder()
                    .title("Pending Task " + i)
                    .course(testCourse)
                    .completed(false)
                    .estimatedEffortHours(1)
                    .build();
            taskRepository.save(task);
        }
        
        ProgressResult result = taskService.calculateProgress(testCourse.getId());
        
        assertEquals(3, result.getTotalTasks());
        assertEquals(1, result.getCompletedTasks());
        // Should be approximately 33.33%
        assertTrue(result.getCompletionPercentage() >= 33.0 && result.getCompletionPercentage() <= 34.0);
    }

    // ==================== BUSINESS LOGIC 4: TASK PRIORITIZATION ====================

    @Test
    @DisplayName("Task Prioritization: Overdue tasks have highest priority")
    void testTaskPrioritization_OverdueTasks_HighestPriority() {
        // Create overdue task (yesterday)
        Task overdueTask = Task.builder()
                .title("Overdue Task")
                .course(testCourse)
                .dueDate(LocalDate.now().minusDays(1))
                .estimatedEffortHours(2)
                .completed(false)
                .build();
        taskRepository.save(overdueTask);
        
        // Create future task (in 10 days)
        Task futureTask = Task.builder()
                .title("Future Task")
                .course(testCourse)
                .dueDate(LocalDate.now().plusDays(10))
                .estimatedEffortHours(2)
                .completed(false)
                .build();
        taskRepository.save(futureTask);
        
        List<Task> prioritized = taskService.getTasksByPriority(testCourse.getId());
        
        assertEquals(2, prioritized.size());
        assertEquals("Overdue Task", prioritized.get(0).getTitle());
        assertEquals("Future Task", prioritized.get(1).getTitle());
    }

    @Test
    @DisplayName("Task Prioritization: Closer due date has higher priority")
    void testTaskPrioritization_CloserDueDate_HigherPriority() {
        // Create task due in 3 days
        Task soonTask = Task.builder()
                .title("Soon Task")
                .course(testCourse)
                .dueDate(LocalDate.now().plusDays(3))
                .estimatedEffortHours(2)
                .completed(false)
                .build();
        taskRepository.save(soonTask);
        
        // Create task due in 30 days
        Task laterTask = Task.builder()
                .title("Later Task")
                .course(testCourse)
                .dueDate(LocalDate.now().plusDays(30))
                .estimatedEffortHours(2)
                .completed(false)
                .build();
        taskRepository.save(laterTask);
        
        List<Task> prioritized = taskService.getTasksByPriority(testCourse.getId());
        
        assertEquals(2, prioritized.size());
        assertEquals("Soon Task", prioritized.get(0).getTitle());
        assertEquals("Later Task", prioritized.get(1).getTitle());
    }

    @Test
    @DisplayName("Task Prioritization: Higher effort increases priority")
    void testTaskPrioritization_HigherEffort_HigherPriority() {
        // Create two tasks with same due date but different effort
        Task highEffortTask = Task.builder()
                .title("High Effort Task")
                .course(testCourse)
                .dueDate(LocalDate.now().plusDays(10))
                .estimatedEffortHours(20)
                .completed(false)
                .build();
        taskRepository.save(highEffortTask);
        
        Task lowEffortTask = Task.builder()
                .title("Low Effort Task")
                .course(testCourse)
                .dueDate(LocalDate.now().plusDays(10))
                .estimatedEffortHours(1)
                .completed(false)
                .build();
        taskRepository.save(lowEffortTask);
        
        List<Task> prioritized = taskService.getTasksByPriority(testCourse.getId());
        
        assertEquals(2, prioritized.size());
        assertEquals("High Effort Task", prioritized.get(0).getTitle());
        assertEquals("Low Effort Task", prioritized.get(1).getTitle());
    }

    @Test
    @DisplayName("Task Prioritization: Completed tasks are excluded")
    void testTaskPrioritization_ExcludesCompletedTasks() {
        // Create completed task
        Task completedTask = Task.builder()
                .title("Completed Task")
                .course(testCourse)
                .dueDate(LocalDate.now().minusDays(1)) // Would be highest priority if incomplete
                .estimatedEffortHours(10)
                .completed(true)
                .build();
        taskRepository.save(completedTask);
        
        // Create incomplete task
        Task pendingTask = Task.builder()
                .title("Pending Task")
                .course(testCourse)
                .dueDate(LocalDate.now().plusDays(30))
                .estimatedEffortHours(1)
                .completed(false)
                .build();
        taskRepository.save(pendingTask);
        
        List<Task> prioritized = taskService.getTasksByPriority(testCourse.getId());
        
        assertEquals(1, prioritized.size());
        assertEquals("Pending Task", prioritized.get(0).getTitle());
    }

    @Test
    @DisplayName("Task Prioritization: Tasks without due date have lowest urgency from date")
    void testTaskPrioritization_NoDueDate_LowerPriority() {
        // Create task with due date
        Task withDueDate = Task.builder()
                .title("With Due Date")
                .course(testCourse)
                .dueDate(LocalDate.now().plusDays(5))
                .estimatedEffortHours(2)
                .completed(false)
                .build();
        taskRepository.save(withDueDate);
        
        // Create task without due date but same effort
        Task noDueDate = Task.builder()
                .title("No Due Date")
                .course(testCourse)
                .dueDate(null)
                .estimatedEffortHours(2)
                .completed(false)
                .build();
        taskRepository.save(noDueDate);
        
        List<Task> prioritized = taskService.getTasksByPriority(testCourse.getId());
        
        assertEquals(2, prioritized.size());
        // Task with due date should be higher priority
        assertEquals("With Due Date", prioritized.get(0).getTitle());
    }

    @Test
    @DisplayName("Task Prioritization: Priority score calculation is correct")
    void testTaskPrioritization_PriorityScoreCalculation() {
        // Task due today with 5 hours effort
        // Score = (100 - 0) + (5 * 2) = 100 + 10 = 110
        Task todayTask = Task.builder()
                .title("Today Task")
                .course(testCourse)
                .dueDate(LocalDate.now())
                .estimatedEffortHours(5)
                .completed(false)
                .build();
        
        double score = todayTask.calculatePriorityScore();
        assertEquals(110.0, score);
        
        // Task due in 50 days with 10 hours effort
        // Score = (100 - 50) + (10 * 2) = 50 + 20 = 70
        Task futureTask = Task.builder()
                .title("Future Task")
                .course(testCourse)
                .dueDate(LocalDate.now().plusDays(50))
                .estimatedEffortHours(10)
                .completed(false)
                .build();
        
        double futureScore = futureTask.calculatePriorityScore();
        assertEquals(70.0, futureScore);
        
        // Verify today task has higher priority
        assertTrue(score > futureScore);
    }
}

