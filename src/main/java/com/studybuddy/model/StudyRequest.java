package com.studybuddy.model;

import java.time.LocalDateTime;

public class StudyRequest {

    private int requestId;
    private int requesterId;
    private int receiverId;
    private String subjectName;
    private LocalDateTime requestTime;
    private String status; // PENDING | ACCEPTED | REJECTED | COMPLETED | CANCELLED

    // Joined fields for display (not stored in this table)
    private String requesterName;
    private String receiverName;

    public StudyRequest() {}

    public int getRequestId()                       { return requestId; }
    public void setRequestId(int id)                { this.requestId = id; }

    public int getRequesterId()                     { return requesterId; }
    public void setRequesterId(int id)              { this.requesterId = id; }

    public int getReceiverId()                      { return receiverId; }
    public void setReceiverId(int id)               { this.receiverId = id; }

    public String getSubjectName()                  { return subjectName; }
    public void setSubjectName(String s)            { this.subjectName = s; }

    public LocalDateTime getRequestTime()           { return requestTime; }
    public void setRequestTime(LocalDateTime t)     { this.requestTime = t; }

    public String getStatus()                       { return status; }
    public void setStatus(String s)                 { this.status = s; }

    public String getRequesterName()                { return requesterName; }
    public void setRequesterName(String n)          { this.requesterName = n; }

    public String getReceiverName()                 { return receiverName; }
    public void setReceiverName(String n)           { this.receiverName = n; }
}
