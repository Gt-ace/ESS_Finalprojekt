package ch.unisg.studybuddy.ui;

import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Nav;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;

/**
 * Main layout providing navigation for the StudyBuddy application.
 */
public class MainLayout extends AppLayout {

    public MainLayout() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("📚 StudyBuddy");
        logo.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.MEDIUM,
                LumoUtility.FontWeight.BOLD
        );
        logo.getStyle()
                .set("color", "var(--lumo-primary-color)")
                .set("margin", "0");

        Span tagline = new Span("Your Study Companion");
        tagline.addClassNames(
                LumoUtility.FontSize.SMALL,
                LumoUtility.TextColor.SECONDARY
        );

        VerticalLayout logoLayout = new VerticalLayout(logo, tagline);
        logoLayout.setSpacing(false);
        logoLayout.setPadding(false);
        logoLayout.setAlignItems(FlexComponent.Alignment.START);

        HorizontalLayout header = new HorizontalLayout(
                new DrawerToggle(),
                logoLayout
        );
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.NONE,
                LumoUtility.Padding.Horizontal.MEDIUM
        );

        addToNavbar(header);
    }

    private void createDrawer() {
        Nav nav = new Nav();
        nav.addClassNames(LumoUtility.Padding.MEDIUM);

        VerticalLayout navLinks = new VerticalLayout();
        navLinks.setSpacing(true);
        navLinks.setPadding(false);

        navLinks.add(
                createNavItem(VaadinIcon.DASHBOARD, "Dashboard", DashboardView.class),
                createNavItem(VaadinIcon.ACADEMY_CAP, "Courses", CoursesView.class),
                createNavItem(VaadinIcon.TASKS, "Tasks", TasksView.class),
                createNavItem(VaadinIcon.CALENDAR, "Study Sessions", StudySessionsView.class),
                createNavItem(VaadinIcon.USER, "Profile", ProfileView.class)
        );

        nav.add(navLinks);
        addToDrawer(nav);
    }

    private RouterLink createNavItem(VaadinIcon icon, String text, Class<?> navigationTarget) {
        Icon navIcon = icon.create();
        navIcon.getStyle().set("margin-right", "var(--lumo-space-s)");
        
        Span label = new Span(text);
        
        HorizontalLayout content = new HorizontalLayout(navIcon, label);
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(false);
        content.getStyle()
                .set("padding", "var(--lumo-space-s) var(--lumo-space-m)")
                .set("border-radius", "var(--lumo-border-radius-m)")
                .set("cursor", "pointer");

        RouterLink link = new RouterLink();
        link.setRoute((Class) navigationTarget);
        link.add(content);
        link.addClassNames(LumoUtility.TextColor.BODY);
        link.getStyle()
                .set("text-decoration", "none")
                .set("display", "block")
                .set("width", "100%");

        return link;
    }
}

