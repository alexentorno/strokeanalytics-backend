package com.strokeanalytics.backend.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Allows the frontend (running on a different origin during local
 * development) to call this API from the browser.
 * <p>
 * Matches any localhost/127.0.0.1 port rather than a single fixed one,
 * because the Next.js dev server falls back to 3001, 3002, etc. whenever
 * port 3000 is already taken — a fixed-origin allow-list would silently
 * stop working the moment that happens. Tighten this to an exact
 * production frontend URL once one exists.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private static final String[] LOCAL_DEV_ORIGIN_PATTERNS = {
            "http://localhost:*",
            "http://127.0.0.1:*"
    };

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(LOCAL_DEV_ORIGIN_PATTERNS)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
