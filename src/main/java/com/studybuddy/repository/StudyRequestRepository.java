package com.studybuddy.repository;

import com.studybuddy.model.StudyRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public class StudyRequestRepository {

    private final JdbcTemplate jdbc;

    public StudyRequestRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<StudyRequest> rowMapper = (rs, rowNum) -> {
        StudyRequest r = new StudyRequest();
        r.setRequestId(rs.getInt("request_id"));
        r.setRequesterId(rs.getInt("requester_id"));
        r.setReceiverId(rs.getInt("receiver_id"));
        r.setSubjectName(rs.getString("subject_name"));
        Timestamp ts = rs.getTimestamp("request_time");
        if (ts != null) r.setRequestTime(ts.toLocalDateTime());
        r.setStatus(rs.getString("status"));
        // joined columns (may be null if not joined)
        try { r.setRequesterName(rs.getString("requester_name")); } catch (Exception ignored) {}
        try { r.setReceiverName(rs.getString("receiver_name")); } catch (Exception ignored) {}
        return r;
    };

    private static final String SELECT_WITH_NAMES =
            "SELECT sr.*, " +
            "req.student_name AS requester_name, " +
            "rec.student_name AS receiver_name " +
            "FROM study_requests sr " +
            "JOIN students req ON sr.requester_id = req.student_id " +
            "JOIN students rec ON sr.receiver_id  = rec.student_id ";

    public Optional<StudyRequest> findById(int id) {
        List<StudyRequest> results = jdbc.query(
                SELECT_WITH_NAMES + "WHERE sr.request_id = ?", rowMapper, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    // Requests received by a student
    public List<StudyRequest> findIncoming(int receiverId) {
        return jdbc.query(
                SELECT_WITH_NAMES + "WHERE sr.receiver_id = ? ORDER BY sr.request_time DESC",
                rowMapper, receiverId);
    }

    // Requests sent by a student
    public List<StudyRequest> findSent(int requesterId) {
        return jdbc.query(
                SELECT_WITH_NAMES + "WHERE sr.requester_id = ? ORDER BY sr.request_time DESC",
                rowMapper, requesterId);
    }

    // Accepted partnerships involving a student (either side)
    public List<StudyRequest> findAcceptedPartnerships(int studentId) {
        return jdbc.query(
                SELECT_WITH_NAMES +
                "WHERE (sr.requester_id = ? OR sr.receiver_id = ?) AND sr.status = 'ACCEPTED' " +
                "ORDER BY sr.request_time DESC",
                rowMapper, studentId, studentId);
    }

    // Check for an existing active (PENDING or ACCEPTED) request between two students for a subject (both directions)
    public boolean activeRequestExists(int studentA, int studentB, String subject) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM study_requests " +
                "WHERE LOWER(subject_name) = LOWER(?) " +
                "AND status IN ('PENDING','ACCEPTED') " +
                "AND ((requester_id = ? AND receiver_id = ?) OR (requester_id = ? AND receiver_id = ?))",
                Integer.class, subject, studentA, studentB, studentB, studentA);
        return count != null && count > 0;
    }

    public int save(StudyRequest r) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO study_requests (requester_id, receiver_id, subject_name, request_time, status) VALUES (?,?,?,NOW(),'PENDING')",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, r.getRequesterId());
            ps.setInt(2, r.getReceiverId());
            ps.setString(3, r.getSubjectName());
            return ps;
        }, keyHolder);
        return keyHolder.getKey().intValue();
    }

    public boolean updateStatus(int requestId, String newStatus, String requiredCurrentStatus) {
        int rows = jdbc.update(
                "UPDATE study_requests SET status = ? WHERE request_id = ? AND status = ?",
                newStatus, requestId, requiredCurrentStatus);
        return rows > 0;
    }

    public int countPending() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM study_requests WHERE status = 'PENDING'", Integer.class);
        return count == null ? 0 : count;
    }

    public int countAccepted() {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM study_requests WHERE status = 'ACCEPTED'", Integer.class);
        return count == null ? 0 : count;
    }
}
