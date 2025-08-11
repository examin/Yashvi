package com.catalog.secretsharing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Represents the keys section of the test case input containing n and k values
 * 
 * n: The number of roots provided in the given JSON
 * k: The minimum number of roots required to solve for the coefficients of the polynomial
 * k = m + 1, where m is the degree of the polynomial
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Keys containing polynomial parameters")
public class Keys {
    
    /**
     * The total number of polynomial roots provided
     */
    @JsonProperty("n")
    @NotNull(message = "n value is required")
    @Min(value = 1, message = "n must be at least 1")
    @Schema(description = "Total number of polynomial roots provided", example = "4", minimum = "1")
    private Integer n;
    
    /**
     * The minimum number of roots required to solve for polynomial coefficients
     * This is equal to degree + 1 of the polynomial
     */
    @JsonProperty("k")
    @NotNull(message = "k value is required")
    @Min(value = 1, message = "k must be at least 1")
    @Schema(description = "Minimum number of roots required (degree + 1)", example = "3", minimum = "1")
    private Integer k;
    
    /**
     * Validates that k <= n (can't require more points than available)
     * 
     * @return true if k <= n, false otherwise
     */
    public boolean isValid() {
        return k != null && n != null && k <= n && k > 0 && n > 0;
    }
    
    /**
     * Gets the degree of the polynomial (k - 1)
     * 
     * @return the polynomial degree
     */
    public int getPolynomialDegree() {
        return k - 1;
    }
    
    /**
     * Gets the number of extra points available beyond minimum required
     * 
     * @return n - k (extra points available)
     */
    public int getExtraPoints() {
        return n - k;
    }
    
    @Override
    public String toString() {
        return String.format("Keys{n=%d, k=%d, degree=%d}", n, k, getPolynomialDegree());
    }
}