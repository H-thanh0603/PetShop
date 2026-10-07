package com.petshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Spring Boot entry point for the PetShop application.
 *
 * Legacy servlets/filters are registered explicitly in
 * {@link com.petshop.config.WebRegistrationConfig} during the migration;
 * they keep their existing behaviour. Run with `java -jar petshop-boot.war`
 * or deploy the plain WAR to an external Tomcat 10.1.
 *
 * The {@code services} package lives outside {@code com.petshop} (legacy
 * package consolidation kept its name), so it must be scanned explicitly —
 * controllers constructor-inject those services as Spring beans.
 */
@SpringBootApplication(scanBasePackages = {"com.petshop", "services"})
public class PetShopApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(PetShopApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(PetShopApplication.class);
    }
}
