package com.petshop.config;

import controller.filter.AuthorizationFilter;
import controller.filter.CookieAttributeFilter;
import controller.filter.CsrfFilter;
import controller.filter.PetTypeFilter;
import controller.filter.RateLimitFilter;
import controller.filter.StaticAssetCacheFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the legacy servlet filters with the same URL patterns and
 * ordering they had in WEB-INF/web.xml before the Spring Boot migration.
 *
 * Order (must match the old web.xml filter-mapping order):
 * 1. RateLimitFilter      /*          (auth rate limiting, anti-brute-force)
 * 2. AuthorizationFilter  /admin/*, /pages/admin/*  (role checks)
 * 3. CsrfFilter           /*          (CSRF token validation on POST/PUT/DELETE)
 * 4. StaticAssetCacheFilter /assets/* (cache headers for static assets)
 * 5. CookieAttributeFilter /*         (HttpOnly/Secure/SameSite cookie flags)
 * 6. PetTypeFilter        /*          (pet-type model attribute for pages)
 */
@Configuration
public class WebFilterConfig {

    @Bean
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilter() {
        FilterRegistrationBean<RateLimitFilter> bean = new FilterRegistrationBean<>(new RateLimitFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(10);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<AuthorizationFilter> authorizationFilter() {
        FilterRegistrationBean<AuthorizationFilter> bean = new FilterRegistrationBean<>(new AuthorizationFilter());
        bean.addUrlPatterns("/admin/*", "/pages/admin/*");
        bean.setOrder(20);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<CsrfFilter> csrfFilter() {
        FilterRegistrationBean<CsrfFilter> bean = new FilterRegistrationBean<>(new CsrfFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(30);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<StaticAssetCacheFilter> staticAssetCacheFilter() {
        FilterRegistrationBean<StaticAssetCacheFilter> bean = new FilterRegistrationBean<>(new StaticAssetCacheFilter());
        bean.addUrlPatterns("/assets/*");
        bean.setOrder(40);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<CookieAttributeFilter> cookieAttributeFilter() {
        FilterRegistrationBean<CookieAttributeFilter> bean = new FilterRegistrationBean<>(new CookieAttributeFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(50);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<PetTypeFilter> petTypeFilter() {
        FilterRegistrationBean<PetTypeFilter> bean = new FilterRegistrationBean<>(new PetTypeFilter());
        bean.addUrlPatterns("/*");
        bean.setOrder(60);
        return bean;
    }
}
