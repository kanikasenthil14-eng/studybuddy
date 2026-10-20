package com.studybuddy.controller;

import com.studybuddy.model.Student;
import com.studybuddy.model.StudyPreference;
import com.studybuddy.service.MatchingService;
import com.studybuddy.service.MatchingService.MatchResult;
import com.studybuddy.service.PreferenceService;
import com.studybuddy.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/matches")
public class MatchingController {

    private final MatchingService matchingService;
    private final StudentService studentService;
    private final PreferenceService preferenceService;

    public MatchingController(MatchingService matchingService,
                              StudentService studentService,
                              PreferenceService preferenceService) {
        this.matchingService = matchingService;
        this.studentService = studentService;
        this.preferenceService = preferenceService;
    }

    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("students",    studentService.getAllStudents());
        model.addAttribute("subjects",    preferenceService.getAllSubjects());
        model.addAttribute("skillLevels", PreferenceService.SKILL_LEVEL_LIST);
        model.addAttribute("locations",   PreferenceService.LOCATION_LIST);
        model.addAttribute("timeOptions", PreferenceService.TIME_OPTIONS);
        return "matches/search";
    }

    @GetMapping("/results")
    public String searchResults(@RequestParam int studentId,
                                @RequestParam String subject,
                                @RequestParam(required = false) String filterSkill,
                                @RequestParam(required = false) String filterTime,
                                @RequestParam(required = false) String filterLocation,
                                Model model) {
        try {
            Student searcher = studentService.getStudentById(studentId);

            // Searcher must have a preference for this subject
            List<StudyPreference> myPrefs = preferenceService.getPreferencesForStudent(studentId);
            boolean hasSubject = myPrefs.stream()
                    .anyMatch(p -> p.getSubjectName().equalsIgnoreCase(subject));
            if (!hasSubject) {
                model.addAttribute("warningMessage",
                        "You don't have a preference for \"" + subject + "\". Add it first to get a full compatibility score.");
            }

            List<MatchResult> results = matchingService.findMatches(
                    searcher, subject, filterSkill, filterTime, filterLocation);

            // Attach candidate Student objects
            for (MatchResult r : results) {
                try {
                    Student candidate = studentService.getStudentById(r.candidatePref.getStudentId());
                    // We use a wrapper approach: store candidate in a map for the template
                    // Since MatchResult.candidate is final, we rebuild with the student
                    // (MatchResult is a simple data holder; we pass a parallel list)
                } catch (Exception ignored) {}
            }

            // Build enriched list with student info attached
            List<EnrichedMatch> enriched = results.stream().map(r -> {
                Student c = studentService.findById(r.candidatePref.getStudentId()).orElse(null);
                return new EnrichedMatch(r, c);
            }).toList();

            model.addAttribute("searcher",       searcher);
            model.addAttribute("subject",        subject);
            model.addAttribute("matches",        enriched);
            model.addAttribute("filterSkill",    filterSkill);
            model.addAttribute("filterTime",     filterTime);
            model.addAttribute("filterLocation", filterLocation);
            model.addAttribute("students",       studentService.getAllStudents());
            model.addAttribute("subjects",       preferenceService.getAllSubjects());
            model.addAttribute("skillLevels",    PreferenceService.SKILL_LEVEL_LIST);
            model.addAttribute("locations",      PreferenceService.LOCATION_LIST);
            model.addAttribute("timeOptions",    PreferenceService.TIME_OPTIONS);
            return "matches/search";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }

    @GetMapping("/details")
    public String matchDetails(@RequestParam int studentId,
                               @RequestParam int candidateId,
                               @RequestParam String subject,
                               Model model) {
        try {
            Student searcher  = studentService.getStudentById(studentId);
            Student candidate = studentService.getStudentById(candidateId);

            List<StudyPreference> myPrefs = preferenceService.getPreferencesForStudent(studentId);
            StudyPreference myPref = myPrefs.stream()
                    .filter(p -> p.getSubjectName().equalsIgnoreCase(subject))
                    .findFirst().orElse(null);

            List<StudyPreference> theirPrefs = preferenceService.getPreferencesForStudent(candidateId);
            StudyPreference theirPref = theirPrefs.stream()
                    .filter(p -> p.getSubjectName().equalsIgnoreCase(subject))
                    .findFirst().orElse(null);

            if (theirPref == null) {
                model.addAttribute("errorMessage", "Candidate does not have a preference for this subject.");
                return "error/404";
            }

            MatchResult result = matchingService.score(myPref, theirPref);

            model.addAttribute("searcher",   searcher);
            model.addAttribute("candidate",  candidate);
            model.addAttribute("myPref",     myPref);
            model.addAttribute("theirPref",  theirPref);
            model.addAttribute("result",     result);
            model.addAttribute("subject",    subject);
            return "matches/details";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }

    /** Simple wrapper to carry both MatchResult and the resolved Student together in templates. */
    public static class EnrichedMatch {
        public final MatchResult result;
        public final Student candidate;

        public EnrichedMatch(MatchResult result, Student candidate) {
            this.result    = result;
            this.candidate = candidate;
        }
    }
}
