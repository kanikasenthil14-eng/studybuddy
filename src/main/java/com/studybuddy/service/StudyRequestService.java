package com.studybuddy.service;

import com.studybuddy.model.StudyRequest;
import com.studybuddy.repository.PreferenceRepository;
import com.studybuddy.repository.StudentRepository;
import com.studybuddy.repository.StudyRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudyRequestService {

    private final StudyRequestRepository requestRepo;
    private final StudentRepository studentRepo;
    private final PreferenceRepository prefRepo;

    public StudyRequestService(StudyRequestRepository requestRepo,
                               StudentRepository studentRepo,
                               PreferenceRepository prefRepo) {
        this.requestRepo = requestRepo;
        this.studentRepo = studentRepo;
        this.prefRepo = prefRepo;
    }

    @Transactional
    public int sendRequest(int requesterId, int receiverId, String subject) {
        if (requesterId == receiverId) {
            throw new IllegalArgumentException("You cannot send a study request to yourself.");
        }
        studentRepo.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("Requester not found."));
        studentRepo.findById(receiverId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver not found."));

        // Both students must have a preference for this subject
        boolean requesterHasSubject = prefRepo.findByStudentId(requesterId).stream()
                .anyMatch(p -> p.getSubjectName().equalsIgnoreCase(subject));
        boolean receiverHasSubject = prefRepo.findByStudentId(receiverId).stream()
                .anyMatch(p -> p.getSubjectName().equalsIgnoreCase(subject));

        if (!requesterHasSubject || !receiverHasSubject) {
            throw new IllegalArgumentException("Both students must have a preference for the selected subject.");
        }

        // Prevent duplicate active requests (both directions)
        if (requestRepo.activeRequestExists(requesterId, receiverId, subject)) {
            throw new IllegalArgumentException("An active request already exists between these students for this subject.");
        }

        StudyRequest req = new StudyRequest();
        req.setRequesterId(requesterId);
        req.setReceiverId(receiverId);
        req.setSubjectName(subject);
        return requestRepo.save(req);
    }

    public List<StudyRequest> getIncomingRequests(int studentId) {
        studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        return requestRepo.findIncoming(studentId);
    }

    public List<StudyRequest> getSentRequests(int studentId) {
        studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        return requestRepo.findSent(studentId);
    }

    public List<StudyRequest> getPartnerships(int studentId) {
        studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        return requestRepo.findAcceptedPartnerships(studentId);
    }

    /** Only the receiver may accept a PENDING request. */
    @Transactional
    public void acceptRequest(int requestId, int actingStudentId) {
        StudyRequest req = getRequestOrThrow(requestId);
        if (req.getReceiverId() != actingStudentId) {
            throw new IllegalArgumentException("Only the receiver can accept this request.");
        }
        boolean updated = requestRepo.updateStatus(requestId, "ACCEPTED", "PENDING");
        if (!updated) {
            throw new IllegalStateException("Request is no longer pending and cannot be accepted.");
        }
    }

    /** Only the receiver may reject a PENDING request. */
    @Transactional
    public void rejectRequest(int requestId, int actingStudentId) {
        StudyRequest req = getRequestOrThrow(requestId);
        if (req.getReceiverId() != actingStudentId) {
            throw new IllegalArgumentException("Only the receiver can reject this request.");
        }
        boolean updated = requestRepo.updateStatus(requestId, "REJECTED", "PENDING");
        if (!updated) {
            throw new IllegalStateException("Request is no longer pending and cannot be rejected.");
        }
    }

    /** Only the requester may cancel a PENDING request. */
    @Transactional
    public void cancelRequest(int requestId, int actingStudentId) {
        StudyRequest req = getRequestOrThrow(requestId);
        if (req.getRequesterId() != actingStudentId) {
            throw new IllegalArgumentException("Only the requester can cancel this request.");
        }
        boolean updated = requestRepo.updateStatus(requestId, "CANCELLED", "PENDING");
        if (!updated) {
            throw new IllegalStateException("Only pending requests can be cancelled.");
        }
    }

    /** Either participant may mark an ACCEPTED partnership as COMPLETED. */
    @Transactional
    public void completePartnership(int requestId, int actingStudentId) {
        StudyRequest req = getRequestOrThrow(requestId);
        if (req.getRequesterId() != actingStudentId && req.getReceiverId() != actingStudentId) {
            throw new IllegalArgumentException("You are not a participant in this partnership.");
        }
        boolean updated = requestRepo.updateStatus(requestId, "COMPLETED", "ACCEPTED");
        if (!updated) {
            throw new IllegalStateException("Only accepted partnerships can be marked as completed.");
        }
    }

    private StudyRequest getRequestOrThrow(int requestId) {
        return requestRepo.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found: " + requestId));
    }
}
