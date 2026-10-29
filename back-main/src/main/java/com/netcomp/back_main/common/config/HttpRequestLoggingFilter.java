package com.netcomp.back_main.common.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class HttpRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpRequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startedAt = System.nanoTime();

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.put("http_method", request.getMethod());
            MDC.put("http_path", request.getRequestURI());
            MDC.put("http_status", Integer.toString(response.getStatus()));
            MDC.put("http_duration_ms", Long.toString((System.nanoTime() - startedAt) / 1_000_000));

            try {
                LOGGER.info("HTTP request");
            } finally {
                MDC.remove("http_method");
                MDC.remove("http_path");
                MDC.remove("http_status");
                MDC.remove("http_duration_ms");
            }
        }
    }
}
