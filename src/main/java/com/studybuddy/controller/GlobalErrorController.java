package com.studybuddy.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class GlobalErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message    = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        if (statusCode != null) {
            int code = Integer.parseInt(statusCode.toString());
            if (code == HttpStatus.NOT_FOUND.value()) {
                model.addAttribute("errorMessage", "The page or record you requested was not found.");
                return "error/404";
            }
        }
        model.addAttribute("errorMessage",
                message != null && !message.toString().isBlank() ? message.toString() : null);
        return "error/500";
    }
}
