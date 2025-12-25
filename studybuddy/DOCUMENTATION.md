# StudyBuddy - Project Documentation

**Design of Software Systems - Assignment 6**  
**Authors:** Arthur Van Petegem & Jamie Maier  
**University of St.Gallen | Fall 2025**

---

## 1. Domain & Business Logic

### 1.1 Chosen Domain
**StudyBuddy** is a lightweight study planner designed for university students. The application helps students organize their academic workload by managing courses, scheduling study sessions, tracking tasks, and monitoring progress.

### 1.2 Implemented Business Logic

| # | Business Logic | Description |
|---|----------------|-------------|
| 1 | **Daily Load Check** | Sums study session durations per day and warns if a new session would exceed the preferred daily workload (defined per course in CoursePreference). |
| 2 | **Clash Detection** | Detects and flags overlapping study sessions to prevent scheduling conflicts. |
| 3 | **Progress Roll-up** | Aggregates completed tasks per course and computes a completion percentage. |
| 4 | **Task Prioritization** | Calculates a priority score based on due date proximity and estimated effort: `score = (100 - daysUntilDue) + (effort × 2)`. Tasks are ordered by this score. |

---

## 2. Domain Model

### 2.1 Entity Classes (6 Entities)

| Entity | Description | Key Fields |
|--------|-------------|------------|
| `StudentProfile` | Represents a student user | id, name, email, locale, settings |
| `Course` | Academic course enrollment | id, title, term, instructor, description |
| `CoursePreference` | Study preferences per course | id, preferredDailyWorkloadMinutes, notificationsEnabled, priorityLevel |
| `CourseNote` | Notes and key points for a course | id, summary, keyPoints, lastUpdated |
| `StudySession` | Planned/completed study session | id, startTime, durationMinutes, location, completed |
| `Task` | Assignment or task for a course | id, title, taskType, dueDate, estimatedEffortHours, completed |

### 2.2 Entity Relationships

```
┌─────────────────┐
│ StudentProfile  │
└────────┬────────┘
         │ 1:N
         ▼
┌─────────────────┐       1:1      ┌──────────────────┐
│     Course      │◄──────────────►│ CoursePreference │
└────────┬────────┘                └──────────────────┘
         │                    1:1
         ├────────────────────────►┌──────────────────┐
         │                         │   CourseNote     │
         │ 1:N                     └──────────────────┘
         ├─────────────────────────►┌──────────────────┐
         │                          │  StudySession    │
         │ 1:N                      └──────────────────┘
         └─────────────────────────►┌──────────────────┐
                                    │      Task        │
                                    └──────────────────┘
```

**Associations:**
- **One-to-One:** Course ↔ CoursePreference, Course ↔ CourseNote
- **One-to-Many:** StudentProfile → Course, Course → StudySession, Course → Task

### 2.3 UML Class Diagram
*Generate using your IDE or online tool (e.g., IntelliJ → Diagrams → Show Diagram)*

---

## 3. Persistence Layer

### 3.1 Database Configuration
- **Database:** H2 In-Memory Database
- **ORM:** Spring Data JPA with Hibernate
- **DDL:** Auto-generated (`spring.jpa.hibernate.ddl-auto=create-drop`)

### 3.2 Database Tables

| Table Name | Entity | Primary Key | Foreign Keys |
|------------|--------|-------------|--------------|
| `student_profiles` | StudentProfile | id (auto) | - |
| `courses` | Course | id (auto) | student_profile_id |
| `course_preferences` | CoursePreference | id (auto) | course_id (unique) |
| `course_notes` | CourseNote | id (auto) | course_id (unique) |
| `study_sessions` | StudySession | id (auto) | course_id |
| `tasks` | Task | id (auto) | course_id |

### 3.3 Repository Interfaces
Each entity has a corresponding `JpaRepository` with custom query methods:
- `StudentProfileRepository` - findByEmail()
- `CourseRepository` - findByStudentProfileId(), findByTerm()
- `StudySessionRepository` - findByCourseIdAndDate(), findByStudentIdAndDate()
- `TaskRepository` - findByCourseIdAndCompleted(), countCompletedByCourseId()

---

## 4. REST API

### 4.1 Endpoints Overview

| Resource | Method | Endpoint | Description |
|----------|--------|----------|-------------|
| Students | GET | `/api/students` | Get all students |
| | POST | `/api/students` | Create student |
| | GET | `/api/students/{id}` | Get by ID |
| Courses | GET | `/api/courses` | Get all courses |
| | POST | `/api/courses/student/{studentId}` | Create course |
| | GET | `/api/courses/{id}/progress` | **BL3: Progress Roll-up** |
| Tasks | GET | `/api/tasks/prioritized` | **BL4: Task Prioritization** |
| | PATCH | `/api/tasks/{id}/complete` | Mark complete |
| Sessions | POST | `/api/sessions/check-load` | **BL1: Daily Load Check** |
| | POST | `/api/sessions/check-clash` | **BL2: Clash Detection** |

### 4.2 Business Logic Endpoints

