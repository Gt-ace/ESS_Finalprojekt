package ch.unisg.studybuddy;

import ch.unisg.studybuddy.model.*;
import ch.unisg.studybuddy.persistence.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Initializes the database with sample data for demonstration purposes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final StudentProfileRepository studentProfileRepository;
    private final CourseRepository courseRepository;
    private final CoursePreferenceRepository coursePreferenceRepository;
    private final CourseNoteRepository courseNoteRepository;
    private final StudySessionRepository studySessionRepository;
    private final TaskRepository taskRepository;

    @Override
    public void run(String... args) {
        if (studentProfileRepository.count() > 0) {
            log.info("Database already initialized, skipping sample data creation.");
            return;
        }

        log.info("Initializing database with sample data...");

        // Create student profile
        StudentProfile student = StudentProfile.builder()
                .name("Arthur Van Petegem & Jamie Maier")
                .email("studybuddy@student.unisg.ch")
                .locale("en")
                .settings("{\"theme\": \"light\", \"notifications\": true}")
                .build();
        student = studentProfileRepository.save(student);

        // Create courses
        Course essCourse = createCourse(student, "Design of Software Systems", "Fall 2025", 
                "Prof. Dr. Ronny Seiger", 
                "Covers software design patterns, frameworks, and architectural patterns.");
        
        Course mathCourse = createCourse(student, "Mathematics for Business", "Fall 2025",
                "Prof. Dr. Schmidt",
                "Linear algebra, calculus, and optimization methods.");
        
        Course econCourse = createCourse(student, "Microeconomics", "Fall 2025",
                "Prof. Dr. Mueller",
                "Supply and demand, market structures, and consumer theory.");

        Course dataCourse = createCourse(student, "Data Analytics", "Fall 2025",
                "Prof. Dr. Weber",
                "Statistical analysis, machine learning basics, and data visualization.");

        // Create course preferences
        createPreference(essCourse, 120, true, 1);
        createPreference(mathCourse, 90, true, 2);
        createPreference(econCourse, 60, false, 3);
        createPreference(dataCourse, 90, true, 2);

        // Create course notes
        createNote(essCourse, 
                "Key topics: SOLID principles, Design Patterns (Strategy, Observer, Factory), " +
                "Spring Boot, Vaadin, JPA/Hibernate",
                "• Frameworks use IoC (Hollywood Principle)\n• Spring Boot for rapid development\n• " +
                "Vaadin for Java-based web UI");

        createNote(mathCourse,
                "Focus on optimization problems and matrix operations for the exam.",
                "• Remember eigenvector decomposition\n• Practice Lagrange multipliers");

        // Create tasks for ESS Course
        createTask(essCourse, "Complete Assignment 6", Task.TaskType.PROJECT, 
                LocalDate.now().plusDays(21), 20, false,
                "Full-stack Spring Boot application with Vaadin UI");
        createTask(essCourse, "Read Chapter 10: Frameworks", Task.TaskType.READING,
                LocalDate.now().plusDays(3), 3, true, null);
        createTask(essCourse, "Practice Spring Boot exercises", Task.TaskType.EXERCISE,
                LocalDate.now().plusDays(5), 4, false, null);
        createTask(essCourse, "Review Design Patterns", Task.TaskType.EXAM_PREP,
                LocalDate.now().plusDays(7), 5, false, null);

        // Create tasks for Math Course
        createTask(mathCourse, "Problem Set 5", Task.TaskType.EXERCISE,
                LocalDate.now().plusDays(2), 4, false, "Chapter 7 exercises 1-20");
        createTask(mathCourse, "Study for Midterm", Task.TaskType.EXAM_PREP,
                LocalDate.now().plusDays(10), 15, false, null);

        // Create tasks for Econ Course
        createTask(econCourse, "Case Study Analysis", Task.TaskType.ASSIGNMENT,
                LocalDate.now().plusDays(4), 6, false, "Analyze market structure of tech industry");
        createTask(econCourse, "Chapter 8 Reading", Task.TaskType.READING,
                LocalDate.now().minusDays(1), 2, false, "Overdue - Game Theory chapter");

        // Create tasks for Data Course  
        createTask(dataCourse, "Python Data Analysis Project", Task.TaskType.PROJECT,
                LocalDate.now().plusDays(14), 12, false, null);
        createTask(dataCourse, "Complete DataCamp Module", Task.TaskType.EXERCISE,
                LocalDate.now(), 3, false, "Machine Learning Fundamentals");

        // Create study sessions
        LocalDateTime now = LocalDateTime.now();
        
        // ESS sessions
        createSession(essCourse, now.plusDays(1).withHour(9).withMinute(0), 90, "Library Study Room A", false);
        createSession(essCourse, now.plusDays(2).withHour(14).withMinute(0), 60, "Home Office", false);
        createSession(essCourse, now.minusDays(1).withHour(10).withMinute(0), 120, "Computer Lab", true);

        // Math sessions
        createSession(mathCourse, now.plusDays(1).withHour(14).withMinute(0), 60, "Math Tutorial Room", false);
        createSession(mathCourse, now.plusDays(3).withHour(11).withMinute(0), 90, "Library", false);

        // Econ sessions
        createSession(econCourse, now.plusDays(2).withHour(16).withMinute(0), 45, "Café Study", false);

        // Data sessions
        createSession(dataCourse, now.plusDays(1).withHour(16).withMinute(0), 60, "Computer Lab B", false);

        log.info("Sample data initialization complete!");
        log.info("Created: 1 student, 4 courses, {} tasks, {} study sessions",
                taskRepository.count(), studySessionRepository.count());
    }

    private Course createCourse(StudentProfile student, String title, String term, 
                                String instructor, String description) {
        Course course = Course.builder()
                .title(title)
                .term(term)
                .instructor(instructor)
                .description(description)
                .studentProfile(student)
                .build();
        return courseRepository.save(course);
    }

    private void createPreference(Course course, int dailyLimit, boolean notifications, int priority) {
        CoursePreference pref = CoursePreference.builder()
                .course(course)
                .preferredDailyWorkloadMinutes(dailyLimit)
                .notificationsEnabled(notifications)
                .priorityLevel(priority)
                .build();
        coursePreferenceRepository.save(pref);
    }

    private void createNote(Course course, String summary, String keyPoints) {
        CourseNote note = CourseNote.builder()
                .course(course)
                .summary(summary)
                .keyPoints(keyPoints)
                .build();
        courseNoteRepository.save(note);
    }

    private void createTask(Course course, String title, Task.TaskType type, 
                           LocalDate dueDate, int effort, boolean completed, String description) {
        Task task = Task.builder()
                .title(title)
                .description(description)
                .taskType(type)
                .dueDate(dueDate)
                .estimatedEffortHours(effort)
                .completed(completed)
                .course(course)
                .build();
        taskRepository.save(task);
    }

    private void createSession(Course course, LocalDateTime startTime, int duration, 
                               String location, boolean completed) {
        StudySession session = StudySession.builder()
                .course(course)
                .startTime(startTime)
                .durationMinutes(duration)
                .location(location)
                .completed(completed)
                .build();
        studySessionRepository.save(session);
    }
}

