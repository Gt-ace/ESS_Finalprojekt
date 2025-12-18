package ch.unisg.studybuddy.service;

import ch.unisg.studybuddy.model.StudentProfile;
import ch.unisg.studybuddy.persistence.StudentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class StudentProfileServiceImpl implements StudentProfileService {

    private final StudentProfileRepository studentProfileRepository;

    @Override
    public List<StudentProfile> findAll() {
        return studentProfileRepository.findAll();
    }

    @Override
    public Optional<StudentProfile> findById(Long id) {
        return studentProfileRepository.findById(id);
    }

    @Override
    public Optional<StudentProfile> findByEmail(String email) {
        return studentProfileRepository.findByEmail(email);
    }

    @Override
    public StudentProfile save(StudentProfile studentProfile) {
        return studentProfileRepository.save(studentProfile);
    }

    @Override
    public void deleteById(Long id) {
        studentProfileRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return studentProfileRepository.existsByEmail(email);
    }
}

