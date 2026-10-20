package com.studybuddy.model;

import jakarta.validation.constraints.*;

public class Student {

    private int studentId;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must be 100 characters or fewer")
    private String studentName;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    @Size(max = 150)
    private String email;

    @Size(max = 30, message = "Contact info must be 30 characters or fewer")
    private String contactInfo;

    @NotBlank(message = "Department is required")
    @Size(max = 100)
    private String department;

    @Min(value = 1, message = "Year of study must be between 1 and 6")
    @Max(value = 6, message = "Year of study must be between 1 and 6")
    private int yearOfStudy;

    public Student() {}

    public Student(int studentId, String studentName, String email,
                   String contactInfo, String department, int yearOfStudy) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.email = email;
        this.contactInfo = contactInfo;
        this.department = department;
        this.yearOfStudy = yearOfStudy;
    }

    public int getStudentId()               { return studentId; }
    public void setStudentId(int id)        { this.studentId = id; }

    public String getStudentName()          { return studentName; }
    public void setStudentName(String n)    { this.studentName = n; }

    public String getEmail()                { return email; }
    public void setEmail(String e)          { this.email = e; }

    public String getContactInfo()          { return contactInfo; }
    public void setContactInfo(String c)    { this.contactInfo = c; }

    public String getDepartment()           { return department; }
    public void setDepartment(String d)     { this.department = d; }

    public int getYearOfStudy()             { return yearOfStudy; }
    public void setYearOfStudy(int y)       { this.yearOfStudy = y; }
}
