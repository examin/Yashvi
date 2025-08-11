package com.catalog.secretsharing.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * Represents a single root point with encoded y-value and base
 * 
 * Format Example:
 * "2": {
 *     "base": "2",
 *     "value": "111"
 * }
 * 
 * This represents the point (x=2, y=decoded_value) where:
 * - x is the key (2 in this example)
 * - y is decoded from "111" in base 2, which equals 7 in decimal
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Root point with base-encoded y-value")
public class RootPoint {
    
    /**
     * The base/radix for decoding the value (2-36)
     */
    @JsonProperty("base")
    @NotBlank(message = "Base is required")
    @Pattern(regexp = "^(?:[2-9]|[1-2][0-9]|3[0-6])$", 
            message = "Base must be between 2 and 36")
    @Schema(description = "Base/radix for value encoding (2-36)", example = "10")
    private String base;
    
    /**
     * The encoded y-value in the specified base
     */
    @JsonProperty("value")
    @NotBlank(message = "Value is required")
    @Schema(description = "Encoded y-value in the specified base", example = "4")
    private String value;
    
    /**
     * The x-coordinate (will be set from the JSON key)
     */
    @Schema(description = "X-coordinate of the point", example = "1")
    private Integer x;
    
    /**
     * The decoded y-coordinate (calculated from value and base)
     */
    @Schema(description = "Decoded y-coordinate", example = "4")
    private String decodedY;
    
    /**
     * Constructor with x coordinate
     */
    public RootPoint(Integer x, String base, String value) {
        this.x = x;
        this.base = base;
        this.value = value;
    }
    
    /**
     * Gets the base as an integer
     * 
     * @return base as integer
     */
    public int getBaseAsInt() {
        return Integer.parseInt(base);
    }
    
    /**
     * Validates that the value is valid for the given base
     * 
     * @return true if valid, false otherwise
     */
    public boolean isValidValueForBase() {
        try {
            int baseInt = Integer.parseInt(base);
            if (baseInt < 2 || baseInt > 36) {
                return false;
            }
            
            // Check if all characters in value are valid for the base
            String upperValue = value.toUpperCase();
            for (char c : upperValue.toCharArray()) {
                int digitValue;
                if (Character.isDigit(c)) {
                    digitValue = c - '0';
                } else if (Character.isLetter(c)) {
                    digitValue = c - 'A' + 10;
                } else {
                    return false; // Invalid character
                }
                
                if (digitValue >= baseInt) {
                    return false; // Digit too large for base
                }
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Creates a string representation for debugging
     */
    @Override
    public String toString() {
        return String.format("RootPoint{x=%d, base=%s, value='%s', decodedY='%s'}", 
                x, base, value, decodedY);
    }
}