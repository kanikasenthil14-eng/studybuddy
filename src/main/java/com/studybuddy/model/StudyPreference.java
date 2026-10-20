package com.studybuddy.model;

import jakarta.validation.constraints.*;

public class StudyPreference {

    private int preferenceId;

    private int studentId;

    @NotBlank(message = "Subject name is required")
    @Size(max = 100)
    private String subjectName;

    @NotBlank(message = "Learning goal is required")
    @Size(max = 255)
    private String learningGoal;

    @NotBlank(message = "Skill level is required")
    private String skillLevel;   // BEGINNER | INTERMEDIATE | ADVANCED

    @NotBlank(message = "Preferred time is required")
    @Size(max = 100)
    private String preferredTime;

    @NotBlank(message = "Preferred location is required")
    private String preferredLocation; // LIBRARY | CLASSROOM | HOSTEL | ONLINE

    public StudyPreference() {}

    public StudyPreference(int preferenceId, int studentId, String subjectName,
                           String learningGoal, String skillLevel,
                           String preferredTime, String preferredLocation) {
        this.preferenceId = preferenceId;
        this.studentId = studentId;
        this.subjectName = subjectName;
        this.learningGoal = learningGoal;
        this.skillLevel = skillLevel;
        this.preferredTime = preferredTime;
        this.preferredLocation = preferredLocation;
    }

    public int getPreferenceId()                    { return preferenceId; }
    public void setPreferenceId(int id)             { this.preferenceId = id; }

    public int getStudentId()                       { return studentId; }
    public void setStudentId(int id)                { this.studentId = id; }

    public String getSubjectName()                  { return subjectName; }
    public void setSubjectName(String s)            { this.subjectName = s; }

    public String getLearningGoal()                 { return learningGoal; }
    public void setLearningGoal(String g)           { this.learningGoal = g; }

    public String getSkillLevel()                   { return skillLevel; }
    public void setSkillLevel(String s)             { this.skillLevel = s; }

    public String getPreferredTime()                { return preferredTime; }
    public void setPreferredTime(String t)          { this.preferredTime = t; }

    public String getPreferredLocation()            { return preferredLocation; }
    public void setPreferredLocation(String l)      { this.preferredLocation = l; }
}
