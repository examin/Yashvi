package com.catalog.secretsharing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.Contact;

/**
 * Main Spring Boot Application Class for Shamir's Secret Sharing
 * 
 * This application implements Shamir's Secret Sharing algorithm to reconstruct
 * polynomial secrets from encoded y-values using Lagrange interpolation.
 * 
 * @author Catalog Assignment Solution
 * @version 1.0.0
 */
@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Shamir's Secret Sharing API",
        version = "1.0.0",
        description = "API for reconstructing polynomial secrets using Shamir's Secret Sharing algorithm with Lagrange interpolation",
        contact = @Contact(
            name = "Catalog Assignment",
            email = "support@catalog.com"
        )
    )
)
public class ShamirSecretSharingApplication {

    /**
     * Main method to start the Spring Boot application
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(ShamirSecretSharingApplication.class, args);
        
        // Print startup information
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🔐 SHAMIR'S SECRET SHARING APPLICATION STARTED");
        System.out.println("=".repeat(60));
        System.out.println("📄 API Documentation: http://localhost:8080/swagger-ui/index.html");
        System.out.println("🔍 Health Check: http://localhost:8080/actuator/health");
        System.out.println("🌐 Base URL: http://localhost:8080/api/secret-sharing");
        System.out.println("=".repeat(60));
        System.out.println("✅ Application ready to process secret sharing requests!");
        System.out.println("=".repeat(60) + "\n");
    }
}