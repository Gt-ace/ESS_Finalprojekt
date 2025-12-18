package ch.unisg.studybuddy.persistence;

import ch.unisg.studybuddy.model.CoursePreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CoursePreferenceRepository extends JpaRepository<CoursePreference, Long> {
    
    Optional<CoursePreference> findByCourseId(Long courseId);
}

