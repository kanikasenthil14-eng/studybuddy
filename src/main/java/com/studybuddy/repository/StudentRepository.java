package com.studybuddy.repository;

import com.studybuddy.model.Student;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class StudentRepository {

    private final JdbcTemplate jdbc;

    public StudentRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Student> rowMapper = (rs, rowNum) -> new Student(
            rs.getInt("student_id"),
            rs.getString("student_name"),
            rs.getString("email"),
            rs.getString("contact_info"),
            rs.getString("department"),
            rs.getInt("year_of_study")
    );

    public List<Student> findAll() {
        return jdbc.query("SELECT * FROM students ORDER BY student_name", rowMapper);
    }

    public Optional<Student> findById(int id) {
        List<Student> results = jdbc.query(
                "SELECT * FROM students WHERE student_id = ?", rowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public boolean existsByEmail(String email) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM students WHERE LOWER(email) = LOWER(?)", Integer.class, email);
        return count != null && count > 0;
    }

    public boolean existsByEmailAndNotId(String email, int excludeId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM students WHERE LOWER(email) = LOWER(?) AND student_id <> ?",
                Integer.class, email, excludeId);
        return count != null && count > 0;
    }

    public int save(Student s) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO students (student_name, email, contact_info, department, year_of_study) VALUES (?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, s.getStudentName());
            ps.setString(2, s.getEmail());
            ps.setString(3, s.getContactInfo());
            ps.setString(4, s.getDepartment());
            ps.setInt(5, s.getYearOfStudy());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void update(Student s) {
        jdbc.update(
                "UPDATE students SET student_name=?, email=?, contact_info=?, department=?, year_of_study=? WHERE student_id=?",
                s.getStudentName(), s.getEmail(), s.getContactInfo(),
                s.getDepartment(), s.getYearOfStudy(), s.getStudentId());
    }

    public int countAll() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM students", Integer.class);
        return count == null ? 0 : count;
    }
}
