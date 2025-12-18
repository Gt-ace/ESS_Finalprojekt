package ch.unisg.studybuddy.service;

import ch.unisg.studybuddy.model.StudentProfile;

import java.util.List;
import java.util.Optional;

public interface StudentProfileService {
    
    List<StudentProfile> findAll();
    
    Optional<StudentProfile> findById(Long id);
    
    Optional<StudentProfile> findByEmail(String email);
    
    StudentProfile save(StudentProfile studentProfile);
    
    void deleteById(Long id);
    
    boolean existsByEmail(String email);
}

