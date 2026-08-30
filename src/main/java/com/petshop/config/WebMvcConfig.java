package com.petshop.config;

import Util.AppConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC glue for the migrated app:
 * - "/" forwards to the legacy index.jsp (which redirects to /home)
 * - uploaded images are served from the configurable upload directory
 *   (outside the WAR so redeploys don't delete them), falling back to the
 *   packaged webapp assets for images shipped with the application.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.jsp");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = AppConfig.getOrDefault("app.upload-dir",
                System.getProperty("user.dir") + File.separator + "uploads");
        // Must be absolute for the "file:" resource location to resolve.
        String absoluteUploadDir = new File(uploadDir).getAbsolutePath();
        // Uploaded images (outside the WAR) take priority; packaged images in
        // classpath:/static/assets/images are the fallback.
        registry.addResourceHandler("/assets/images/**")
                .addResourceLocations("file:" + absoluteUploadDir + File.separator,
                        "classpath:/static/assets/images/");
    }
}
