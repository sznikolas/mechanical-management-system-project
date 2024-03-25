package com.nikolas.mechanicalmanagementsystem.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@Slf4j
@ControllerAdvice
public class UnauthorizedExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public void handleUnauthorizedException(UnauthorizedException ex, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.error("UnauthorizedExceptionHandler::handleUnauthorizedException - Unauthorized request: please log in");
        response.sendRedirect("/system/login");
    }
}
