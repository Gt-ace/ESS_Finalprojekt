package ch.unisg.studybuddy.persistence;

import ch.unisg.studybuddy.model.CourseNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseNoteRepository extends JpaRepository<CourseNote, Long> {
    
    Optional<CourseNote> findByCourseId(Long courseId);
}

