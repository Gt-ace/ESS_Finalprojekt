package ch.unisg.studybuddy.ui;

import ch.unisg.studybuddy.model.Course;
import ch.unisg.studybuddy.model.StudySession;
import ch.unisg.studybuddy.model.Task;
import ch.unisg.studybuddy.service.CourseService;
import ch.unisg.studybuddy.service.StudySessionService;
import ch.unisg.studybuddy.service.TaskService;
import ch.unisg.studybuddy.service.dto.ProgressResult;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "dashboard", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | StudyBuddy")
public class DashboardView extends VerticalLayout {

    private final CourseService courseService;
    private final TaskService taskService;
    private final StudySessionService studySessionService;

    @Autowired
    public DashboardView(CourseService courseService, TaskService taskService, 
                         StudySessionService studySessionService) {
        this.courseService = courseService;
        this.taskService = taskService;
        this.studySessionService = studySessionService;

        addClassName("dashboard-view");
        setPadding(true);
        setSpacing(true);

        add(createWelcomeSection());
        add(createStatsCards());
        add(createMainContent());
    }

    private Component createWelcomeSection() {
        H2 title = new H2("Welcome to StudyBuddy! 👋");
        title.addClassNames(LumoUtility.Margin.Bottom.NONE);

        Paragraph subtitle = new Paragraph("Track your courses, manage tasks, and plan study sessions.");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Top.XSMALL);

