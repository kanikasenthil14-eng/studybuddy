-- ============================================================
-- Study Buddy Matcher - Database Schema
-- Database: study_buddy
-- Run this script in MySQL Workbench before starting the app.
-- This script creates tables only if they do not already exist.
-- It will NOT drop your existing database or data.
-- ============================================================

CREATE DATABASE IF NOT EXISTS study_buddy
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE study_buddy;

-- Table: students
CREATE TABLE IF NOT EXISTS students (
    student_id   INT AUTO_INCREMENT PRIMARY KEY,
    student_name VARCHAR(100) NOT NULL,
    email        VARCHAR(150) NOT NULL UNIQUE,
    contact_info VARCHAR(30),
    department   VARCHAR(100) NOT NULL,
    year_of_study INT NOT NULL,
    CONSTRAINT chk_year CHECK (year_of_study BETWEEN 1 AND 6)
);

-- Table: study_preferences
CREATE TABLE IF NOT EXISTS study_preferences (
    preference_id     INT AUTO_INCREMENT PRIMARY KEY,
    student_id        INT NOT NULL,
    subject_name      VARCHAR(100) NOT NULL,
    learning_goal     VARCHAR(255) NOT NULL,
    skill_level       ENUM('BEGINNER','INTERMEDIATE','ADVANCED') NOT NULL,
    preferred_time    VARCHAR(100) NOT NULL,
    preferred_location ENUM('LIBRARY','CLASSROOM','HOSTEL','ONLINE') NOT NULL,
    CONSTRAINT fk_pref_student FOREIGN KEY (student_id)
        REFERENCES students(student_id) ON DELETE CASCADE,
    -- Prevent duplicate subject for the same student (case-insensitive via LOWER in app)
    CONSTRAINT uq_student_subject UNIQUE (student_id, subject_name)
);

-- Drop index if exists before creating (safe re-run)
SET @exist_idx = (SELECT COUNT(*) FROM information_schema.statistics 
    WHERE table_schema='study_buddy' AND table_name='study_preferences' AND index_name='idx_pref_subject');
SET @sql = IF(@exist_idx > 0, 'SELECT 1', 'CREATE INDEX idx_pref_subject ON study_preferences(subject_name)');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Table: study_requests
CREATE TABLE IF NOT EXISTS study_requests (
    request_id   INT AUTO_INCREMENT PRIMARY KEY,
    requester_id INT NOT NULL,
    receiver_id  INT NOT NULL,
    subject_name VARCHAR(100) NOT NULL,
    request_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status       ENUM('PENDING','ACCEPTED','REJECTED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_req_requester FOREIGN KEY (requester_id)
        REFERENCES students(student_id) ON DELETE CASCADE,
    CONSTRAINT fk_req_receiver FOREIGN KEY (receiver_id)
        REFERENCES students(student_id) ON DELETE CASCADE,
    -- A student cannot send a request to themselves (enforced in app layer too)
    CONSTRAINT chk_no_self_request CHECK (requester_id <> receiver_id)
);

SET @e1 = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema='study_buddy' AND table_name='study_requests' AND index_name='idx_req_status');
SET @s1 = IF(@e1 > 0, 'SELECT 1', 'CREATE INDEX idx_req_status ON study_requests(status)');
PREPARE stmt FROM @s1; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @e2 = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema='study_buddy' AND table_name='study_requests' AND index_name='idx_req_subject');
SET @s2 = IF(@e2 > 0, 'SELECT 1', 'CREATE INDEX idx_req_subject ON study_requests(subject_name)');
PREPARE stmt FROM @s2; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @e3 = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema='study_buddy' AND table_name='study_requests' AND index_name='idx_req_receiver');
SET @s3 = IF(@e3 > 0, 'SELECT 1', 'CREATE INDEX idx_req_receiver ON study_requests(receiver_id)');
PREPARE stmt FROM @s3; EXECUTE stmt; DEALLOCATE PREPARE stmt;
