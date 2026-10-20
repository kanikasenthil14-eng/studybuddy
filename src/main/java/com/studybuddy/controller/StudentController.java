package com.studybuddy.controller;

import com.studybuddy.model.Student;
import com.studybuddy.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String listStudents(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        return "students/list";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("student", new Student());
        return "students/register";
    }

    @PostMapping
    public String registerStudent(@Valid @ModelAttribute("student") Student student,
                                  BindingResult result,
                                  RedirectAttributes redirectAttrs) {
        if (result.hasErrors()) {
            return "students/register";
        }
        try {
            int newId = studentService.registerStudent(student);
            redirectAttrs.addFlashAttribute("successMessage", "Student registered successfully!");
            return "redirect:/students/" + newId;
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "duplicate", e.getMessage());
            return "students/register";
        }
    }

    @GetMapping("/{id}")
    public String viewStudent(@PathVariable int id, Model model) {
        try {
            model.addAttribute("student", studentService.getStudentById(id));
            return "students/details";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable int id, Model model) {
        try {
            model.addAttribute("student", studentService.getStudentById(id));
            return "students/edit";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }

    @PostMapping("/{id}/update")
    public String updateStudent(@PathVariable int id,
                                @Valid @ModelAttribute("student") Student student,
                                BindingResult result,
                                RedirectAttributes redirectAttrs) {
        student.setStudentId(id);
        if (result.hasErrors()) {
            return "students/edit";
        }
        try {
            studentService.updateStudent(student);
            redirectAttrs.addFlashAttribute("successMessage", "Profile updated successfully!");
            return "redirect:/students/" + id;
        } catch (IllegalArgumentException e) {
            result.rejectValue("email", "duplicate", e.getMessage());
            return "students/edit";
        }
    }
}
