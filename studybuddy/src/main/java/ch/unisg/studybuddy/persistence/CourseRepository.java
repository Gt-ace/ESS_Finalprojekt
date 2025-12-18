package ch.unisg.studybuddy.persistence;

import ch.unisg.studybuddy.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    
    List<Course> findByStudentProfileId(Long studentProfileId);
    
    List<Course> findByTerm(String term);
    
    @Query("SELECT c FROM Course c WHERE c.studentProfile.id = :studentId AND c.term = :term")
    List<Course> findByStudentAndTerm(@Param("studentId") Long studentId, @Param("term") String term);
}

