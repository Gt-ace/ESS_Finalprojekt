# StudyBuddy

A lightweight study planner application built with Spring Boot and Vaadin for the Design of Software Systems course at University of St.Gallen.

## Features

- **Course Management**: Create, update, and delete courses with preferences and notes
- **Task Tracking**: Manage tasks with priority scoring based on due date and effort
- **Study Sessions**: Schedule and track study sessions with clash detection
- **Progress Tracking**: Visual progress bars and completion percentages
- **Professional UI**: Modern Vaadin-based web interface

## Business Logic

1. **Daily Load Check**: Warns when study sessions exceed preferred daily workload
2. **Clash Detection**: Detects overlapping study sessions
3. **Progress Roll-up**: Calculates completion percentage per course
4. **Task Prioritization**: Orders tasks by priority score (due date + effort)

## Tech Stack

- Java 17
- Spring Boot 3.2.1
- Vaadin 24.3.3
- Spring Data JPA
- H2 In-Memory Database
- Lombok
- JUnit 5

## Running the Application

### Prerequisites
- Java 17 installed
- Maven 3.6+

### Quick Start

```bash
# On macOS with multiple Java versions:
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Or use the provided script:
./run.sh

# Or manually:
mvn spring-boot:run
```

### Access the Application

- **Web UI**: http://localhost:8080
- **H2 Console**: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:studybuddy`)

## REST API Endpoints

### Students
- `GET /api/students` - Get all students
- `GET /api/students/{id}` - Get student by ID
- `POST /api/students` - Create student
- `PUT /api/students/{id}` - Update student
- `DELETE /api/students/{id}` - Delete student

### Courses
- `GET /api/courses` - Get all courses
- `GET /api/courses/{id}` - Get course by ID
- `GET /api/courses/{id}/progress` - Get course progress (Business Logic 3)
- `POST /api/courses/student/{studentId}` - Create course
- `PUT /api/courses/{id}` - Update course
- `DELETE /api/courses/{id}` - Delete course
- `GET/PUT /api/courses/{id}/preference` - Course preferences
- `GET/PUT /api/courses/{id}/note` - Course notes

### Tasks
- `GET /api/tasks` - Get all tasks
- `GET /api/tasks/prioritized` - Get tasks ordered by priority (Business Logic 4)
- `POST /api/tasks/course/{courseId}` - Create task
- `PATCH /api/tasks/{id}/complete` - Mark task complete
- `DELETE /api/tasks/{id}` - Delete task

### Study Sessions
- `GET /api/sessions` - Get all sessions
- `POST /api/sessions/course/{courseId}` - Create session
- `POST /api/sessions/check-load` - Check daily workload (Business Logic 1)
- `POST /api/sessions/check-clash` - Check for clashes (Business Logic 2)

## Running Tests

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn test
```

## Project Structure

```
src/main/java/ch/unisg/studybuddy/
├── StudyBuddyApplication.java
├── DataInitializer.java
├── controller/           # REST Controllers
├── model/               # Entity Classes (6 entities)
├── persistence/         # Repository Interfaces
├── service/             # Business Logic Services
│   └── dto/            # Data Transfer Objects
└── ui/                  # Vaadin Views
```

## Authors

Arthur Van Petegem & Jamie Maier - University of St.Gallen

## License

This project is for educational purposes as part of the Design of Software Systems course.

