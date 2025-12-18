package ch.unisg.studybuddy.ui;

import ch.unisg.studybuddy.model.StudentProfile;
import ch.unisg.studybuddy.service.StudentProfileService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profile | StudyBuddy")
public class ProfileView extends VerticalLayout {

    private final StudentProfileService studentProfileService;
    
    private StudentProfile currentStudent;
    private TextField nameField;
    private EmailField emailField;
    private ComboBox<String> localeField;

    @Autowired
    public ProfileView(StudentProfileService studentProfileService) {
        this.studentProfileService = studentProfileService;

        addClassName("profile-view");
        setPadding(true);
        setSpacing(true);
        setMaxWidth("600px");

        loadOrCreateStudent();
        
        add(createHeader());
        add(createProfileCard());
        add(createInfoSection());
    }

    private void loadOrCreateStudent() {
        List<StudentProfile> students = studentProfileService.findAll();
        if (students.isEmpty()) {
            currentStudent = StudentProfile.builder()
                    .name("Student")
                    .email("student@unisg.ch")
                    .locale("en")
                    .build();
            currentStudent = studentProfileService.save(currentStudent);
        } else {
            currentStudent = students.get(0);
        }
    }

    private VerticalLayout createHeader() {
        H2 title = new H2("👤 My Profile");
        title.addClassNames(LumoUtility.Margin.NONE);
        
        Paragraph subtitle = new Paragraph("Manage your account settings");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY);

        VerticalLayout header = new VerticalLayout(title, subtitle);
        header.setSpacing(false);
        header.setPadding(false);
        return header;
    }

    private VerticalLayout createProfileCard() {
        VerticalLayout card = new VerticalLayout();
        card.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("padding", "var(--lumo-space-l)");

        H3 sectionTitle = new H3("Personal Information");
        sectionTitle.getStyle().set("margin-top", "0");

        nameField = new TextField("Full Name");
        nameField.setWidthFull();
        nameField.setValue(currentStudent.getName() != null ? currentStudent.getName() : "");
        nameField.setRequired(true);

        emailField = new EmailField("Email Address");
        emailField.setWidthFull();
        emailField.setValue(currentStudent.getEmail() != null ? currentStudent.getEmail() : "");
        emailField.setRequired(true);

        localeField = new ComboBox<>("Language");
        localeField.setItems("en", "de", "fr", "it");
        localeField.setItemLabelGenerator(locale -> {
            switch (locale) {
                case "en": return "English";
                case "de": return "Deutsch";
                case "fr": return "Français";
                case "it": return "Italiano";
                default: return locale;
            }
        });
        localeField.setValue(currentStudent.getLocale() != null ? currentStudent.getLocale() : "en");
        localeField.setWidthFull();

        Button saveButton = new Button("Save Changes", e -> saveProfile());
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.getStyle().set("margin-top", "var(--lumo-space-m)");

        card.add(sectionTitle, nameField, emailField, localeField, saveButton);
        return card;
    }

    private VerticalLayout createInfoSection() {
        VerticalLayout section = new VerticalLayout();
        section.getStyle()
                .set("background", "var(--lumo-base-color)")
                .set("border-radius", "var(--lumo-border-radius-l)")
                .set("box-shadow", "var(--lumo-box-shadow-s)")
                .set("padding", "var(--lumo-space-l)");

        H3 title = new H3("ℹ️ About StudyBuddy");
        title.getStyle().set("margin-top", "0");

        Paragraph description = new Paragraph(
                "StudyBuddy is a lightweight study planner designed to help students organize " +
                "their courses, plan study sessions, manage tasks, and avoid scheduling conflicts. " +
                "Built with Spring Boot and Vaadin for the Design of Software Systems course at " +
                "University of St.Gallen."
        );
        description.addClassNames(LumoUtility.TextColor.SECONDARY);

        Paragraph version = new Paragraph("Version 1.0.0 | © 2025 Arthur Van Petegem & Jamie Maier");
        version.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);

        section.add(title, description, version);
        return section;
    }

    private void saveProfile() {
        if (nameField.isEmpty() || emailField.isEmpty()) {
            Notification.show("Name and Email are required", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        // Check if email is already used by another user
        String newEmail = emailField.getValue();
        if (!newEmail.equals(currentStudent.getEmail()) && studentProfileService.existsByEmail(newEmail)) {
            Notification.show("Email is already in use", 3000, Notification.Position.BOTTOM_START)
                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
            return;
        }

        currentStudent.setName(nameField.getValue());
        currentStudent.setEmail(emailField.getValue());
        currentStudent.setLocale(localeField.getValue());
        
        currentStudent = studentProfileService.save(currentStudent);
        
        Notification.show("Profile updated successfully!", 3000, Notification.Position.BOTTOM_START)
                .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}

