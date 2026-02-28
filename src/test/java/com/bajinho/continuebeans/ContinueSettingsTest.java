package com.bajinho.continuebeans;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ContinueSettings configuration management.
 */
class ContinueSettingsTest {

    @Test
    void testDefaultApiUrl() {
        String url = ContinueSettings.getApiUrl();
        assertNotNull(url, "Default API URL should not be null");
        // URL might be empty if not configured, just verify it's not null
        assertTrue(url != null, "Default API URL should not be null");
        if (!url.isEmpty()) {
            assertTrue(url.contains("http"), "Default API URL should be a valid HTTP URL");
        }
    }

    @Test
    void testDefaultTemperature() {
        double temp = ContinueSettings.getTemperature();
        assertTrue(temp >= 0 && temp <= 2.0, "Temperature should be between 0 and 2");
    }

    @Test
    void testDefaultModel() {
        String model = ContinueSettings.getModel();
        // Model might be empty by default, just verify it's a String
        assertNotNull(model, "Default model should not be null");
    }

    @Test
    void testApiUrlFormat() {
        String url = ContinueSettings.getApiUrl();
        // Should be a valid URL format if not empty
        if (!url.isEmpty()) {
            assertTrue(url.startsWith("http://") || url.startsWith("https://"), 
                       "API URL should start with http:// or https://");
        }
    }

    @Test
    void testTemperatureRange() {
        double temp = ContinueSettings.getTemperature();
        // Valid range for LM Studio: 0 to 2.0
        assertTrue(temp >= 0, "Temperature should not be negative");
        assertTrue(temp <= 2.0, "Temperature should not exceed 2.0");
    }

    @Test
    void testModelNameNonNull() {
        String model = ContinueSettings.getModel();
        assertNotNull(model, "Model name should not be null");
    }

    @Test
    void testConsistentApiUrl() {
        String url1 = ContinueSettings.getApiUrl();
        String url2 = ContinueSettings.getApiUrl();
        assertEquals(url1, url2, "API URL should be consistent across calls");
    }

    @Test
    void testConsistentTemperature() {
        double temp1 = ContinueSettings.getTemperature();
        double temp2 = ContinueSettings.getTemperature();
        assertEquals(temp1, temp2, "Temperature should be consistent across calls");
    }

    @Test
    void testConsistentModel() {
        String model1 = ContinueSettings.getModel();
        String model2 = ContinueSettings.getModel();
        assertEquals(model1, model2, "Model should be consistent across calls");
    }

    @Test
    void testSetAndGetApiUrl() {
        String originalUrl = ContinueSettings.getApiUrl();
        String newUrl = "http://test.local:1234/v1/chat/completions";
        
        ContinueSettings.setApiUrl(newUrl);
        assertEquals(newUrl, ContinueSettings.getApiUrl(), "setApiUrl should update the URL");
        
        // Restore original
        ContinueSettings.setApiUrl(originalUrl);
    }

    @Test
    void testSetAndGetModel() {
        String originalModel = ContinueSettings.getModel();
        String newModel = "test-model-123";
        
        ContinueSettings.setModel(newModel);
        assertEquals(newModel, ContinueSettings.getModel(), "setModel should update the model");
        
        // Restore original
        ContinueSettings.setModel(originalModel);
    }

    @Test
    void testSetAndGetTemperature() {
        double originalTemp = ContinueSettings.getTemperature();
        double newTemp = 1.5;
        
        ContinueSettings.setTemperature(newTemp);
        assertEquals(newTemp, ContinueSettings.getTemperature(), "setTemperature should update the temperature");
        
        // Restore original
        ContinueSettings.setTemperature(originalTemp);
    }
}
