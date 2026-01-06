# StudyBuddy

## How to Run

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Development Mode
```bash
./run.sh
```

Or manually:
```bash
mvn spring-boot:run
```

### Production Mode
```bash
# Build the executable JAR
mvn clean package

# Run the application
java -jar target/studybuddy-1.0.0.jar
```

### Access the Application
Open your browser at `http://localhost:8080/dashboard`

### Troubleshooting
If port 8080 is already in use, kill the existing process:

**Windows:**
```powershell
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**macOS/Linux:**
```bash
lsof -ti:8080 | xargs kill -9
```
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
