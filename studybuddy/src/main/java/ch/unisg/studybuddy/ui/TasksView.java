package ch.unisg.studybuddy.ui;

import ch.unisg.studybuddy.model.Course;
import ch.unisg.studybuddy.model.Task;
import ch.unisg.studybuddy.service.CourseService;
import ch.unisg.studybuddy.service.TaskService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Route(value = "tasks", layout = MainLayout.class)
@PageTitle("Tasks | StudyBuddy")
public class TasksView extends VerticalLayout {

    private final TaskService taskService;
    private final CourseService courseService;

    private Grid<Task> grid = new Grid<>(Task.class, false);
    private ComboBox<Course> courseFilter;
    private Checkbox showCompletedFilter;

    @Autowired
    public TasksView(TaskService taskService, CourseService courseService) {
        this.taskService = taskService;
        this.courseService = courseService;

        addClassName("tasks-view");
        setSizeFull();
        setPadding(true);

        add(createHeader());
        add(createFilters());
        add(createGrid());

        updateGrid();
    }

    private HorizontalLayout createHeader() {
        H2 title = new H2("✅ Tasks");
        title.addClassNames(LumoUtility.Margin.NONE);

        Button addButton = new Button("Add Task", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openTaskDialog(null));

        HorizontalLayout header = new HorizontalLayout(title, addButton);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        return header;
    }

    private HorizontalLayout createFilters() {
        courseFilter = new ComboBox<>("Filter by Course");
        courseFilter.setItems(courseService.findAll());
        courseFilter.setItemLabelGenerator(Course::getTitle);
        courseFilter.setClearButtonVisible(true);
        courseFilter.addValueChangeListener(e -> updateGrid());

        showCompletedFilter = new Checkbox("Show Completed");
        showCompletedFilter.setValue(true);
        showCompletedFilter.addValueChangeListener(e -> updateGrid());

        Button priorityButton = new Button("Sort by Priority", VaadinIcon.SORT.create());
        priorityButton.addClickListener(e -> {
            Long courseId = courseFilter.getValue() != null ? courseFilter.getValue().getId() : null;
            List<Task> prioritized = taskService.getTasksByPriority(courseId);
            grid.setItems(prioritized);
            Notification.show("Sorted by priority!", 2000, Notification.Position.BOTTOM_START);
        });

        HorizontalLayout filters = new HorizontalLayout(courseFilter, showCompletedFilter, priorityButton);
        filters.setAlignItems(FlexComponent.Alignment.END);
        filters.setSpacing(true);

        return filters;
    }

