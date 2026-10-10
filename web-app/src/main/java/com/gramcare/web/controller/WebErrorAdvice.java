package com.gramcare.web.controller;

import com.gramcare.web.client.ServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Catches backend failures raised while submitting forms (validation errors
 * returned by a service, or a service being down) and shows a readable error
 * page instead of a stack trace. List pages handle their own errors inline.
 */
@ControllerAdvice
public class WebErrorAdvice {

    private static final Logger log = LoggerFactory.getLogger(WebErrorAdvice.class);

    @ExceptionHandler(ServiceUnavailableException.class)
    public String handleBackendError(ServiceUnavailableException ex, Model model) {
        log.warn("Backend call failed: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error-page";
    }

    @ExceptionHandler({NumberFormatException.class, IllegalArgumentException.class})
    public String handleBadInput(RuntimeException ex, Model model) {
        model.addAttribute("errorMessage", "Some of the values entered were not valid. Please go back and check the form.");
        return "error-page";
    }
}
