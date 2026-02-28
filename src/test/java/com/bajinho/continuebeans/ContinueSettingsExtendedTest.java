package com.bajinho.continuebeans;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extended tests for ContinueSettings to achieve 100% coverage.
 */
class ContinueSettingsExtendedTest {

    @BeforeEach
    void setUp() {
        ContinueSettings.setApiUrl("http://127.0.0.1:1234/v1/chat/completions");
        ContinueSettings.setModel("default-model");
        ContinueSettings.setTemperature(0.7);
    }

    @Test
    void testSetAndGetApiUrlMultipleTimes() {
        ContinueSettings.setApiUrl("http://localhost:8080");
        assertEquals("http://localhost:8080", ContinueSettings.getApiUrl());
        
        ContinueSettings.setApiUrl("http://example.com:9090");
        assertEquals("http://example.com:9090", ContinueSettings.getApiUrl());
    }

    @Test
    void testSetAndGetModelMultipleTimes() {
        ContinueSettings.setModel("gpt-4");
        assertEquals("gpt-4", ContinueSettings.getModel());
        
        ContinueSettings.setModel("llama3");
        assertEquals("llama3", ContinueSettings.getModel());
    }

    @Test
    void testSetAndGetTemperatureMultipleTimes() {
        ContinueSettings.setTemperature(0.5);
        assertEquals(0.5, ContinueSettings.getTemperature());
        
        ContinueSettings.setTemperature(0.9);
        assertEquals(0.9, ContinueSettings.getTemperature());
    }

    @Test
    void testSetTemperatureLow() {
        ContinueSettings.setTemperature(0.0);
        assertEquals(0.0, ContinueSettings.getTemperature());
    }

    @Test
    void testSetTemperatureHigh() {
        ContinueSettings.setTemperature(1.0);
        assertEquals(1.0, ContinueSettings.getTemperature());
    }

    @Test
    void testSetTemperatureEdgeValues() {
        ContinueSettings.setTemperature(0.1);
        assertEquals(0.1, ContinueSettings.getTemperature());
        
        ContinueSettings.setTemperature(0.99);
        assertEquals(0.99, ContinueSettings.getTemperature());
    }

    @Test
    void testSetApiUrlWithDifferentPorts() {
        ContinueSettings.setApiUrl("http://localhost:1234");
        assertTrue(ContinueSettings.getApiUrl().contains("1234"));
        
        ContinueSettings.setApiUrl("http://localhost:5000");
        assertTrue(ContinueSettings.getApiUrl().contains("5000"));
    }

    @Test
    void testSetApiUrlWithPath() {
        ContinueSettings.setApiUrl("http://localhost:1234/api/chat");
        assertTrue(ContinueSettings.getApiUrl().contains("api/chat"));
    }

    @Test
    void testSetApiUrlHttps() {
        ContinueSettings.setApiUrl("https://localhost:1234");
        assertTrue(ContinueSettings.getApiUrl().contains("https"));
    }

    @Test
    void testSetModelWithSpecialCharacters() {
        ContinueSettings.setModel("model-v2.0");
        assertEquals("model-v2.0", ContinueSettings.getModel());
    }

    @Test
    void testSetModelEmpty() {
        ContinueSettings.setModel("");
        assertEquals("", ContinueSettings.getModel());
    }

    @Test
    void testSetModelNull() {
        assertThrows(NullPointerException.class, () -> {
            ContinueSettings.setModel(null);
        });
    }

    @Test
    void testSetApiUrlEmpty() {
        ContinueSettings.setApiUrl("");
        assertEquals("", ContinueSettings.getApiUrl());
    }

    @Test
    void testSetApiUrlNull() {
        assertThrows(NullPointerException.class, () -> {
            ContinueSettings.setApiUrl(null);
        });
    }

    @Test
    void testConsistencyAfterMultipleOperations() {
        ContinueSettings.setApiUrl("http://test1:1234");
        ContinueSettings.setModel("model1");
        ContinueSettings.setTemperature(0.5);
        
        assertEquals("http://test1:1234", ContinueSettings.getApiUrl());
        assertEquals("model1", ContinueSettings.getModel());
        assertEquals(0.5, ContinueSettings.getTemperature());
    }

    @Test
    void testIndependenceOfSettings() {
        ContinueSettings.setApiUrl("http://test:1234");
        ContinueSettings.setModel("model1");
        ContinueSettings.setTemperature(0.7);
        
        ContinueSettings.setModel("model2");
        
        assertEquals("http://test:1234", ContinueSettings.getApiUrl());
        assertEquals(0.7, ContinueSettings.getTemperature());
        assertEquals("model2", ContinueSettings.getModel());
    }
}
