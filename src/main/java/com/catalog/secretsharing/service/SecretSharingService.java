package com.catalog.secretsharing.service;

import com.catalog.secretsharing.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Main service orchestrating the Shamir's Secret Sharing algorithm
 * 
 * This service coordinates the base decoding and Lagrange interpolation
 * to reconstruct the secret from encoded polynomial roots.
 */
@Service
@Slf4j
public class SecretSharingService {
    
    private final BaseDecoderService baseDecoderService;
    private final LagrangeInterpolationService lagrangeInterpolationService;
    
    @Autowired
    public SecretSharingService(BaseDecoderService baseDecoderService,
                               LagrangeInterpolationService lagrangeInterpolationService) {
        this.baseDecoderService = baseDecoderService;
        this.lagrangeInterpolationService = lagrangeInterpolationService;
    }
    
    /**
     * Calculates the secret from a test case input
     * 
     * @param testCase the test case containing keys and root points
     * @return the secret response with calculated secret and metadata
     */
    public SecretResponse calculateSecret(TestCaseInput testCase) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validate input
            validateTestCase(testCase);
            
            // Get root points and decode them
            List<RootPoint> rootPoints = testCase.getRootPointsList();
            log.info("Processing {} root points, need minimum {}", 
                    rootPoints.size(), testCase.getKeys().getK());
            
            // Decode y-values from their respective bases
            List<RootPoint> decodedPoints = rootPoints.stream()
                    .map(baseDecoderService::decodeRootPoint)
                    .collect(Collectors.toList());
            
            // Select k points for interpolation (use first k points)
            int k = testCase.getKeys().getK();
            List<RootPoint> selectedPoints = decodedPoints.subList(0, k);
            
            // Convert to interpolation points
            List<LagrangeInterpolationService.Point> interpolationPoints = 
                    selectedPoints.stream()
                    .map(rp -> new LagrangeInterpolationService.Point(rp.getX(), rp.getDecodedY()))
                    .collect(Collectors.toList());
            
            // Calculate the secret using Lagrange interpolation
            BigDecimal secretDecimal = lagrangeInterpolationService.calculateConstantTerm(interpolationPoints);
            String secret = secretDecimal.toBigInteger().toString();
            
            // Create response points
            List<SecretResponse.DecodedPoint> responsePoints = selectedPoints.stream()
                    .map(rp -> SecretResponse.DecodedPoint.builder()
                            .x(rp.getX())
                            .y(rp.getDecodedY())
                            .originalBase(rp.getBase())
                            .originalValue(rp.getValue())
                            .build())
                    .collect(Collectors.toList());
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            // Create metadata
            SecretResponse.ProcessingMetadata metadata = SecretResponse.ProcessingMetadata.builder()
                    .baseConversionsCount(selectedPoints.size())
                    .precisionUsed("HIGH")
                    .validationsPassed(3)
                    .warnings(new ArrayList<>())
                    .build();
            
            SecretResponse response = SecretResponse.success(
                    secret,
                    secretDecimal,
                    responsePoints,
                    testCase.getKeys().getPolynomialDegree(),
                    testCase.getKeys().getN(),
                    processingTime
            );
            response.setMetadata(metadata);
            
