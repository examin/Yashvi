package com.catalog.secretsharing.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.math.MathContext;
import java.util.List;

/**
 * Service implementing Lagrange Interpolation for polynomial reconstruction
 * 
 * This service reconstructs polynomial coefficients from given points using
 * Lagrange interpolation, specifically finding the constant term (secret).
 */
@Service
@Slf4j
public class LagrangeInterpolationService {
    
    /**
     * Mathematical context for high precision calculations
     */
    private static final MathContext MATH_CONTEXT = new MathContext(50, RoundingMode.HALF_UP);
    
    /**
     * Represents a point in 2D space for interpolation
     */
    public static class Point {
        private final BigDecimal x;
        private final BigDecimal y;
        
        public Point(BigDecimal x, BigDecimal y) {
            this.x = x;
            this.y = y;
        }
        
        public Point(int x, String y) {
            this.x = new BigDecimal(x);
            this.y = new BigDecimal(y);
        }
        
        public Point(String x, String y) {
            this.x = new BigDecimal(x);
            this.y = new BigDecimal(y);
        }
        
        public BigDecimal getX() { return x; }
        public BigDecimal getY() { return y; }
        
        @Override
        public String toString() {
            return String.format("Point(x=%s, y=%s)", x.toString(), y.toString());
        }
    }
    
    /**
     * Calculates the constant term (f(0)) of a polynomial using Lagrange interpolation
     * This represents the secret in Shamir's Secret Sharing scheme
     * 
     * @param points the points to interpolate through
     * @return the constant term (secret) as BigDecimal
     * @throws IllegalArgumentException if points are invalid
     */
    public BigDecimal calculateConstantTerm(List<Point> points) {
        validatePoints(points);
        
        log.debug("Starting Lagrange interpolation with {} points", points.size());
        
        // The constant term is f(0), so we evaluate the Lagrange polynomial at x = 0
        BigDecimal secret = BigDecimal.ZERO;
        
        for (int i = 0; i < points.size(); i++) {
            Point currentPoint = points.get(i);
            
            // Calculate the Lagrange basis polynomial Li(0)
            BigDecimal basisValue = calculateLagrangeBasis(points, i, BigDecimal.ZERO);
            
            // Add yi * Li(0) to the secret
            BigDecimal contribution = currentPoint.getY().multiply(basisValue, MATH_CONTEXT);
            secret = secret.add(contribution, MATH_CONTEXT);
            
            log.debug("Point {}: x={}, y={}, basis={}, contribution={}", 
                    i, currentPoint.getX(), currentPoint.getY(), basisValue, contribution);
        }
        
        log.debug("Calculated secret (constant term): {}", secret);
        return secret;
    }
    
    /**
     * Calculates the Lagrange basis polynomial Li(x) for the i-th point
     * 
     * @param points all interpolation points
     * @param i the index of the current basis polynomial
     * @param x the x value at which to evaluate the basis polynomial
     * @return the value of Li(x)
     */
    private BigDecimal calculateLagrangeBasis(List<Point> points, int i, BigDecimal x) {
        BigDecimal result = BigDecimal.ONE;
        Point currentPoint = points.get(i);
        
        for (int j = 0; j < points.size(); j++) {
            if (i != j) {
                Point otherPoint = points.get(j);
                
                // Calculate (x - xj) / (xi - xj)
                BigDecimal numerator = x.subtract(otherPoint.getX(), MATH_CONTEXT);
                BigDecimal denominator = currentPoint.getX().subtract(otherPoint.getX(), MATH_CONTEXT);
                
                if (denominator.equals(BigDecimal.ZERO)) {
                    throw new IllegalArgumentException(
                        String.format("Duplicate x-coordinates found: x[%d] = x[%d] = %s", 
                                    i, j, currentPoint.getX()));
                }
                
                BigDecimal fraction = numerator.divide(denominator, MATH_CONTEXT);
                result = result.multiply(fraction, MATH_CONTEXT);
            }
        }
        
        return result;
    }
    
    /**
     * Evaluates the interpolated polynomial at a given x value
     * 
     * @param points the interpolation points
     * @param x the x value at which to evaluate
     * @return the y value at the given x
     */
    public BigDecimal evaluatePolynomial(List<Point> points, BigDecimal x) {
        validatePoints(points);
        
        BigDecimal result = BigDecimal.ZERO;
        
        for (int i = 0; i < points.size(); i++) {
            Point currentPoint = points.get(i);
            BigDecimal basisValue = calculateLagrangeBasis(points, i, x);
            BigDecimal contribution = currentPoint.getY().multiply(basisValue, MATH_CONTEXT);
            result = result.add(contribution, MATH_CONTEXT);
        }
        
        return result;
    }
    
