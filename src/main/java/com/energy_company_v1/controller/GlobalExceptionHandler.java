package com.energy_company_v1.controller;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleException(Exception e, Model model, HttpServletRequest request) {
        model.addAttribute("errorMessage", "Произошла ошибка: " + e.getMessage());
        model.addAttribute("errorCode", "500");
        model.addAttribute("requestedUrl", request.getRequestURL());
        return "error";
    }

    @ExceptionHandler(org.springframework.web.servlet.NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFoundException(Model model, HttpServletRequest request) {
        model.addAttribute("errorMessage", "Страница не найдена");
        model.addAttribute("errorCode", "404");
        model.addAttribute("requestedUrl", request.getRequestURL());
        return "error";
    }

    // Обработка других типов ошибок
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String handleAccessDeniedException(Model model, HttpServletRequest request) {
        model.addAttribute("errorMessage", "Доступ запрещен");
        model.addAttribute("errorCode", "403");
        model.addAttribute("requestedUrl", request.getRequestURL());
        return "error";
    }
}