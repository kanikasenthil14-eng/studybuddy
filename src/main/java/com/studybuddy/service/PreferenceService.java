package com.studybuddy.service;

import com.studybuddy.model.StudyPreference;
import com.studybuddy.repository.PreferenceRepository;
import com.studybuddy.repository.StudentRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class PreferenceService {

    public static final Set<String> VALID_SKILL_LEVELS  = Set.of("BEGINNER", "INTERMEDIATE", "ADVANCED");
    public static final Set<String> VALID_LOCATIONS     = Set.of("LIBRARY", "CLASSROOM", "HOSTEL", "ONLINE");
    public static final List<String> SKILL_LEVEL_LIST   = List.of("BEGINNER", "INTERMEDIATE", "ADVANCED");
    public static final List<String> LOCATION_LIST      = List.of("LIBRARY", "CLASSROOM", "HOSTEL", "ONLINE");
    public static final List<String> TIME_OPTIONS       = List.of("Morning", "Afternoon", "Evening", "Weekend");

    private final PreferenceRepository prefRepo;
    private final StudentRepository studentRepo;

    public PreferenceService(PreferenceRepository prefRepo, StudentRepository studentRepo) {
        this.prefRepo = prefRepo;
        this.studentRepo = studentRepo;
    }

    public List<StudyPreference> getPreferencesForStudent(int studentId) {
        studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        return prefRepo.findByStudentId(studentId);
    }

    public StudyPreference getPreferenceById(int id) {
        return prefRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Preference not found: " + id));
    }

    public int addPreference(StudyPreference pref) {
        validateFields(pref);
        studentRepo.findById(pref.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + pref.getStudentId()));
        if (prefRepo.existsByStudentAndSubject(pref.getStudentId(), pref.getSubjectName())) {
            throw new IllegalArgumentException("You already have a preference for this subject.");
        }
        return prefRepo.save(pref);
    }

    public void updatePreference(StudyPreference pref) {
        validateFields(pref);
        prefRepo.findById(pref.getPreferenceId())
                .orElseThrow(() -> new IllegalArgumentException("Preference not found: " + pref.getPreferenceId()));
        if (prefRepo.existsByStudentAndSubjectExcluding(pref.getStudentId(), pref.getSubjectName(), pref.getPreferenceId())) {
            throw new IllegalArgumentException("You already have a preference for this subject.");
        }
        prefRepo.update(pref);
    }

    public void deletePreference(int id) {
        prefRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Preference not found: " + id));
        prefRepo.delete(id);
    }

    public List<String> getAllSubjects() {
        return prefRepo.findAllSubjects();
    }

    public int countPreferences() {
        return prefRepo.countAll();
    }

    private void validateFields(StudyPreference pref) {
        if (!VALID_SKILL_LEVELS.contains(pref.getSkillLevel())) {
            throw new IllegalArgumentException("Invalid skill level: " + pref.getSkillLevel());
        }
        if (!VALID_LOCATIONS.contains(pref.getPreferredLocation())) {
            throw new IllegalArgumentException("Invalid location: " + pref.getPreferredLocation());
        }
    }
}
