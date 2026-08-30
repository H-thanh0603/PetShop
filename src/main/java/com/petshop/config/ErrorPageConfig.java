package com.petshop.config;

import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.ErrorPageRegistrar;
import org.springframework.boot.web.server.ErrorPageRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import java.nio.charset.StandardCharsets;

/**
 * Replaces the <error-page> entries from WEB-INF/web.xml. The error JSPs
 * render the same pages as before (404.jsp / 500.jsp, no stack traces).
 */
@Configuration
public class ErrorPageConfig {

    @Bean
    public ErrorPageRegistrar errorPageRegistrar() {
        return (ErrorPageRegistry registry) -> {
            registry.addErrorPages(new ErrorPage(HttpStatus.NOT_FOUND, "/pages/error/404.jsp"));
            registry.addErrorPages(new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/pages/error/500.jsp"));
            registry.addErrorPages(new ErrorPage(Throwable.class, "/pages/error/500.jsp"));
        };
    }
}
