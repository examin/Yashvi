package com.catalog.secretsharing.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents the complete test case input JSON structure
 * 
 * Example JSON:
 * {
 *   "keys": {
 *     "n": 4,
 *     "k": 3
 *   },
 *   "1": {
 *     "base": "10",
 *     "value": "4"
 *   },
 *   "2": {
 *     "base": "2", 
 *     "value": "111"
 *   }
 *   // ... more root points
 * }
 */
@Data
@NoArgsConstructor
@Schema(description = "Complete test case input with keys and root points")
public class TestCaseInput {
    
    /**
     * The keys section containing n and k values
     */
    @JsonProperty("keys")
    @Valid
    @NotNull(message = "Keys section is required")
    @Schema(description = "Polynomial parameters (n and k values)")
    private Keys keys;
    
    /**
     * Dynamic map to store root points with their x-coordinates as keys
     * This handles the dynamic JSON structure where root points have numeric keys
     */
    @JsonIgnore
    private Map<String, RootPoint> rootPoints = new HashMap<>();
    
    /**
     * Constructor with keys
     */
    public TestCaseInput(Keys keys) {
        this.keys = keys;
        this.rootPoints = new HashMap<>();
    }
    
    /**
     * Getter for Jackson to serialize the dynamic root points
     * 
     * @return map of root points
     */
    @JsonAnyGetter
    public Map<String, RootPoint> getRootPointsMap() {
        return rootPoints;
    }
    
    /**
     * Setter for Jackson to deserialize dynamic root points
     * Handles any JSON property that is not "keys"
     * 
     * @param key the x-coordinate as string
     * @param value the root point data
     */
    @JsonAnySetter
    public void setRootPoint(String key, RootPoint value) {
        if (!"keys".equals(key)) {
            try {
                // Parse the key as x-coordinate
                Integer x = Integer.parseInt(key);
                value.setX(x);
                rootPoints.put(key, value);
            } catch (NumberFormatException e) {
                // Ignore invalid keys
                System.err.println("Invalid root point key: " + key);
            }
        }
    }
    
    /**
     * Gets all root points as a list, sorted by x-coordinate
     * 
     * @return sorted list of root points
     */
    public List<RootPoint> getRootPointsList() {
        return rootPoints.entrySet().stream()
                .map(entry -> {
                    RootPoint point = entry.getValue();
                    if (point.getX() == null) {
                        point.setX(Integer.parseInt(entry.getKey()));
                    }
                    return point;
                })
                .sorted((p1, p2) -> Integer.compare(p1.getX(), p2.getX()))
                .toList();
    }
    
    /**
     * Gets a specific root point by x-coordinate
     * 
     * @param x the x-coordinate
     * @return the root point or null if not found
     */
    public RootPoint getRootPoint(int x) {
        return rootPoints.get(String.valueOf(x));
    }
    
    /**
     * Adds a root point
     * 
     * @param x the x-coordinate
     * @param rootPoint the root point
     */
    public void addRootPoint(int x, RootPoint rootPoint) {
        rootPoint.setX(x);
        rootPoints.put(String.valueOf(x), rootPoint);
    }
    
    /**
     * Validates the test case input
     * 
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        if (keys == null || !keys.isValid()) {
            return false;
        }
        
        if (rootPoints.size() != keys.getN()) {
            return false;
        }
        
        // Validate each root point
        for (RootPoint point : rootPoints.values()) {
            if (point == null || !point.isValidValueForBase()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Gets the number of root points
     * 
     * @return number of root points
     */
    public int getRootPointCount() {
        return rootPoints.size();
    }
    
    @Override
    public String toString() {
        return String.format("TestCaseInput{keys=%s, rootPointsCount=%d}", 
                keys, rootPoints.size());
    }
}