        VerticalLayout section = new VerticalLayout(title, subtitle);
        section.setSpacing(false);
        section.setPadding(false);
        return section;
    }

    private Component createStatsCards() {
        List<Course> courses = courseService.findAll();
        List<Task> allTasks = taskService.findAll();
        long pendingTasks = allTasks.stream().filter(t -> !t.getCompleted()).count();
        long completedTasks = allTasks.stream().filter(Task::getCompleted).count();

        HorizontalLayout cards = new HorizontalLayout();
        cards.setWidthFull();
        cards.setSpacing(true);

        cards.add(
                createStatCard("Courses", String.valueOf(courses.size()), VaadinIcon.ACADEMY_CAP, "#2563eb"),
                createStatCard("Pending Tasks", String.valueOf(pendingTasks), VaadinIcon.TASKS, "#dc2626"),
                createStatCard("Completed", String.valueOf(completedTasks), VaadinIcon.CHECK_CIRCLE, "#16a34a"),
                createStatCard("Today", LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd")), VaadinIcon.CALENDAR, "#7c3aed")
        );

        return cards;
    }

    private Component createStatCard(String label, String value, VaadinIcon iconType, String color) {
        Icon icon = iconType.create();
        icon.setSize("24px");
        icon.getStyle().set("color", color);

        H3 valueLabel = new H3(value);
        valueLabel.getStyle()
                .set("margin", "0")
                .set("font-size", "2rem")
                .set("font-weight", "700")
                .set("color", color);

        Span labelSpan = new Span(label);
        labelSpan.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        VerticalLayout content = new VerticalLayout(icon, valueLabel, labelSpan);
        content.setSpacing(false);
        content.setPadding(true);
        content.setAlignItems(FlexComponent.Alignment.START);
        content.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("min-width", "180px")
                .set("flex", "1");

        return content;
    }

    private Component createMainContent() {
        HorizontalLayout content = new HorizontalLayout();
        content.setWidthFull();
        content.setSpacing(true);

        content.add(createCourseProgressSection());
        content.add(createUpcomingTasksSection());

        return content;
    }

    private Component createCourseProgressSection() {
        VerticalLayout section = new VerticalLayout();
        section.addClassName("section-card");
        section.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("flex", "1");

        H3 title = new H3("📊 Course Progress");
        title.getStyle().set("margin-top", "0");
        section.add(title);

        List<Course> courses = courseService.findAll();
        
        if (courses.isEmpty()) {
            section.add(new Paragraph("No courses yet. Add your first course!"));
        } else {
            for (Course course : courses) {
                section.add(createCourseProgressItem(course));
            }
        }

        return section;
    }

    private Component createCourseProgressItem(Course course) {
        ProgressResult progress = taskService.calculateProgress(course.getId());

        Span courseName = new Span(course.getTitle());
        courseName.getStyle().set("font-weight", "500");

        Span percentage = new Span(String.format("%.0f%%", progress.getCompletionPercentage()));
        percentage.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);

        HorizontalLayout header = new HorizontalLayout(courseName, percentage);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);

        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue(progress.getCompletionPercentage() / 100.0);
        progressBar.setWidthFull();
        progressBar.getStyle().set("margin-top", "var(--lumo-space-xs)");

        Span taskInfo = new Span(String.format("%d/%d tasks completed", 
                progress.getCompletedTasks(), progress.getTotalTasks()));
        taskInfo.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL);

        VerticalLayout item = new VerticalLayout(header, progressBar, taskInfo);
        item.setSpacing(false);
        item.setPadding(false);
        item.getStyle()
                .set("margin-bottom", "var(--lumo-space-m)")
                .set("padding-bottom", "var(--lumo-space-m)")
                .set("border-bottom", "1px solid var(--lumo-contrast-10pct)");

        return item;
    }

    private Component createUpcomingTasksSection() {
        VerticalLayout section = new VerticalLayout();
        section.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("flex", "1");

        H3 title = new H3("⚡ Priority Tasks");
        title.getStyle().set("margin-top", "0");
        section.add(title);

        List<Task> prioritizedTasks = taskService.getTasksByPriority(null);
        
        if (prioritizedTasks.isEmpty()) {
            section.add(new Paragraph("No pending tasks. Great job!"));
        } else {
            // Show top 5 priority tasks
            prioritizedTasks.stream().limit(5).forEach(task -> {
                section.add(createTaskItem(task));
            });
        }

        return section;
    }

    private Component createTaskItem(Task task) {
        Icon icon = task.getCompleted() ? VaadinIcon.CHECK_CIRCLE.create() : VaadinIcon.CIRCLE_THIN.create();
        icon.setSize("18px");
        icon.getStyle().set("color", task.getCompleted() ? "#16a34a" : "var(--lumo-contrast-50pct)");

        Span title = new Span(task.getTitle());
        title.getStyle().set("font-weight", "500");
        if (task.getCompleted()) {
            title.getStyle().set("text-decoration", "line-through");
        }

        Span courseLabel = new Span(task.getCourse() != null ? task.getCourse().getTitle() : "");
        courseLabel.addClassNames(LumoUtility.FontSize.XSMALL);
        courseLabel.getStyle()
                .set("background", "var(--lumo-primary-color-10pct)")
                .set("color", "var(--lumo-primary-color)")
                .set("padding", "2px 8px")
                .set("border-radius", "var(--lumo-border-radius-s)");

        String dueDateText = "";
        if (task.getDueDate() != null) {
            long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), task.getDueDate());
            if (daysUntil < 0) {
                dueDateText = "Overdue!";
            } else if (daysUntil == 0) {
                dueDateText = "Due today";
            } else if (daysUntil == 1) {
                dueDateText = "Due tomorrow";
            } else {
                dueDateText = "Due in " + daysUntil + " days";
            }
        }
        Span dueDate = new Span(dueDateText);
        dueDate.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);

        VerticalLayout textContent = new VerticalLayout(title, new HorizontalLayout(courseLabel, dueDate));
        textContent.setSpacing(false);
        textContent.setPadding(false);

        HorizontalLayout item = new HorizontalLayout(icon, textContent);
        item.setAlignItems(FlexComponent.Alignment.START);
        item.getStyle()
                .set("padding", "var(--lumo-space-s)")
                .set("margin-bottom", "var(--lumo-space-xs)")
                .set("background", "var(--lumo-contrast-5pct)")
                .set("border-radius", "var(--lumo-border-radius-m)");

        return item;
    }
}

