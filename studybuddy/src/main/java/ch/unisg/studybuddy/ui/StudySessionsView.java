package ch.unisg.studybuddy.ui;

import ch.unisg.studybuddy.model.Course;
import ch.unisg.studybuddy.model.StudySession;
import ch.unisg.studybuddy.service.CourseService;
import ch.unisg.studybuddy.service.StudySessionService;
import ch.unisg.studybuddy.service.dto.ClashCheckResult;
import ch.unisg.studybuddy.service.dto.LoadCheckResult;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "sessions", layout = MainLayout.class)
@PageTitle("Study Sessions | StudyBuddy")
public class StudySessionsView extends VerticalLayout {

    private final StudySessionService studySessionService;
    private final CourseService courseService;

    private Grid<StudySession> grid = new Grid<>(StudySession.class, false);
    private ComboBox<Course> courseFilter;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    @Autowired
    public StudySessionsView(StudySessionService studySessionService, CourseService courseService) {
        this.studySessionService = studySessionService;
        this.courseService = courseService;

        addClassName("sessions-view");
        setSizeFull();
        setPadding(true);

        add(createHeader());
        add(createFilters());
        add(createGrid());

        updateGrid();
    }

    private HorizontalLayout createHeader() {
        H2 title = new H2("📅 Study Sessions");
        title.addClassNames(LumoUtility.Margin.NONE);

        Button addButton = new Button("Schedule Session", VaadinIcon.PLUS.create());
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(e -> openSessionDialog(null));

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

        HorizontalLayout filters = new HorizontalLayout(courseFilter);
        filters.setAlignItems(FlexComponent.Alignment.END);

        return filters;
    }

