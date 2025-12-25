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
    }

    @Test
    @DisplayName("Empty course shows 0% completion")
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
    @DisplayName("All tasks completed shows 100%")
    void testProgressRollup_AllCompleted_Returns100Percent() {
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
    @DisplayName("Mixed completion shows correct percentage")
    void testProgressRollup_MixedCompletion_ReturnsCorrectPercentage() {
        for (int i = 0; i < 2; i++) {
            Task task = Task.builder()
                    .title("Completed Task " + i)
                    .course(testCourse)
                    .completed(true)
                    .estimatedEffortHours(1)
                    .build();
            taskRepository.save(task);
        }
        
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
    @DisplayName("No completed tasks shows 0%")
    void testProgressRollup_NoneCompleted_ReturnsZeroPercent() {
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
    @DisplayName("One of three completed shows 33%")
    void testProgressRollup_OneOfThree_ReturnsCorrectPercentage() {
        Task completed = Task.builder()
                .title("Completed Task")
                .course(testCourse)
                .completed(true)
                .estimatedEffortHours(1)
                .build();
        taskRepository.save(completed);
        
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

    @Test
    @DisplayName("Overdue tasks have highest priority")
    void testTaskPrioritization_OverdueTasks_HighestPriority() {
        Task overdueTask = Task.builder()
                .title("Overdue Task")
                .course(testCourse)
                .dueDate(LocalDate.now().minusDays(1))
                .estimatedEffortHours(2)
                .completed(false)
                .build();
        taskRepository.save(overdueTask);
        
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
    @DisplayName("Closer due date has higher priority")
    void testTaskPrioritization_CloserDueDate_HigherPriority() {
        Task soonTask = Task.builder()
                .title("Soon Task")
                .course(testCourse)
                .dueDate(LocalDate.now().plusDays(3))
                .estimatedEffortHours(2)
                .completed(false)
                .build();
        taskRepository.save(soonTask);
        
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
    @DisplayName("Higher effort increases priority")
    void testTaskPrioritization_HigherEffort_HigherPriority() {
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
    @DisplayName("Completed tasks are excluded")
    void testTaskPrioritization_ExcludesCompletedTasks() {
        Task completedTask = Task.builder()
                .title("Completed Task")
                .course(testCourse)
                .dueDate(LocalDate.now().minusDays(1))
                .estimatedEffortHours(10)
                .completed(true)
                .build();
        taskRepository.save(completedTask);
        
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
    @DisplayName("Tasks without due date have lowest urgency")
    void testTaskPrioritization_NoDueDate_LowerPriority() {
        Task withDueDate = Task.builder()
                .title("With Due Date")
                .course(testCourse)
                .dueDate(LocalDate.now().plusDays(5))
                .estimatedEffortHours(2)
                .completed(false)
                .build();
        taskRepository.save(withDueDate);
        
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
        assertEquals("With Due Date", prioritized.get(0).getTitle());
    }

    @Test
    @DisplayName("Priority score calculation is correct")
    void testTaskPrioritization_PriorityScoreCalculation() {
        Task todayTask = Task.builder()
                .title("Today Task")
                .course(testCourse)
                .dueDate(LocalDate.now())
                .estimatedEffortHours(5)
                .completed(false)
                .build();
        
        double score = todayTask.calculatePriorityScore();
        assertEquals(110.0, score);
        
        Task futureTask = Task.builder()
                .title("Future Task")
                .course(testCourse)
                .dueDate(LocalDate.now().plusDays(50))
                .estimatedEffortHours(10)
                .completed(false)
                .build();
        
        double futureScore = futureTask.calculatePriorityScore();
        assertEquals(70.0, futureScore);
        
        assertTrue(score > futureScore);
    }
}

