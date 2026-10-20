package com.studybuddy.controller;

import com.studybuddy.service.StudentService;
import com.studybuddy.service.StudyRequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/requests")
public class RequestController {

    private final StudyRequestService requestService;
    private final StudentService studentService;

    public RequestController(StudyRequestService requestService, StudentService studentService) {
        this.requestService = requestService;
        this.studentService = studentService;
    }

    // --- Select student to view requests ---

    @GetMapping("/select")
    public String selectStudent(Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        return "requests/select";
    }

    // --- Incoming requests ---

    @GetMapping("/incoming")
    public String incoming(@RequestParam int studentId, Model model) {
        try {
            model.addAttribute("student",  studentService.getStudentById(studentId));
            model.addAttribute("requests", requestService.getIncomingRequests(studentId));
            model.addAttribute("students", studentService.getAllStudents());
            return "requests/incoming";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }

    // --- Sent requests ---

    @GetMapping("/sent")
    public String sent(@RequestParam int studentId, Model model) {
        try {
            model.addAttribute("student",  studentService.getStudentById(studentId));
            model.addAttribute("requests", requestService.getSentRequests(studentId));
            model.addAttribute("students", studentService.getAllStudents());
            return "requests/sent";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }

    // --- Send a request (POST from match details page) ---

    @PostMapping("/send")
    public String sendRequest(@RequestParam int requesterId,
                              @RequestParam int receiverId,
                              @RequestParam String subject,
                              RedirectAttributes redirectAttrs) {
        try {
            requestService.sendRequest(requesterId, receiverId, subject);
            redirectAttrs.addFlashAttribute("successMessage", "Study request sent successfully!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/matches/details?studentId=" + requesterId
                + "&candidateId=" + receiverId + "&subject=" + subject;
    }

    // --- Accept ---

    @PostMapping("/{id}/accept")
    public String accept(@PathVariable int id,
                         @RequestParam int studentId,
                         RedirectAttributes redirectAttrs) {
        try {
            requestService.acceptRequest(id, studentId);
            redirectAttrs.addFlashAttribute("successMessage", "Request accepted!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/requests/incoming?studentId=" + studentId;
    }

    // --- Reject ---

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable int id,
                         @RequestParam int studentId,
                         RedirectAttributes redirectAttrs) {
        try {
            requestService.rejectRequest(id, studentId);
            redirectAttrs.addFlashAttribute("successMessage", "Request rejected.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/requests/incoming?studentId=" + studentId;
    }

    // --- Cancel ---

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable int id,
                         @RequestParam int studentId,
                         RedirectAttributes redirectAttrs) {
        try {
            requestService.cancelRequest(id, studentId);
            redirectAttrs.addFlashAttribute("successMessage", "Request cancelled.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/requests/sent?studentId=" + studentId;
    }

    // --- Complete partnership ---

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable int id,
                           @RequestParam int studentId,
                           RedirectAttributes redirectAttrs) {
        try {
            requestService.completePartnership(id, studentId);
            redirectAttrs.addFlashAttribute("successMessage", "Partnership marked as completed!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/requests/partnerships?studentId=" + studentId;
    }

    // --- Partnerships ---

    @GetMapping("/partnerships")
    public String partnerships(@RequestParam(defaultValue = "0") int studentId, Model model) {
        model.addAttribute("students", studentService.getAllStudents());
        if (studentId == 0) {
            model.addAttribute("student", null);
            return "requests/partnerships";
        }
        try {
            model.addAttribute("student",      studentService.getStudentById(studentId));
            model.addAttribute("partnerships", requestService.getPartnerships(studentId));
            return "requests/partnerships";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/404";
        }
    }
}
