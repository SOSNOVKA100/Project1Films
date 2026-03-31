package com.example.project1films.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class RequestTimingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        httpRequest.setAttribute("startTime", System.currentTimeMillis());

        try {
            chain.doFilter(request, response);
        } finally {
            long startTime = (Long) httpRequest.getAttribute("startTime");
            long duration = System.currentTimeMillis() - startTime;

            if (duration > 1000) { // Если запрос выполняется дольше 1 секунды
                System.out.printf("Slow request: %s %s took %d ms%n",
                        httpRequest.getMethod(),
                        httpRequest.getRequestURI(),
                        duration);
            }
        }
    }
}