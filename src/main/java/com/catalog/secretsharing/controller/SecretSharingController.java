package com.catalog.secretsharing.controller;

import com.catalog.secretsharing.model.TestCaseInput;
import com.catalog.secretsharing.model.SecretResponse;
import com.catalog.secretsharing.model.Keys;
import com.catalog.secretsharing.model.RootPoint;
import com.catalog.secretsharing.service.SecretSharingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.HashMap;

/**
 * REST Controller for Shamir's Secret Sharing operations
 * 
 * This controller provides endpoints for calculating secrets from polynomial
 * root points using Shamir's Secret Sharing algorithm.
 */
@RestController
@RequestMapping("/api/secret-sharing")
@Tag(name = "Secret Sharing", description = "Shamir's Secret Sharing API endpoints")
@Slf4j
public class SecretSharingController {
    
    private final SecretSharingService secretSharingService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public SecretSharingController(SecretSharingService secretSharingService,
                                  ObjectMapper objectMapper) {
        this.secretSharingService = secretSharingService;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Calculate secret from JSON test case
     */
    @PostMapping("/calculate")
    @Operation(
        summary = "Calculate secret from test case",
        description = "Calculates the secret using Shamir's Secret Sharing algorithm from provided root points"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Secret calculated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<SecretResponse> calculateSecret(
            @Valid @RequestBody TestCaseInput testCase) {
        
        log.info("Received secret calculation request with {} points", 
                testCase.getRootPointCount());
        
        try {
            SecretResponse response = secretSharingService.calculateSecret(testCase);
            
            if (response.getSuccess()) {
                log.info("Successfully calculated secret: {}", response.getSecret());
                return ResponseEntity.ok(response);
            } else {
                log.error("Failed to calculate secret: {}", response.getErrorMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error processing secret calculation: {}", e.getMessage(), e);
            SecretResponse errorResponse = SecretResponse.error(
                "Failed to process request: " + e.getMessage(), 
                System.currentTimeMillis()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Calculate secret with verification using multiple point combinations
     */
    @PostMapping("/calculate-verified")
    @Operation(
        summary = "Calculate secret with verification",
        description = "Calculates secret and verifies consistency using different point combinations"
    )
    public ResponseEntity<SecretResponse> calculateSecretWithVerification(
            @Valid @RequestBody TestCaseInput testCase) {
        
        log.info("Received verified secret calculation request");
        
        try {
            SecretResponse response = secretSharingService.calculateSecretWithVerification(testCase);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error in verified calculation: {}", e.getMessage(), e);
            SecretResponse errorResponse = SecretResponse.error(
                "Verification failed: " + e.getMessage(), 
                System.currentTimeMillis()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Calculate secret from uploaded JSON file
     */
    @PostMapping(value = "/calculate-from-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Calculate secret from uploaded JSON file",
        description = "Upload a JSON file containing the test case and calculate the secret"
    )
    public ResponseEntity<SecretResponse> calculateSecretFromFile(
            @Parameter(description = "JSON file containing test case")
            @RequestParam("file") MultipartFile file) {
        
        log.info("Received file upload request: {}", file.getOriginalFilename());
        
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                    SecretResponse.error("File is empty", System.currentTimeMillis())
                );
            }
            
            if (!file.getOriginalFilename().toLowerCase().endsWith(".json")) {
                return ResponseEntity.badRequest().body(
                    SecretResponse.error("File must be a JSON file", System.currentTimeMillis())
                );
            }
            
            // Parse JSON
            String jsonContent = new String(file.getBytes());
            TestCaseInput testCase = objectMapper.readValue(jsonContent, TestCaseInput.class);
            
            // Calculate secret
            SecretResponse response = secretSharingService.calculateSecret(testCase);
            
            if (response.getSuccess()) {
                log.info("Successfully processed file and calculated secret: {}", response.getSecret());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error processing uploaded file: {}", e.getMessage(), e);
            SecretResponse errorResponse = SecretResponse.error(
                "Failed to process file: " + e.getMessage(), 
                System.currentTimeMillis()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Run predefined test cases
     */
    @GetMapping("/test/{testNumber}")
    @Operation(
        summary = "Run predefined test case",
        description = "Runs one of the predefined test cases (1 or 2)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Test completed successfully"),
        @ApiResponse(responseCode = "404", description = "Test case not found")
    })
    public ResponseEntity<SecretResponse> runTestCase(
            @Parameter(description = "Test case number (1 or 2)")
            @PathVariable int testNumber) {
        
        log.info("Running predefined test case: {}", testNumber);
        
        try {
            TestCaseInput testCase = createTestCase(testNumber);
            
            if (testCase == null) {
                return ResponseEntity.notFound().build();
            }
            
            SecretResponse response = secretSharingService.calculateSecret(testCase);
            
            if (response.getSuccess()) {
                log.info("Test case {} completed successfully. Secret: {}", 
                        testNumber, response.getSecret());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Error running test case {}: {}", testNumber, e.getMessage(), e);
            SecretResponse errorResponse = SecretResponse.error(
                "Test case failed: " + e.getMessage(), 
                System.currentTimeMillis()
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Get algorithm information
     */
    @GetMapping("/info")
    @Operation(
        summary = "Get algorithm information",
        description = "Returns information about the algorithm implementation"
    )
    public ResponseEntity<Map<String, Object>> getAlgorithmInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("algorithm", "Shamir's Secret Sharing");
        info.put("method", "Lagrange Interpolation");
        info.put("precision", "High precision using BigDecimal");
        info.put("supportedBases", "2-36");
        info.put("description", secretSharingService.getAlgorithmInfo());
        info.put("version", "1.0.0");
        
        return ResponseEntity.ok(info);
    }
    
    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    @Operation(
        summary = "Health check",
        description = "Simple health check endpoint"
    )
    public ResponseEntity<Map<String, String>> healthCheck() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "Secret Sharing Service");
        status.put("timestamp", java.time.LocalDateTime.now().toString());
        
        return ResponseEntity.ok(status);
    }
    
    /**
     * Creates predefined test cases
     */
    private TestCaseInput createTestCase(int testNumber) {
        switch (testNumber) {
            case 1:
                return createTestCase1();
            case 2:
                return createTestCase2();
            default:
                return null;
        }
    }
    
    /**
     * Creates test case 1
     */
    private TestCaseInput createTestCase1() {
        TestCaseInput testCase = new TestCaseInput();
        testCase.setKeys(new Keys(4, 3));
        
        testCase.addRootPoint(1, new RootPoint(1, "10", "4"));
        testCase.addRootPoint(2, new RootPoint(2, "2", "111"));
        testCase.addRootPoint(3, new RootPoint(3, "10", "12"));
        testCase.addRootPoint(6, new RootPoint(6, "4", "213"));
        
        return testCase;
    }
    
    /**
     * Creates test case 2
     */
    private TestCaseInput createTestCase2() {
        TestCaseInput testCase = new TestCaseInput();
        testCase.setKeys(new Keys(10, 7));
        
        testCase.addRootPoint(1, new RootPoint(1, "6", "13444211440455345511"));
        testCase.addRootPoint(2, new RootPoint(2, "15", "aed7015a346d63"));
        testCase.addRootPoint(3, new RootPoint(3, "15", "6aeeb69631c227c"));
        testCase.addRootPoint(4, new RootPoint(4, "16", "e1b5e05623d881f"));
        testCase.addRootPoint(5, new RootPoint(5, "8", "316034514573652620673"));
        testCase.addRootPoint(6, new RootPoint(6, "3", "2122212201122002221120200210011020220200"));
        testCase.addRootPoint(7, new RootPoint(7, "3", "20120221122211000100210021102001201112121"));
        testCase.addRootPoint(8, new RootPoint(8, "6", "20220554335330240002224253"));
        testCase.addRootPoint(9, new RootPoint(9, "12", "45153788322a1255483"));
        testCase.addRootPoint(10, new RootPoint(10, "7", "1101613130313526312514143"));
        
        return testCase;
    }
}