    private Grid<StudySession> createGrid() {
        grid.addColumn(session -> session.getCourse() != null ? session.getCourse().getTitle() : "")
                .setHeader("Course").setFlexGrow(1);

        grid.addColumn(session -> session.getStartTime() != null ? 
                session.getStartTime().format(DATE_FORMAT) : "")
                .setHeader("Date").setFlexGrow(1);

        grid.addColumn(session -> {
            if (session.getStartTime() == null) return "";
            String start = session.getStartTime().format(TIME_FORMAT);
            String end = session.getEndTime().format(TIME_FORMAT);
            return start + " - " + end;
        }).setHeader("Time").setFlexGrow(1);

        grid.addColumn(session -> session.getDurationMinutes() + " min")
                .setHeader("Duration").setWidth("100px").setFlexGrow(0);

        grid.addColumn(StudySession::getLocation).setHeader("Location").setFlexGrow(1);

        grid.addComponentColumn(session -> {
            Checkbox checkbox = new Checkbox();
            checkbox.setValue(session.getCompleted());
            checkbox.addValueChangeListener(e -> {
                session.setCompleted(e.getValue());
                studySessionService.save(session);
                Notification.show(e.getValue() ? "Session marked complete!" : "Session marked incomplete",
                        2000, Notification.Position.BOTTOM_START);
            });
            
            Span label = new Span(session.getCompleted() ? "Completed" : "Pending");
            label.addClassNames(LumoUtility.FontSize.SMALL);
            if (session.getCompleted()) {
                label.getStyle().set("color", "#16a34a");
            }
            
            HorizontalLayout status = new HorizontalLayout(checkbox, label);
            status.setAlignItems(FlexComponent.Alignment.CENTER);
            return status;
        }).setHeader("Status").setFlexGrow(1);

        grid.addComponentColumn(session -> {
            Button editBtn = new Button(new Icon(VaadinIcon.EDIT));
            editBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL);
            editBtn.addClickListener(e -> openSessionDialog(session));

            Button deleteBtn = new Button(new Icon(VaadinIcon.TRASH));
            deleteBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteBtn.addClickListener(e -> {
                studySessionService.deleteById(session.getId());
                updateGrid();
                Notification.show("Session deleted", 3000, Notification.Position.BOTTOM_START)
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

    private void openSessionDialog(StudySession session) {
        List<Course> courses = courseService.findAll();
        if (courses.isEmpty()) {
            Notification.show("Please create a course first!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_WARNING);
            return;
        }

        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(session == null ? "Schedule New Session" : "Edit Session");
        dialog.setWidth("500px");

        ComboBox<Course> courseField = new ComboBox<>("Course");
        courseField.setItems(courses);
        courseField.setItemLabelGenerator(Course::getTitle);
        courseField.setRequired(true);
        courseField.setWidthFull();

        DateTimePicker startTimeField = new DateTimePicker("Start Time");
        startTimeField.setStep(Duration.ofMinutes(15));
        startTimeField.setWidthFull();

        IntegerField durationField = new IntegerField("Duration (minutes)");
        durationField.setMin(15);
        durationField.setMax(480);
        durationField.setValue(60);
        durationField.setStepButtonsVisible(true);
        durationField.setStep(15);
        durationField.setWidthFull();

        TextField locationField = new TextField("Location");
        locationField.setPlaceholder("e.g., Library Room 101");
        locationField.setWidthFull();

        TextArea notesField = new TextArea("Notes");
        notesField.setWidthFull();

        // Warning area for business logic checks
        Span warningArea = new Span();
        warningArea.getStyle()
                .set("padding", "var(--lumo-space-s)")
                .set("border-radius", "var(--lumo-border-radius-s)")
                .set("display", "none");

        // Add change listeners for real-time validation
        Runnable validateSession = () -> {
            if (courseField.getValue() != null && startTimeField.getValue() != null && durationField.getValue() != null) {
                Course course = courseField.getValue();
                LocalDateTime startTime = startTimeField.getValue();
                int duration = durationField.getValue();

                // Check daily load
                LoadCheckResult loadResult = studySessionService.checkDailyLoad(
                        course.getId(), startTime.toLocalDate(), duration);

                // Check for clashes
                StudySession proposedSession = StudySession.builder()
                        .id(session != null ? session.getId() : null)
                        .startTime(startTime)
                        .durationMinutes(duration)
                        .build();
                ClashCheckResult clashResult = studySessionService.checkForClashes(course.getId(), proposedSession);

                StringBuilder warnings = new StringBuilder();
                if (loadResult.isExceedsLimit()) {
                    warnings.append("⚠️ ").append(loadResult.getWarningMessage()).append("\n");
                }
                if (clashResult.isHasClash()) {
                    warnings.append("⚠️ ").append(clashResult.getWarningMessage());
                }

                if (warnings.length() > 0) {
                    warningArea.setText(warnings.toString());
                    warningArea.getStyle()
                            .set("display", "block")
                            .set("background", "#fef3c7")
                            .set("color", "#92400e");
                } else {
                    warningArea.getStyle().set("display", "none");
                }
            }
        };

        courseField.addValueChangeListener(e -> validateSession.run());
        startTimeField.addValueChangeListener(e -> validateSession.run());
        durationField.addValueChangeListener(e -> validateSession.run());

        if (session != null) {
            courseField.setValue(session.getCourse());
            startTimeField.setValue(session.getStartTime());
            durationField.setValue(session.getDurationMinutes());
            locationField.setValue(session.getLocation() != null ? session.getLocation() : "");
            notesField.setValue(session.getNotes() != null ? session.getNotes() : "");
        } else {
            startTimeField.setValue(LocalDateTime.now().plusHours(1).withMinute(0));
        }

        VerticalLayout form = new VerticalLayout(courseField, startTimeField, durationField, 
                locationField, notesField, warningArea);
        form.setSpacing(true);
        form.setPadding(false);
        dialog.add(form);

        Button saveButton = new Button("Save", e -> {
            if (courseField.isEmpty() || startTimeField.isEmpty()) {
                Notification.show("Course and Start Time are required", 3000, Notification.Position.BOTTOM_START)
                        .addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            StudySession toSave = session != null ? session : new StudySession();
            toSave.setStartTime(startTimeField.getValue());
            toSave.setDurationMinutes(durationField.getValue());
            toSave.setLocation(locationField.getValue());
            toSave.setNotes(notesField.getValue());

            if (session == null) {
                studySessionService.createSession(courseField.getValue().getId(), toSave);
            } else {
                studySessionService.save(toSave);
            }

            dialog.close();
            updateGrid();
            Notification.show("Session saved!", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        Button cancelButton = new Button("Cancel", e -> dialog.close());
        dialog.getFooter().add(cancelButton, saveButton);
        dialog.open();
    }

    private void updateGrid() {
        List<StudySession> sessions;
        
        if (courseFilter.getValue() != null) {
            sessions = studySessionService.findByCourseId(courseFilter.getValue().getId());
        } else {
            sessions = studySessionService.findAll();
        }
        
        // Sort by start time (most recent first)
        sessions.sort((a, b) -> {
            if (a.getStartTime() == null) return 1;
            if (b.getStartTime() == null) return -1;
            return b.getStartTime().compareTo(a.getStartTime());
        });
        
        grid.setItems(sessions);
    }
}

