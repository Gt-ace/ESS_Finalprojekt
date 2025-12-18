package ch.unisg.studybuddy.service;

import ch.unisg.studybuddy.model.Course;
import ch.unisg.studybuddy.model.CourseNote;
import ch.unisg.studybuddy.model.CoursePreference;
import ch.unisg.studybuddy.model.StudentProfile;
import ch.unisg.studybuddy.persistence.CourseNoteRepository;
import ch.unisg.studybuddy.persistence.CoursePreferenceRepository;
import ch.unisg.studybuddy.persistence.CourseRepository;
import ch.unisg.studybuddy.persistence.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final CoursePreferenceRepository coursePreferenceRepository;
    private final CourseNoteRepository courseNoteRepository;

    @Override
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    @Override
    public Optional<Course> findById(Long id) {
        return courseRepository.findById(id);
    }

    @Override
    public List<Course> findByStudentProfileId(Long studentProfileId) {
        return courseRepository.findByStudentProfileId(studentProfileId);
    }

    @Override
    public Course save(Course course) {
        return courseRepository.save(course);
    }

    @Override
    public Course createCourse(Long studentProfileId, Course course) {
        StudentProfile student = studentProfileRepository.findById(studentProfileId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + studentProfileId));
        
        course.setStudentProfile(student);
        
        // Create default preference if not set
        if (course.getCoursePreference() == null) {
            CoursePreference defaultPreference = CoursePreference.builder()
                    .preferredDailyWorkloadMinutes(120)
                    .notificationsEnabled(true)
                    .priorityLevel(3)
                    .build();
            course.setCoursePreference(defaultPreference);
        }
        
        return courseRepository.save(course);
    }

    @Override
    public void deleteById(Long id) {
        courseRepository.deleteById(id);
    }

    @Override
    public Optional<CoursePreference> findPreferenceByCourseId(Long courseId) {
        return coursePreferenceRepository.findByCourseId(courseId);
    }

    @Override
    public CoursePreference savePreference(Long courseId, CoursePreference preference) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));
        
        preference.setCourse(course);
        return coursePreferenceRepository.save(preference);
    }

    @Override
    public Optional<CourseNote> findNoteByCourseId(Long courseId) {
        return courseNoteRepository.findByCourseId(courseId);
    }

    @Override
    public CourseNote saveNote(Long courseId, CourseNote note) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + courseId));
        
        note.setCourse(course);
        return courseNoteRepository.save(note);
    }
}

