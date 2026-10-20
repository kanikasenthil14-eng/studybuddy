package com.studybuddy.controller;

import com.studybuddy.service.PreferenceService;
import com.studybuddy.service.StudentService;
import com.studybuddy.service.StudyRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final StudentService studentService;
    private final PreferenceService preferenceService;
    private final com.studybuddy.repository.StudyRequestRepository requestRepo;

    public HomeController(StudentService studentService,
                          PreferenceService preferenceService,
                          com.studybuddy.repository.StudyRequestRepository requestRepo) {
        this.studentService = studentService;
        this.preferenceService = preferenceService;
        this.requestRepo = requestRepo;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("totalStudents",     studentService.countStudents());
        model.addAttribute("totalPreferences",  preferenceService.countPreferences());
        model.addAttribute("pendingRequests",   requestRepo.countPending());
        model.addAttribute("acceptedPartnerships", requestRepo.countAccepted());
        return "index";
    }
}
