package com.studybuddy.service;

import com.studybuddy.model.Student;
import com.studybuddy.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepo;

    public StudentService(StudentRepository studentRepo) {
        this.studentRepo = studentRepo;
    }

    public List<Student> getAllStudents() {
        return studentRepo.findAll();
    }

    public Student getStudentById(int id) {
        return studentRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + id));
    }

    public Optional<Student> findById(int id) {
        return studentRepo.findById(id);
    }

    /** Saves a new student. Throws if email is already taken. */
    public int registerStudent(Student student) {
        if (studentRepo.existsByEmail(student.getEmail())) {
            throw new IllegalArgumentException("A student with this email already exists.");
        }
        return studentRepo.save(student);
    }

    /** Updates an existing student. Throws if email is taken by another student. */
    public void updateStudent(Student student) {
        studentRepo.findById(student.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + student.getStudentId()));
        if (studentRepo.existsByEmailAndNotId(student.getEmail(), student.getStudentId())) {
            throw new IllegalArgumentException("Another student is already using this email.");
        }
        studentRepo.update(student);
    }

    public int countStudents() {
        return studentRepo.countAll();
    }
}