    /**
     * Validates the input points for interpolation
     * 
     * @param points the points to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePoints(List<Point> points) {
        if (points == null || points.isEmpty()) {
            throw new IllegalArgumentException("Points list cannot be null or empty");
        }
        
        if (points.size() < 2) {
            throw new IllegalArgumentException("At least 2 points are required for interpolation");
        }
        
        // Check for duplicate x-coordinates
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                if (points.get(i).getX().equals(points.get(j).getX())) {
                    throw new IllegalArgumentException(
                        String.format("Duplicate x-coordinate found: %s at indices %d and %d", 
                                    points.get(i).getX(), i, j));
                }
            }
        }
        
        // Check for null points
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            if (point == null || point.getX() == null || point.getY() == null) {
                throw new IllegalArgumentException("Point at index " + i + " is null or contains null values");
            }
        }
    }
    
    /**
     * Calculates all polynomial coefficients using Lagrange interpolation
     * Note: This is computationally expensive and mainly for debugging/verification
     * 
     * @param points the interpolation points
     * @return array of coefficients [a0, a1, a2, ...] where polynomial = a0 + a1*x + a2*x^2 + ...
     */
    public BigDecimal[] calculateAllCoefficients(List<Point> points) {
        validatePoints(points);
        
        int degree = points.size() - 1;
        BigDecimal[] coefficients = new BigDecimal[degree + 1];
        
        // Initialize coefficients to zero
        for (int i = 0; i <= degree; i++) {
            coefficients[i] = BigDecimal.ZERO;
        }
        
        // For each point, calculate its contribution to each coefficient
        for (int i = 0; i < points.size(); i++) {
            Point point = points.get(i);
            BigDecimal[] termCoefficients = calculateTermCoefficients(points, i);
            
            // Add the contribution of this term to each coefficient
            for (int j = 0; j <= degree; j++) {
                BigDecimal contribution = point.getY().multiply(termCoefficients[j], MATH_CONTEXT);
                coefficients[j] = coefficients[j].add(contribution, MATH_CONTEXT);
            }
        }
        
        return coefficients;
    }
    
    /**
     * Calculates the coefficients of the i-th Lagrange basis polynomial
     * 
     * @param points all interpolation points
     * @param i the index of the basis polynomial
     * @return coefficients of Li(x)
     */
    private BigDecimal[] calculateTermCoefficients(List<Point> points, int i) {
        int degree = points.size() - 1;
        BigDecimal[] coeffs = new BigDecimal[degree + 1];
        
        // Initialize with [1] (constant polynomial 1)
        coeffs[0] = BigDecimal.ONE;
        for (int j = 1; j <= degree; j++) {
            coeffs[j] = BigDecimal.ZERO;
        }
        
        Point currentPoint = points.get(i);
        
        // For each other point, multiply by (x - xj) / (xi - xj)
        for (int j = 0; j < points.size(); j++) {
            if (i != j) {
                Point otherPoint = points.get(j);
                BigDecimal denominator = currentPoint.getX().subtract(otherPoint.getX(), MATH_CONTEXT);
                
                // Multiply current polynomial by (x - xj)
                BigDecimal[] newCoeffs = new BigDecimal[degree + 1];
                for (int k = 0; k <= degree; k++) {
                    newCoeffs[k] = BigDecimal.ZERO;
                }
                
                // Multiply by x (shift coefficients)
                for (int k = 0; k < degree; k++) {
                    newCoeffs[k + 1] = newCoeffs[k + 1].add(coeffs[k], MATH_CONTEXT);
                }
                
                // Multiply by -xj (subtract xj times each coefficient)
                BigDecimal negXj = otherPoint.getX().negate();
                for (int k = 0; k <= degree; k++) {
                    newCoeffs[k] = newCoeffs[k].add(coeffs[k].multiply(negXj, MATH_CONTEXT), MATH_CONTEXT);
                }
                
                // Divide by (xi - xj)
                for (int k = 0; k <= degree; k++) {
                    newCoeffs[k] = newCoeffs[k].divide(denominator, MATH_CONTEXT);
                }
                
                coeffs = newCoeffs;
            }
        }
        
        return coeffs;
    }
    
    /**
     * Gets the mathematical context used for calculations
     * 
     * @return the math context
     */
    public MathContext getMathContext() {
        return MATH_CONTEXT;
    }
}