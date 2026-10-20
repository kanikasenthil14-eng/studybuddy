package com.studybuddy.repository;

import com.studybuddy.model.StudyPreference;
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
public class PreferenceRepository {

    private final JdbcTemplate jdbc;

    public PreferenceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<StudyPreference> rowMapper = (rs, rowNum) -> new StudyPreference(
            rs.getInt("preference_id"),
            rs.getInt("student_id"),
            rs.getString("subject_name"),
            rs.getString("learning_goal"),
            rs.getString("skill_level"),
            rs.getString("preferred_time"),
            rs.getString("preferred_location")
    );

    public List<StudyPreference> findByStudentId(int studentId) {
        return jdbc.query(
                "SELECT * FROM study_preferences WHERE student_id = ? ORDER BY subject_name",
                rowMapper, studentId);
    }

    public Optional<StudyPreference> findById(int id) {
        List<StudyPreference> results = jdbc.query(
                "SELECT * FROM study_preferences WHERE preference_id = ?", rowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public boolean existsByStudentAndSubject(int studentId, String subjectName) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM study_preferences WHERE student_id = ? AND LOWER(subject_name) = LOWER(?)",
                Integer.class, studentId, subjectName);
        return count != null && count > 0;
    }

    public boolean existsByStudentAndSubjectExcluding(int studentId, String subjectName, int excludeId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM study_preferences WHERE student_id = ? AND LOWER(subject_name) = LOWER(?) AND preference_id <> ?",
                Integer.class, studentId, subjectName, excludeId);
        return count != null && count > 0;
    }

    public int save(StudyPreference p) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO study_preferences (student_id, subject_name, learning_goal, skill_level, preferred_time, preferred_location) VALUES (?,?,?,?,?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, p.getStudentId());
            ps.setString(2, p.getSubjectName());
            ps.setString(3, p.getLearningGoal());
            ps.setString(4, p.getSkillLevel());
            ps.setString(5, p.getPreferredTime());
            ps.setString(6, p.getPreferredLocation());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public void update(StudyPreference p) {
        jdbc.update(
                "UPDATE study_preferences SET subject_name=?, learning_goal=?, skill_level=?, preferred_time=?, preferred_location=? WHERE preference_id=?",
                p.getSubjectName(), p.getLearningGoal(), p.getSkillLevel(),
                p.getPreferredTime(), p.getPreferredLocation(), p.getPreferenceId());
    }

    public void delete(int id) {
        jdbc.update("DELETE FROM study_preferences WHERE preference_id = ?", id);
    }

    // Returns all preferences for students who share the given subject (excluding the given student)
    public List<StudyPreference> findBySubjectExcludingStudent(String subjectName, int excludeStudentId) {
        return jdbc.query(
                "SELECT * FROM study_preferences WHERE LOWER(subject_name) = LOWER(?) AND student_id <> ?",
                rowMapper, subjectName, excludeStudentId);
    }

    // Distinct subject names across all preferences
    public List<String> findAllSubjects() {
        return jdbc.queryForList(
                "SELECT DISTINCT subject_name FROM study_preferences ORDER BY subject_name",
                String.class);
    }

    public int countAll() {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM study_preferences", Integer.class);
        return count == null ? 0 : count;
    }
}
