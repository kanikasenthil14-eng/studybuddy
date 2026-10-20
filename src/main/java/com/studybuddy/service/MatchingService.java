package com.studybuddy.service;

import com.studybuddy.model.Student;
import com.studybuddy.model.StudyPreference;
import com.studybuddy.repository.PreferenceRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MatchingService {

    private final PreferenceRepository prefRepo;

    public MatchingService(PreferenceRepository prefRepo) {
        this.prefRepo = prefRepo;
    }

    /**
     * Holds the compatibility result for one candidate student.
     */
    public static class MatchResult {
        public final Student candidate;
        public final StudyPreference candidatePref;  // the preference matching the searched subject
        public final int totalScore;
        public final int subjectScore;
        public final int goalScore;
        public final int timeScore;
        public final int locationScore;
        public final int skillScore;

        public MatchResult(Student candidate, StudyPreference candidatePref,
                           int subjectScore, int goalScore, int timeScore,
                           int locationScore, int skillScore) {
            this.candidate      = candidate;
            this.candidatePref  = candidatePref;
            this.subjectScore   = subjectScore;
            this.goalScore      = goalScore;
            this.timeScore      = timeScore;
            this.locationScore  = locationScore;
            this.skillScore     = skillScore;
            this.totalScore     = subjectScore + goalScore + timeScore + locationScore + skillScore;
        }
    }

    /**
     * Finds and scores all candidates who share the given subject with the searcher.
     * Filters optionally by skillLevel, preferredTime, preferredLocation.
     * Returns results sorted by score descending, then by student ID ascending.
     */
    public List<MatchResult> findMatches(Student searcher, String subject,
                                         String filterSkill, String filterTime, String filterLocation) {
        // Get the searcher's own preference for this subject
        List<StudyPreference> myPrefs = prefRepo.findByStudentId(searcher.getStudentId());
        StudyPreference myPref = myPrefs.stream()
                .filter(p -> p.getSubjectName().equalsIgnoreCase(subject))
                .findFirst()
                .orElse(null);

        // Get all other students' preferences for this subject
        List<StudyPreference> candidates = prefRepo.findBySubjectExcludingStudent(subject, searcher.getStudentId());

        List<MatchResult> results = new ArrayList<>();
        for (StudyPreference cp : candidates) {
            // Apply optional filters
            if (filterSkill != null && !filterSkill.isBlank()
                    && !cp.getSkillLevel().equalsIgnoreCase(filterSkill)) continue;
            if (filterTime != null && !filterTime.isBlank()
                    && !cp.getPreferredTime().equalsIgnoreCase(filterTime)) continue;
            if (filterLocation != null && !filterLocation.isBlank()
                    && !cp.getPreferredLocation().equalsIgnoreCase(filterLocation)) continue;

            MatchResult result = score(myPref, cp);
            results.add(result);
        }

        // Sort: highest score first, tie-break by student ID
        results.sort(Comparator.comparingInt((MatchResult r) -> r.totalScore).reversed()
                .thenComparingInt(r -> r.candidatePref.getStudentId()));

        return results;
    }

    /**
     * Scores a single candidate preference against the searcher's preference.
     * Scoring rules (max 100):
     *   Subject match  : 40 pts (always awarded here since we already filtered by subject)
     *   Learning goal  : 20 pts (normalized exact match after lowercasing and trimming)
     *   Preferred time : 15 pts (case-insensitive exact match)
     *   Location       : 15 pts (exact match)
     *   Skill level    : 10 pts (equal or adjacent in BEGINNER < INTERMEDIATE < ADVANCED)
     */
    public MatchResult score(StudyPreference myPref, StudyPreference candidatePref) {
        int subjectScore  = 40; // already filtered by subject
        int goalScore     = 0;
        int timeScore     = 0;
        int locationScore = 0;
        int skillScore    = 0;

        if (myPref != null) {
            // Learning goal: normalized exact match
            String myGoal   = normalize(myPref.getLearningGoal());
            String theirGoal = normalize(candidatePref.getLearningGoal());
            if (!myGoal.isBlank() && myGoal.equals(theirGoal)) {
                goalScore = 20;
            }

            // Preferred time: case-insensitive exact match
            if (myPref.getPreferredTime() != null && candidatePref.getPreferredTime() != null
                    && myPref.getPreferredTime().equalsIgnoreCase(candidatePref.getPreferredTime())) {
                timeScore = 15;
            }

            // Location: exact match
            if (myPref.getPreferredLocation() != null && candidatePref.getPreferredLocation() != null
                    && myPref.getPreferredLocation().equalsIgnoreCase(candidatePref.getPreferredLocation())) {
                locationScore = 15;
            }

            // Skill level: equal or adjacent
            if (myPref.getSkillLevel() != null && candidatePref.getSkillLevel() != null) {
                skillScore = skillCompatibility(myPref.getSkillLevel(), candidatePref.getSkillLevel());
            }
        }

        return new MatchResult(null, candidatePref,
                subjectScore, goalScore, timeScore, locationScore, skillScore);
    }

    /** Trims and lowercases a string for comparison. */
    private String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    /**
     * Returns 10 if skill levels are equal or adjacent, 0 otherwise.
     * Order: BEGINNER(0) < INTERMEDIATE(1) < ADVANCED(2)
     */
    private int skillCompatibility(String a, String b) {
        List<String> order = List.of("BEGINNER", "INTERMEDIATE", "ADVANCED");
        int idxA = order.indexOf(a.toUpperCase());
        int idxB = order.indexOf(b.toUpperCase());
        if (idxA < 0 || idxB < 0) return 0;
        return Math.abs(idxA - idxB) <= 1 ? 10 : 0;
    }
}
