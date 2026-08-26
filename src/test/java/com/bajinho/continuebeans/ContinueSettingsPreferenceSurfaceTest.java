package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContinueSettingsPreferenceSurfaceTest {
    private String apiUrl;
    private String model;
    private double temperature;
    private AgentMode agentMode;

    @BeforeEach
    void capturePreferences() {
        apiUrl = ContinueSettings.getApiUrl();
        model = ContinueSettings.getModel();
        temperature = ContinueSettings.getTemperature();
        agentMode = ContinueSettings.getAgentMode();
    }

    @AfterEach
    void restorePreferences() {
        ContinueSettings.setApiUrl(apiUrl);
        ContinueSettings.setModel(model);
        ContinueSettings.setTemperature(temperature);
        ContinueSettings.setAgentMode(agentMode);
    }

    @Test
    void defaultsAreNonNullAndAgentModeDefaultsToCode() {
        assertNotNull(ContinueSettings.getApiUrl());
        assertNotNull(ContinueSettings.getModel());
        assertNotNull(ContinueSettings.getAgentMode());
        assertEquals(AgentMode.CODE, AgentMode.defaultMode());
    }

    @Test
    void apiUrlRoundTrip() {
        ContinueSettings.setApiUrl("https://example.test/v1/chat/completions");
        assertEquals("https://example.test/v1/chat/completions", ContinueSettings.getApiUrl());
    }

    @Test
    void modelRoundTripAndEmptyValue() {
        ContinueSettings.setModel("llama3.2");
        assertEquals("llama3.2", ContinueSettings.getModel());
        ContinueSettings.setModel("");
        assertEquals("", ContinueSettings.getModel());
    }

    @Test
    void temperatureSupportsDocumentedBoundaryValues() {
        ContinueSettings.setTemperature(0.0);
        assertEquals(0.0, ContinueSettings.getTemperature());
        ContinueSettings.setTemperature(1.0);
        assertEquals(1.0, ContinueSettings.getTemperature());
        ContinueSettings.setTemperature(2.0);
        assertEquals(2.0, ContinueSettings.getTemperature());
    }

    @Test
    void agentModeRoundTripAndNullFallback() {
        ContinueSettings.setAgentMode(AgentMode.PLANNING);
        assertEquals(AgentMode.PLANNING, ContinueSettings.getAgentMode());
        ContinueSettings.setAgentMode(AgentMode.DOCS);
        assertEquals(AgentMode.DOCS, ContinueSettings.getAgentMode());
        ContinueSettings.setAgentMode(AgentMode.AGENT);
        assertEquals(AgentMode.AGENT, ContinueSettings.getAgentMode());
        ContinueSettings.setAgentMode(null);
        assertEquals(AgentMode.CODE, ContinueSettings.getAgentMode());
    }
}
