package com.nikolas.mechanicalmanagementsystem.security;

import com.nikolas.mechanicalmanagementsystem.utility.UrlUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;

import java.io.IOException;

public class CustomAccessDeniedHandler extends AccessDeniedHandlerImpl {

    /**
     * Handles access denied exception by redirecting the user to the access denied page.
     *
     * @param request             the HTTP request
     * @param response            the HTTP response
     * @param accessDeniedException the access denied exception that occurred
     * @throws IOException      if an I/O error occurs during the redirect
     * @throws ServletException if the servlet encounters difficulty
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        String url = UrlUtil.getApplicationUrl(request);
        response.sendRedirect(url + "/accessDenied");
        super.handle(request, response, accessDeniedException);
    }
}