            log.info("Successfully calculated secret: {} in {}ms", secret, processingTime);
            return response;
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Error calculating secret: {}", e.getMessage(), e);
            return SecretResponse.error(e.getMessage(), processingTime);
        }
    }
    
    /**
     * Calculates secret using alternative point selection strategy
     * Uses different combinations of k points to verify consistency
     * 
     * @param testCase the test case
     * @return the secret response
     */
    public SecretResponse calculateSecretWithVerification(TestCaseInput testCase) {
        long startTime = System.currentTimeMillis();
        
        try {
            validateTestCase(testCase);
            
            List<RootPoint> rootPoints = testCase.getRootPointsList();
            int k = testCase.getKeys().getK();
            int n = testCase.getKeys().getN();
            
            // Decode all points
            List<RootPoint> decodedPoints = rootPoints.stream()
                    .map(baseDecoderService::decodeRootPoint)
                    .collect(Collectors.toList());
            
            // Try multiple combinations and verify they give the same result
            List<String> secrets = new ArrayList<>();
            List<String> warnings = new ArrayList<>();
            
            // Test with first k points
            List<RootPoint> firstK = decodedPoints.subList(0, k);
            BigDecimal secret1 = calculateSecretFromPoints(firstK);
            secrets.add(secret1.toBigInteger().toString());
            
            // If we have extra points, test with alternative combinations
            if (n > k) {
                // Try with last k points
                List<RootPoint> lastK = decodedPoints.subList(n - k, n);
                BigDecimal secret2 = calculateSecretFromPoints(lastK);
                secrets.add(secret2.toBigInteger().toString());
                
                // Compare results
                if (!secret1.equals(secret2)) {
                    warnings.add("Different point combinations yielded different secrets - possible data inconsistency");
                    log.warn("Secret verification failed: {} vs {}", secret1, secret2);
                }
            }
            
            // Use the first calculated secret
            BigDecimal finalSecret = secret1;
            String secretString = finalSecret.toBigInteger().toString();
            
            // Create response
            List<SecretResponse.DecodedPoint> responsePoints = firstK.stream()
                    .map(rp -> SecretResponse.DecodedPoint.builder()
                            .x(rp.getX())
                            .y(rp.getDecodedY())
                            .originalBase(rp.getBase())
                            .originalValue(rp.getValue())
                            .build())
                    .collect(Collectors.toList());
            
            long processingTime = System.currentTimeMillis() - startTime;
            
            SecretResponse.ProcessingMetadata metadata = SecretResponse.ProcessingMetadata.builder()
                    .baseConversionsCount(decodedPoints.size())
                    .precisionUsed("HIGH")
                    .validationsPassed(warnings.isEmpty() ? 4 : 3)
                    .warnings(warnings)
                    .build();
            
            SecretResponse response = SecretResponse.success(
                    secretString,
                    finalSecret,
                    responsePoints,
                    testCase.getKeys().getPolynomialDegree(),
                    testCase.getKeys().getN(),
                    processingTime
            );
            response.setMetadata(metadata);
            
            log.info("Calculated secret with verification: {} in {}ms", secretString, processingTime);
            return response;
            
        } catch (Exception e) {
            long processingTime = System.currentTimeMillis() - startTime;
            log.error("Error in verified secret calculation: {}", e.getMessage(), e);
            return SecretResponse.error(e.getMessage(), processingTime);
        }
    }
    
    /**
     * Helper method to calculate secret from a list of root points
     */
    private BigDecimal calculateSecretFromPoints(List<RootPoint> points) {
        List<LagrangeInterpolationService.Point> interpolationPoints = points.stream()
                .map(rp -> new LagrangeInterpolationService.Point(rp.getX(), rp.getDecodedY()))
                .collect(Collectors.toList());
        
        return lagrangeInterpolationService.calculateConstantTerm(interpolationPoints);
    }
    
    /**
     * Validates the test case input
     */
    private void validateTestCase(TestCaseInput testCase) {
        if (testCase == null) {
            throw new IllegalArgumentException("Test case cannot be null");
        }
        
        if (testCase.getKeys() == null) {
            throw new IllegalArgumentException("Keys section is required");
        }
        
        if (!testCase.getKeys().isValid()) {
            throw new IllegalArgumentException("Invalid keys: k must be <= n and both must be positive");
        }
        
        List<RootPoint> rootPoints = testCase.getRootPointsList();
        if (rootPoints.size() != testCase.getKeys().getN()) {
            throw new IllegalArgumentException(
                String.format("Expected %d root points but found %d", 
                            testCase.getKeys().getN(), rootPoints.size()));
        }
        
        if (rootPoints.size() < testCase.getKeys().getK()) {
            throw new IllegalArgumentException(
                String.format("Need at least %d points but only have %d", 
                            testCase.getKeys().getK(), rootPoints.size()));
        }
        
        // Validate each root point
        for (RootPoint point : rootPoints) {
            if (point == null) {
                throw new IllegalArgumentException("Root point cannot be null");
            }
            if (!point.isValidValueForBase()) {
                throw new IllegalArgumentException(
                    String.format("Invalid value '%s' for base %s at x=%d", 
                                point.getValue(), point.getBase(), point.getX()));
            }
        }
    }
    
    /**
     * Gets algorithm information
     * 
     * @return algorithm details
     */
    public String getAlgorithmInfo() {
        return "Shamir's Secret Sharing with Lagrange Interpolation - " +
               "Reconstructs polynomial constant term from k out of n encoded points";
    }
}