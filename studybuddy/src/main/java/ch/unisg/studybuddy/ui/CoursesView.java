package ch.unisg.studybuddy.ui;

import ch.unisg.studybuddy.model.Course;
import ch.unisg.studybuddy.model.CourseNote;
import ch.unisg.studybuddy.model.CoursePreference;
import ch.unisg.studybuddy.model.StudentProfile;
import ch.unisg.studybuddy.service.CourseService;
import ch.unisg.studybuddy.service.StudentProfileService;
import ch.unisg.studybuddy.service.TaskService;
import ch.unisg.studybuddy.service.dto.ProgressResult;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "courses", layout = MainLayout.class)
@PageTitle("Courses | StudyBuddy")
public class CoursesView extends VerticalLayout {

    private final CourseService courseService;
    private final StudentProfileService studentProfileService;
    private final TaskService taskService;

    private Grid<Course> grid = new Grid<>(Course.class, false);
    private StudentProfile currentStudent;

    @Autowired
    public CoursesView(CourseService courseService, StudentProfileService studentProfileService,
                       TaskService taskService) {
        this.courseService = courseService;
        this.studentProfileService = studentProfileService;
        this.taskService = taskService;

        addClassName("courses-view");
        setSizeFull();
        setPadding(true);

        ensureStudentExists();

        add(createHeader());
        add(createGrid());

        updateGrid();
    }

    private void ensureStudentExists() {
        List<StudentProfile> students = studentProfileService.findAll();
        if (students.isEmpty()) {
            currentStudent = StudentProfile.builder()
                    .name("Default Student")
                    .email("student@unisg.ch")
                    .locale("en")
                    .build();
            currentStudent = studentProfileService.save(currentStudent);
        } else {
            currentStudent = students.get(0);
        }
    }

    private HorizontalLayout createHeader() {
        H2 title = new H2("📚 My Courses");
        title.addClassNames(LumoUtility.Margin.NONE);

        Button addButton = new Button("Add Course", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openCourseDialog(null));

        HorizontalLayout header = new HorizontalLayout(title, addButton);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        return header;
    }

    private Grid<Course> createGrid() {
        grid.addColumn(Course::getTitle).setHeader("Course Title").setFlexGrow(2);
        grid.addColumn(Course::getTerm).setHeader("Term").setFlexGrow(1);
        grid.addColumn(Course::getInstructor).setHeader("Instructor").setFlexGrow(1);
        
        grid.addComponentColumn(course -> {
            ProgressResult progress = taskService.calculateProgress(course.getId());
            ProgressBar bar = new ProgressBar();
            bar.setValue(progress.getCompletionPercentage() / 100.0);
            bar.setWidth("100px");
            
            Span label = new Span(String.format("%.0f%%", progress.getCompletionPercentage()));
            label.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
            
            HorizontalLayout layout = new HorizontalLayout(bar, label);
            layout.setAlignItems(FlexComponent.Alignment.CENTER);
            return layout;
        }).setHeader("Progress").setFlexGrow(1);

        grid.addComponentColumn(course -> {
            Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            editBtn.addClickListener(e -> openCourseDialog(course));

            Button noteBtn = new Button(new Icon(VaadinIcon.NOTEBOOK));
            noteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            noteBtn.addClickListener(e -> openNoteDialog(course));

            Button prefBtn = new Button(new Icon(VaadinIcon.COG));
            prefBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            prefBtn.addClickListener(e -> openPreferenceDialog(course));

            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> {
                courseService.deleteById(course.getId());
                updateGrid();
                Notification.show("Course deleted", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            });

            return new HorizontalLayout(editBtn, noteBtn, prefBtn, deleteBtn);
        }).setHeader("Actions").setFlexGrow(1);

        grid.setWidthFull();
        grid.setHeight("500px");
        grid.getStyle()
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)");

