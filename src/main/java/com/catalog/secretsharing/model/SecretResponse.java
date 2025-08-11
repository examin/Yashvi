package com.catalog.secretsharing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List;

/**
 * Response model containing the calculated secret and additional metadata
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response containing the calculated secret and processing details")
public class SecretResponse {
    
    /**
     * The calculated secret (constant term of the polynomial)
     */
    @JsonProperty("secret")
    @Schema(description = "The calculated secret value (constant term)", example = "3")
    private String secret;
    
    /**
     * The secret as a precise decimal value
     */
    @JsonProperty("secretDecimal")
    @Schema(description = "Secret as precise decimal value", example = "3.0")
    private BigDecimal secretDecimal;
    
    /**
     * List of decoded points used in calculation
     */
    @JsonProperty("pointsUsed")
    @Schema(description = "Points used in the Lagrange interpolation")
    private List<DecodedPoint> pointsUsed;
    
    /**
     * The polynomial degree
     */
    @JsonProperty("polynomialDegree")
    @Schema(description = "Degree of the reconstructed polynomial", example = "2")
    private Integer polynomialDegree;
    
    /**
     * Total number of points available
     */
    @JsonProperty("totalPointsAvailable")
    @Schema(description = "Total number of points available", example = "4")
    private Integer totalPointsAvailable;
    
    /**
     * Number of points actually used
     */
    @JsonProperty("pointsUsedCount")
    @Schema(description = "Number of points used for calculation", example = "3")
    private Integer pointsUsedCount;
    
    /**
     * Processing timestamp
     */
    @JsonProperty("processedAt")
    @Schema(description = "Timestamp when the secret was calculated")
    private LocalDateTime processedAt;
    
    /**
     * Processing time in milliseconds
     */
    @JsonProperty("processingTimeMs")
    @Schema(description = "Processing time in milliseconds", example = "45")
    private Long processingTimeMs;
    
    /**
     * Algorithm used for calculation
     */
    @JsonProperty("algorithm")
    @Schema(description = "Algorithm used for calculation", example = "Lagrange Interpolation")
    private String algorithm;
    
    /**
     * Success flag
     */
    @JsonProperty("success")
    @Schema(description = "Whether the calculation was successful", example = "true")
    private Boolean success;
    
    /**
     * Error message if calculation failed
     */
    @JsonProperty("errorMessage")
    @Schema(description = "Error message if calculation failed")
    private String errorMessage;
    
    /**
     * Additional metadata
     */
    @JsonProperty("metadata")
    @Schema(description = "Additional processing metadata")
    private ProcessingMetadata metadata;
    
    /**
     * Factory method for successful response
     */
    public static SecretResponse success(String secret, BigDecimal secretDecimal, 
                                       List<DecodedPoint> pointsUsed, Integer polynomialDegree,
                                       Integer totalPointsAvailable, Long processingTimeMs) {
        return SecretResponse.builder()
                .secret(secret)
                .secretDecimal(secretDecimal)
                .pointsUsed(pointsUsed)
                .polynomialDegree(polynomialDegree)
                .totalPointsAvailable(totalPointsAvailable)
                .pointsUsedCount(pointsUsed.size())
                .processedAt(LocalDateTime.now())
                .processingTimeMs(processingTimeMs)
                .algorithm("Lagrange Interpolation")
                .success(true)
                .build();
    }
    
    /**
     * Factory method for error response
     */
    public static SecretResponse error(String errorMessage, Long processingTimeMs) {
        return SecretResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .processedAt(LocalDateTime.now())
                .processingTimeMs(processingTimeMs)
                .algorithm("Lagrange Interpolation")
                .build();
    }
    
    /**
     * Represents a decoded point used in calculation
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Decoded point used in calculation")
    public static class DecodedPoint {
        
        @JsonProperty("x")
        @Schema(description = "X-coordinate", example = "1")
        private Integer x;
        
        @JsonProperty("y")
        @Schema(description = "Decoded Y-coordinate", example = "4")
        private String y;
        
        @JsonProperty("originalBase")
        @Schema(description = "Original base of encoding", example = "10")
        private String originalBase;
        
        @JsonProperty("originalValue")
        @Schema(description = "Original encoded value", example = "4")
        private String originalValue;
    }
    
    /**
     * Additional processing metadata
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Processing metadata")
    public static class ProcessingMetadata {
        
        @JsonProperty("baseConversionsCount")
        @Schema(description = "Number of base conversions performed", example = "3")
        private Integer baseConversionsCount;
        
        @JsonProperty("precisionUsed")
        @Schema(description = "Mathematical precision used", example = "HIGH")
        private String precisionUsed;
        
        @JsonProperty("validationsPassed")
        @Schema(description = "Number of validations that passed", example = "5")
        private Integer validationsPassed;
        
        @JsonProperty("warnings")
        @Schema(description = "Any warnings during processing")
        private List<String> warnings;
    }
}