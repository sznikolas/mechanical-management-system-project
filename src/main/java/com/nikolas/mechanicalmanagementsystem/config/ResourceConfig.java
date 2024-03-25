package com.nikolas.mechanicalmanagementsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceConfig implements WebMvcConfigurer {

    /**
     * Adds resource handlers for serving static CSS and JS files.
     * CSS files are served from the "/css/" path, and JS files are served from the "/js/" path.
     * These resources are loaded from the classpath under the "/static/css/" and "/static/js/" directories, respectively.
     *
     * @param registry the ResourceHandlerRegistry to which the resource handlers are added
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/css/**","/js/**")
                .addResourceLocations("classpath:/static/css/", "classpath:/static/js/");
    }
}
