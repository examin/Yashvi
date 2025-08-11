package com.catalog.secretsharing.service;

import com.catalog.secretsharing.model.TestCaseInput;
import com.catalog.secretsharing.model.SecretResponse;
import com.catalog.secretsharing.model.Keys;
import com.catalog.secretsharing.model.RootPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SecretSharingService
 */
@ExtendWith(MockitoExtension.class)
class SecretSharingServiceTest {
    
    @Mock
    private BaseDecoderService baseDecoderService;
    
    @Mock
    private LagrangeInterpolationService lagrangeInterpolationService;
    
    @InjectMocks
    private SecretSharingService secretSharingService;
    
    private TestCaseInput testCase1;
    
    @BeforeEach
    void setUp() {
        // Create test case 1
        testCase1 = new TestCaseInput();
        testCase1.setKeys(new Keys(4, 3));
        testCase1.addRootPoint(1, new RootPoint(1, "10", "4"));
        testCase1.addRootPoint(2, new RootPoint(2, "2", "111"));
        testCase1.addRootPoint(3, new RootPoint(3, "10", "12"));
        testCase1.addRootPoint(6, new RootPoint(6, "4", "213"));
    }
    
    @Test
    void testCalculateSecret_Success() {
        // Arrange
        when(baseDecoderService.decodeRootPoint(any(RootPoint.class)))
            .thenAnswer(invocation -> {
                RootPoint point = invocation.getArgument(0);
                switch (point.getX()) {
                    case 1: point.setDecodedY("4"); break;
                    case 2: point.setDecodedY("7"); break;
                    case 3: point.setDecodedY("12"); break;
                    case 6: point.setDecodedY("39"); break;
                }
                return point;
            });
        
        when(lagrangeInterpolationService.calculateConstantTerm(any()))
            .thenReturn(new BigDecimal("3"));
        
        // Act
        SecretResponse response = secretSharingService.calculateSecret(testCase1);
        
        // Assert
        assertTrue(response.getSuccess());
        assertEquals("3", response.getSecret());
        assertEquals(new BigDecimal("3"), response.getSecretDecimal());
        assertEquals(Integer.valueOf(2), response.getPolynomialDegree());
        assertEquals(Integer.valueOf(4), response.getTotalPointsAvailable());
        assertEquals(Integer.valueOf(3), response.getPointsUsedCount());
        
        verify(baseDecoderService, times(4)).decodeRootPoint(any(RootPoint.class));
        verify(lagrangeInterpolationService, times(1)).calculateConstantTerm(any());
    }
    
    @Test
    void testCalculateSecret_InvalidTestCase() {
        // Arrange
        TestCaseInput invalidTestCase = new TestCaseInput();
        invalidTestCase.setKeys(new Keys(2, 3)); // k > n, invalid
        
        // Act
        SecretResponse response = secretSharingService.calculateSecret(invalidTestCase);
        
        // Assert
        assertFalse(response.getSuccess());
        assertNotNull(response.getErrorMessage());
        assertTrue(response.getErrorMessage().contains("k must be <= n"));
    }
    
    @Test
    void testCalculateSecret_NullInput() {
        // Act
        SecretResponse response = secretSharingService.calculateSecret(null);
        
        // Assert
        assertFalse(response.getSuccess());
        assertNotNull(response.getErrorMessage());
        assertTrue(response.getErrorMessage().contains("Test case cannot be null"));
    }
    
    @Test
    void testCalculateSecretWithVerification_Success() {
        // Arrange
        when(baseDecoderService.decodeRootPoint(any(RootPoint.class)))
            .thenAnswer(invocation -> {
                RootPoint point = invocation.getArgument(0);
                switch (point.getX()) {
                    case 1: point.setDecodedY("4"); break;
                    case 2: point.setDecodedY("7"); break;
                    case 3: point.setDecodedY("12"); break;
                    case 6: point.setDecodedY("39"); break;
                }
                return point;
            });
        
        when(lagrangeInterpolationService.calculateConstantTerm(any()))
            .thenReturn(new BigDecimal("3"));
        
        // Act
        SecretResponse response = secretSharingService.calculateSecretWithVerification(testCase1);
        
        // Assert
        assertTrue(response.getSuccess());
        assertEquals("3", response.getSecret());
        assertNotNull(response.getMetadata());
        assertTrue(response.getMetadata().getWarnings().isEmpty());
    }
    
    @Test
    void testCalculateSecret_BaseDecodingException() {
        // Arrange
        when(baseDecoderService.decodeRootPoint(any(RootPoint.class)))
            .thenThrow(new IllegalArgumentException("Invalid base"));
        
        // Act
        SecretResponse response = secretSharingService.calculateSecret(testCase1);
        
        // Assert
        assertFalse(response.getSuccess());
        assertNotNull(response.getErrorMessage());
        assertTrue(response.getErrorMessage().contains("Invalid base"));
    }
    
    @Test
    void testGetAlgorithmInfo() {
        // Act
        String info = secretSharingService.getAlgorithmInfo();
        
        // Assert
        assertNotNull(info);
        assertTrue(info.contains("Shamir's Secret Sharing"));
        assertTrue(info.contains("Lagrange Interpolation"));
    }
}