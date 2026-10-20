package com.studybuddy.controller;

import com.studybuddy.model.StudyPreference;
import com.studybuddy.service.PreferenceService;
import com.studybuddy.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/preferences")
public class PreferenceController {

    private final PreferenceService preferenceService;
    private final StudentService studentService;

    public PreferenceController(PreferenceService preferenceService, StudentService studentService) {
        this.preferenceService = preferenceService;
        this.studentService = studentService;
    }

    @GetMapping("/student/{studentId}")
    public String listPreferences(@PathVariable int studentId, Model model) {
        try {
            model.addAttribute("student", studentService.getStudentById(studentId));
            model.addAttribute("preferences", preferenceService.getPreferencesForStudent(studentId));
            return "preferences/list";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }

    @GetMapping("/add/{studentId}")
    public String showAddForm(@PathVariable int studentId, Model model) {
        try {
            model.addAttribute("student", studentService.getStudentById(studentId));
            StudyPreference pref = new StudyPreference();
            pref.setStudentId(studentId);
            model.addAttribute("preference", pref);
            addDropdownData(model);
            return "preferences/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }

    @PostMapping("/add/{studentId}")
    public String addPreference(@PathVariable int studentId,
                                @Valid @ModelAttribute("preference") StudyPreference pref,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttrs) {
        pref.setStudentId(studentId);
        if (result.hasErrors()) {
            try { model.addAttribute("student", studentService.getStudentById(studentId)); } catch (Exception ignored) {}
            addDropdownData(model);
            return "preferences/form";
        }
        try {
            preferenceService.addPreference(pref);
            redirectAttrs.addFlashAttribute("successMessage", "Preference added successfully!");
            return "redirect:/preferences/student/" + studentId;
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            try { model.addAttribute("student", studentService.getStudentById(studentId)); } catch (Exception ignored) {}
            addDropdownData(model);
            return "preferences/form";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable int id, Model model) {
        try {
            StudyPreference pref = preferenceService.getPreferenceById(id);
            model.addAttribute("preference", pref);
            model.addAttribute("student", studentService.getStudentById(pref.getStudentId()));
            addDropdownData(model);
            return "preferences/form";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }

    @PostMapping("/edit/{id}")
    public String updatePreference(@PathVariable int id,
                                   @Valid @ModelAttribute("preference") StudyPreference pref,
                                   BindingResult result,
                                   Model model,
                                   RedirectAttributes redirectAttrs) {
        pref.setPreferenceId(id);
        if (result.hasErrors()) {
            try { model.addAttribute("student", studentService.getStudentById(pref.getStudentId())); } catch (Exception ignored) {}
            addDropdownData(model);
            return "preferences/form";
        }
        try {
            preferenceService.updatePreference(pref);
            redirectAttrs.addFlashAttribute("successMessage", "Preference updated successfully!");
            return "redirect:/preferences/student/" + pref.getStudentId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            try { model.addAttribute("student", studentService.getStudentById(pref.getStudentId())); } catch (Exception ignored) {}
            addDropdownData(model);
            return "preferences/form";
        }
    }

    @PostMapping("/delete/{id}")
    public String deletePreference(@PathVariable int id, RedirectAttributes redirectAttrs) {
        try {
            StudyPreference pref = preferenceService.getPreferenceById(id);
            int studentId = pref.getStudentId();
            preferenceService.deletePreference(id);
            redirectAttrs.addFlashAttribute("successMessage", "Preference deleted.");
            return "redirect:/preferences/student/" + studentId;
        } catch (IllegalArgumentException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/students";
        }
    }

    private void addDropdownData(Model model) {
        model.addAttribute("skillLevels",  PreferenceService.SKILL_LEVEL_LIST);
        model.addAttribute("locations",    PreferenceService.LOCATION_LIST);
        model.addAttribute("timeOptions",  PreferenceService.TIME_OPTIONS);
    }
}