**Daily Load Check (POST /api/sessions/check-load)**
```json
Request: { "courseId": 1, "date": "2025-01-15", "proposedDurationMinutes": 60 }
Response: { "exceedsLimit": true, "currentMinutes": 90, "dailyLimitMinutes": 120, "warningMessage": "..." }
```

**Task Prioritization (GET /api/tasks/prioritized?courseId=1)**
```json
Response: [{ "id": 1, "title": "Exam Prep", "priorityScore": 95.0 }, ...]
```

---

## 5. User Interface (Vaadin)

### 5.1 UI Components

| View | Description | Key Features |
|------|-------------|--------------|
| **Dashboard** | Overview page | Stats cards, course progress bars, priority tasks list |
| **Courses** | Course management | Grid with progress, edit/delete/notes/preferences dialogs |
| **Tasks** | Task management | Filterable grid, checkboxes, priority sorting, color-coded due dates |
| **Study Sessions** | Session scheduling | Calendar view, real-time clash/load warnings in dialog |
| **Profile** | User settings | Edit name, email, language preference |

### 5.2 Screenshots Required

**📸 SCREENSHOT 1: Dashboard**
- Navigate to: `http://localhost:8080/`
- Shows: Welcome message, stats cards (Courses, Pending Tasks, Completed), Course Progress section, Priority Tasks section

**📸 SCREENSHOT 2: Courses Page**
- Navigate to: `http://localhost:8080/courses`
- Shows: Course grid with titles, terms, instructors, progress bars, action buttons

**📸 SCREENSHOT 3: Tasks Page**
- Navigate to: `http://localhost:8080/tasks`
- Click "Sort by Priority" button first
- Shows: Task list with checkboxes, due dates (color-coded), priority badges, filter options

**📸 SCREENSHOT 4: Study Sessions Page**
- Navigate to: `http://localhost:8080/sessions`
- Shows: Session grid with dates, times, durations, locations, status

**📸 SCREENSHOT 5: Add Session Dialog (showing Business Logic)**
- Click "Schedule Session" button
- Select a course and pick a time that would overlap with an existing session
- Shows: Warning message for clash detection or daily load exceeded

---

## 6. Unit Tests

### 6.1 Test Coverage
**21 total tests** covering all 4 business logic methods:

| Test Class | Tests | Business Logic Covered |
|------------|-------|------------------------|
| `StudySessionServiceTest` | 9 | BL1: Daily Load Check, BL2: Clash Detection |
| `TaskServiceTest` | 11 | BL3: Progress Roll-up, BL4: Task Prioritization |
| `StudyBuddyApplicationTests` | 1 | Context loading |

### 6.2 Key Test Cases

**Daily Load Check Tests:**
- Session within limit returns OK
- Session exceeds limit returns warning
- Empty day accepts session
- Session exactly at limit is OK

**Clash Detection Tests:**
- Non-overlapping sessions return no clash
- Overlapping sessions detected
- Session completely inside another detected
- Adjacent sessions (end = start) no clash
- Multiple clashes detected

**Progress Roll-up Tests:**
- Empty course shows 0%
- All completed shows 100%
- Mixed completion shows correct %

**Task Prioritization Tests:**
- Overdue tasks highest priority
- Closer due date = higher priority
- Higher effort increases priority
- Completed tasks excluded

---

## 7. Example User Workflow

1. **User opens Dashboard** → Sees overview of 4 courses and 9 pending tasks
2. **Navigates to Tasks** → Clicks "Sort by Priority" to see most urgent tasks first
3. **Identifies "Problem Set 5"** is due in 2 days → Decides to schedule a study session
4. **Goes to Study Sessions** → Clicks "Schedule Session"
5. **Selects "Mathematics for Business"** course, picks today at 14:00 for 90 minutes
6. **⚠️ Warning appears:** "Adding this session would exceed your daily limit by 30 minutes"
7. **User reduces duration** to 60 minutes → Warning disappears
8. **Saves session** → Returns to Dashboard to see updated stats
9. **Completes the task** by checking the checkbox → Progress bar updates to 50%

---

## 8. Difficulties Encountered

| Challenge | Solution |
|-----------|----------|
| **LazyInitializationException** in Vaadin views | Changed `FetchType.LAZY` to `FetchType.EAGER` for entity relationships accessed in UI |
| **Lombok not processing** with Java 22 | Used Java 17 (Amazon Corretto) with explicit annotation processor configuration in pom.xml |
| **Vaadin development mode notifications** | These are normal in dev mode; would be disabled in production build |

---

## 9. Work Distribution

| Team Member | Responsibilities |
|-------------|------------------|
| **Arthur Van Petegem** | Entity model design, Service layer, Business logic implementation, Unit tests |
| **Jamie Maier** | Vaadin UI development, REST controllers, Documentation, Testing |

*Both team members participated in code review and integration testing.*

---

## 10. How to Run

```bash
# Ensure Java 17 is active
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Run the application
cd studybuddy
mvn spring-boot:run

# Access at http://localhost:8080
# Run tests: mvn test
```

---

**Submitted:** December 2025  
**Repository:** Self-contained Maven project with all dependencies

