package ch.unisg.studybuddy.service;

import ch.unisg.studybuddy.model.Course;
import ch.unisg.studybuddy.model.CourseNote;
import ch.unisg.studybuddy.model.CoursePreference;

import java.util.List;
import java.util.Optional;

public interface CourseService {
    
    List<Course> findAll();
    
    Optional<Course> findById(Long id);
    
    List<Course> findByStudentProfileId(Long studentProfileId);
    
    Course save(Course course);
    
    Course createCourse(Long studentProfileId, Course course);
    
    void deleteById(Long id);
    
    // CoursePreference operations
    Optional<CoursePreference> findPreferenceByCourseId(Long courseId);
    
    CoursePreference savePreference(Long courseId, CoursePreference preference);
    
    // CourseNote operations
    Optional<CourseNote> findNoteByCourseId(Long courseId);
    
    CourseNote saveNote(Long courseId, CourseNote note);
}

