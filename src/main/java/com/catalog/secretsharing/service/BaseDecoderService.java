package com.catalog.secretsharing.service;

import com.catalog.secretsharing.model.RootPoint;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for decoding y-values from different number bases to decimal
 * 
 * This service handles the conversion of encoded values from various bases (2-36)
 * to decimal representation using BigInteger for handling large numbers.
 */
@Service
@Slf4j
public class BaseDecoderService {
    
    /**
     * Cache for frequently used conversions to improve performance
     */
    private final Map<String, String> conversionCache = new HashMap<>();
    
    /**
     * Decodes a value from the specified base to decimal representation
     * 
     * @param value the encoded value
     * @param base the source base (2-36)
     * @return the decoded value as a string (decimal representation)
     * @throws IllegalArgumentException if base is invalid or value is invalid for the base
     */
    public String decodeValue(String value, String base) {
        validateInputs(value, base);
        
        // Check cache first
        String cacheKey = value + ":" + base;
        if (conversionCache.containsKey(cacheKey)) {
            log.debug("Cache hit for conversion: {} from base {}", value, base);
            return conversionCache.get(cacheKey);
        }
        
        try {
            int baseInt = Integer.parseInt(base);
            
            // Validate base range
            if (baseInt < 2 || baseInt > 36) {
                throw new IllegalArgumentException("Base must be between 2 and 36, got: " + baseInt);
            }
            
            // Validate value for the base
            validateValueForBase(value, baseInt);
            
            // Convert using BigInteger to handle large numbers
            BigInteger result = new BigInteger(value.toUpperCase(), baseInt);
            String decodedValue = result.toString();
            
            // Cache the result
            conversionCache.put(cacheKey, decodedValue);
            
            log.debug("Decoded '{}' from base {} to decimal: {}", value, base, decodedValue);
            return decodedValue;
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                String.format("Failed to decode value '%s' from base %s: %s", 
                            value, base, e.getMessage()), e);
        }
    }
    
    /**
     * Decodes a RootPoint, setting its decodedY value
     * 
     * @param rootPoint the root point to decode
     * @return the same root point with decodedY set
     */
    public RootPoint decodeRootPoint(RootPoint rootPoint) {
        if (rootPoint == null) {
            throw new IllegalArgumentException("RootPoint cannot be null");
        }
        
        String decodedValue = decodeValue(rootPoint.getValue(), rootPoint.getBase());
        rootPoint.setDecodedY(decodedValue);
        
        log.debug("Decoded root point: x={}, originalValue={}, base={}, decodedY={}", 
                rootPoint.getX(), rootPoint.getValue(), rootPoint.getBase(), decodedValue);
        
        return rootPoint;
    }
    
    /**
     * Validates input parameters
     */
    private void validateInputs(String value, String base) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Value cannot be null or empty");
        }
        
        if (base == null || base.trim().isEmpty()) {
            throw new IllegalArgumentException("Base cannot be null or empty");
        }
        
        try {
            Integer.parseInt(base);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Base must be a valid integer: " + base);
        }
    }
    
    /**
     * Validates that all characters in the value are valid for the given base
     * 
     * @param value the value to validate
     * @param base the base to validate against
     */
    private void validateValueForBase(String value, int base) {
        String upperValue = value.toUpperCase();
        
        for (int i = 0; i < upperValue.length(); i++) {
            char c = upperValue.charAt(i);
            int digitValue;
            
            if (Character.isDigit(c)) {
                digitValue = c - '0';
            } else if (Character.isLetter(c) && c >= 'A' && c <= 'Z') {
                digitValue = c - 'A' + 10;
            } else {
                throw new IllegalArgumentException(
                    String.format("Invalid character '%c' at position %d in value '%s' for base %d", 
                                c, i, value, base));
            }
            
            if (digitValue >= base) {
                throw new IllegalArgumentException(
                    String.format("Character '%c' (value %d) is invalid for base %d in value '%s'", 
                                c, digitValue, base, value));
            }
        }
    }
    
    /**
     * Converts a decimal string back to a specified base (for testing/verification)
     * 
     * @param decimalValue the decimal value as string
     * @param targetBase the target base (2-36)
     * @return the value in the target base
     */
    public String encodeToBase(String decimalValue, int targetBase) {
        if (targetBase < 2 || targetBase > 36) {
            throw new IllegalArgumentException("Base must be between 2 and 36");
        }
        
        try {
            BigInteger value = new BigInteger(decimalValue);
            return value.toString(targetBase).toUpperCase();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid decimal value: " + decimalValue, e);
        }
    }
    
    /**
     * Gets the size of the conversion cache (for monitoring)
     * 
     * @return cache size
     */
    public int getCacheSize() {
        return conversionCache.size();
    }
    
    /**
     * Clears the conversion cache
     */
    public void clearCache() {
        conversionCache.clear();
        log.info("Conversion cache cleared");
    }
    
    /**
     * Gets supported base range information
     * 
     * @return map with min and max supported bases
     */
    public Map<String, Integer> getSupportedBaseRange() {
        Map<String, Integer> range = new HashMap<>();
        range.put("minBase", 2);
        range.put("maxBase", 36);
        return range;
    }
}