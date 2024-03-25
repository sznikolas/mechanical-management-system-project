package com.nikolas.mechanicalmanagementsystem.utility;

import jakarta.servlet.http.HttpServletRequest;

public class UrlUtil {
    public static String getApplicationUrl(HttpServletRequest request){
        String appUrl = request.getRequestURL().toString();
        return appUrl.replace(request.getServletPath(), "");
    }

//        http://localhost:8080/system/something/view/8
//        contextPath: /system
//        servletPath: /something
//        pathInfo: /view/8
//        getRequestURI(): /system/something/view/8

}