        return grid;
    }

    private void openCourseDialog(Course course) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(course == null ? "Add New Course" : "Edit Course");

        TextField titleField = new TextField("Title");
        titleField.setWidthFull();
        titleField.setRequired(true);

        TextField termField = new TextField("Term");
        termField.setWidthFull();
        termField.setPlaceholder("e.g., Fall 2025");

        TextField instructorField = new TextField("Instructor");
        instructorField.setWidthFull();

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setWidthFull();
        descriptionField.setMaxLength(500);

        if (course != null) {
            titleField.setValue(course.getTitle() != null ? course.getTitle() : "");
            termField.setValue(course.getTerm() != null ? course.getTerm() : "");
            instructorField.setValue(course.getInstructor() != null ? course.getInstructor() : "");
            descriptionField.setValue(course.getDescription() != null ? course.getDescription() : "");
        }

        FormLayout form = new FormLayout(titleField, termField, instructorField, descriptionField);
        form.setColspan(descriptionField, 2);
        dialog.add(form);

        Button saveButton = new Button("Save", e -> {
            if (titleField.isEmpty()) {
                Notification.show("Title is required", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            Course toSave = course != null ? course : new Course();
            toSave.setTitle(titleField.getValue());
            toSave.setTerm(termField.getValue());
            toSave.setInstructor(instructorField.getValue());
            toSave.setDescription(descriptionField.getValue());

            if (course == null) {
                courseService.createCourse(currentStudent.getId(), toSave);
            } else {
                courseService.save(toSave);
            }

            dialog.close();
            updateGrid();
            Notification.show("Course saved!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());

        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void openNoteDialog(Course course) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Course Notes: " + course.getTitle());
        dialog.setWidth("600px");

        CourseNote note = courseService.findNoteByCourseId(course.getId())
                .orElse(CourseNote.builder().build());

        TextArea summaryField = new TextArea("Summary");
        summaryField.setWidthFull();
        summaryField.setHeight("200px");
        summaryField.setValue(note.getSummary() != null ? note.getSummary() : "");

        TextArea keyPointsField = new TextArea("Key Points");
        keyPointsField.setWidthFull();
        keyPointsField.setHeight("150px");
        keyPointsField.setValue(note.getKeyPoints() != null ? note.getKeyPoints() : "");

        dialog.add(new VerticalLayout(summaryField, keyPointsField));

        Button saveButton = new Button("Save", e -> {
            note.setSummary(summaryField.getValue());
            note.setKeyPoints(keyPointsField.getValue());
            courseService.saveNote(course.getId(), note);
            dialog.close();
            Notification.show("Notes saved!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void openPreferenceDialog(Course course) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Preferences: " + course.getTitle());

        CoursePreference pref = courseService.findPreferenceByCourseId(course.getId())
                .orElse(CoursePreference.builder()
                        .preferredDailyWorkloadMinutes(120)
                        .notificationsEnabled(true)
                        .priorityLevel(3)
                        .build());

        IntegerField workloadField = new IntegerField("Daily Workload Limit (minutes)");
        workloadField.setValue(pref.getPreferredDailyWorkloadMinutes());
        workloadField.setMin(15);
        workloadField.setMax(480);
        workloadField.setStepButtonsVisible(true);

        IntegerField priorityField = new IntegerField("Priority Level (1=High, 5=Low)");
        priorityField.setValue(pref.getPriorityLevel());
        priorityField.setMin(1);
        priorityField.setMax(5);
        priorityField.setStepButtonsVisible(true);

        FormLayout form = new FormLayout(workloadField, priorityField);
        dialog.add(form);

        Button saveButton = new Button("Save", e -> {
            pref.setPreferredDailyWorkloadMinutes(workloadField.getValue());
            pref.setPriorityLevel(priorityField.getValue());
            courseService.savePreference(course.getId(), pref);
            dialog.close();
            Notification.show("Preferences saved!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void updateGrid() {
        grid.setItems(courseService.findAll());
    }
}