    private Grid<Task> createGrid() {
        grid.addComponentColumn(task -> {
            Checkbox checkbox = new Checkbox();
            checkbox.setValue(task.getCompleted());
            checkbox.addValueChangeListener(e -> {
                if (e.getValue()) {
                    taskService.markAsCompleted(task.getId());
                } else {
                    taskService.markAsIncomplete(task.getId());
                }
                updateGrid();
            });
            return checkbox;
        }).setHeader("").setWidth("60px").setFlexGrow(0);

        grid.addColumn(Task::getTitle).setHeader("Title").setFlexGrow(2);
        
        grid.addColumn(task -> task.getCourse() != null ? task.getCourse().getTitle() : "")
                .setHeader("Course").setFlexGrow(1);
        
        grid.addColumn(task -> task.getTaskType().toString()).setHeader("Type").setFlexGrow(1);

        grid.addComponentColumn(task -> {
            if (task.getDueDate() == null) {
                return new Span("-");
            }
            
            long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), task.getDueDate());
            Span dateSpan = new Span(task.getDueDate().toString());
            
            if (daysUntil < 0) {
                dateSpan.getStyle().set("color", "#dc2626").set("font-weight", "600");
                dateSpan.setText(task.getDueDate() + " (Overdue!)");
            } else if (daysUntil == 0) {
                dateSpan.getStyle().set("color", "#ea580c").set("font-weight", "600");
                dateSpan.setText("Today");
            } else if (daysUntil <= 3) {
                dateSpan.getStyle().set("color", "#ca8a04");
            }
            
            return dateSpan;
        }).setHeader("Due Date").setFlexGrow(1);

        grid.addColumn(task -> task.getEstimatedEffortHours() + "h")
                .setHeader("Effort").setWidth("80px").setFlexGrow(0);

        grid.addComponentColumn(task -> {
            double priority = task.calculatePriorityScore();
            Span badge = new Span(String.format("%.0f", priority));
            badge.getStyle()
                    .set("padding", "2px 8px")
                    .set("border-radius", "var(--lumo-border-radius-s)")
                    .set("font-size", "var(--lumo-font-size-xs)");
            
            if (priority > 80) {
                badge.getStyle().set("background", "#fecaca").set("color", "#991b1b");
            } else if (priority > 50) {
                badge.getStyle().set("background", "#fef08a").set("color", "#854d0e");
            } else {
                badge.getStyle().set("background", "#bbf7d0").set("color", "#166534");
            }
            
            return badge;
        }).setHeader("Priority").setWidth("80px").setFlexGrow(0);

        grid.addComponentColumn(task -> {
            Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            editBtn.addClickListener(e -> openTaskDialog(task));

            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> {
                taskService.deleteById(task.getId());
                updateGrid();
                Notification.show("Task deleted", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });

            return new HorizontalLayout(editBtn, deleteBtn);
        }).setHeader("Actions").setFlexGrow(0);

        grid.setWidthFull();
        grid.setHeight("500px");
        grid.getStyle()
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        return grid;
    }

    private void openTaskDialog(Task task) {
        List<Course> courses = courseService.findAll();
        if (courses.isEmpty()) {
            Notification.show("Please create a course first!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(task == null ? "Add New Task" : "Edit Task");

        TextField titleField = new TextField("Title");
        titleField.setWidthFull();
        titleField.setRequired(true);

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setWidthFull();

        ComboBox<Course> courseField = new ComboBox<>("Course");
        courseField.setItems(courses);
        courseField.setItemLabelGenerator(Course::getTitle);
        courseField.setRequired(true);

        ComboBox<Task.TaskType> typeField = new ComboBox<>("Type");
        typeField.setItems(Task.TaskType.values());

        DatePicker dueDateField = new DatePicker("Due Date");

        IntegerField effortField = new IntegerField("Estimated Effort (hours)");
        effortField.setMin(1);
        effortField.setMax(100);
        effortField.setValue(1);
        effortField.setStepButtonsVisible(true);

        if (task != null) {
            titleField.setValue(task.getTitle() != null ? task.getTitle() : "");
            descriptionField.setValue(task.getDescription() != null ? task.getDescription() : "");
            courseField.setValue(task.getCourse());
            typeField.setValue(task.getTaskType());
            dueDateField.setValue(task.getDueDate());
            effortField.setValue(task.getEstimatedEffortHours());
        } else {
            typeField.setValue(Task.TaskType.READING);
        }

        FormLayout form = new FormLayout(titleField, courseField, typeField, dueDateField, effortField, descriptionField);
        form.setColspan(descriptionField, 2);
        dialog.add(form);

        Button saveButton = new Button("Save", e -> {
            if (titleField.isEmpty() || courseField.isEmpty()) {
                Notification.show("Title and Course are required", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            Task toSave = task != null ? task : new Task();
            toSave.setTitle(titleField.getValue());
            toSave.setDescription(descriptionField.getValue());
            toSave.setTaskType(typeField.getValue());
            toSave.setDueDate(dueDateField.getValue());
            toSave.setEstimatedEffortHours(effortField.getValue());

            if (task == null) {
                taskService.createTask(courseField.getValue().getId(), toSave);
            } else {
                taskService.save(toSave);
            }

            dialog.close();
            updateGrid();
            Notification.show("Task saved!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void updateGrid() {
        List<Task> tasks;
        
        if (courseFilter.getValue() != null) {
            tasks = taskService.findByCourseId(courseFilter.getValue().getId());
        } else {
            tasks = taskService.findAll();
        }
        
        if (!showCompletedFilter.getValue()) {
            tasks = tasks.stream().filter(t -> !t.getCompleted()).toList();
        }
        
        grid.setItems(tasks);
    }
}

