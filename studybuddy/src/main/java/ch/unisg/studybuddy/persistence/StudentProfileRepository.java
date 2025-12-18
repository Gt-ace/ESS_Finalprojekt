package ch.unisg.studybuddy.persistence;

import ch.unisg.studybuddy.model.StudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    
    Optional<StudentProfile> findByEmail(String email);
    
    boolean existsByEmail(String email);
}

