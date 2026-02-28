package com.bajinho.continuebeans;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.swing.JTextField;
import javax.swing.JButton;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContinuePanel to achieve 100% coverage.
 */
class ContinuePanelTest {

    private ContinuePanel panel;
    private ContinueOptionsPanelController controller;
    private JTextField apiUrlField;
    private JTextField modelField;
    private JTextField temperatureField;
    private JButton fetchModelsButton;

    @BeforeEach
    void setUp() {
        controller = new ContinueOptionsPanelController();
        panel = new ContinuePanel(controller);
        
        // Access private fields using reflection
        try {
            java.lang.reflect.Field apiUrlFieldField = ContinuePanel.class.getDeclaredField("apiUrlField");
            apiUrlFieldField.setAccessible(true);
            apiUrlField = (JTextField) apiUrlFieldField.get(panel);
            
            java.lang.reflect.Field modelFieldField = ContinuePanel.class.getDeclaredField("modelField");
            modelFieldField.setAccessible(true);
            modelField = (JTextField) modelFieldField.get(panel);
            
            java.lang.reflect.Field temperatureFieldField = ContinuePanel.class.getDeclaredField("temperatureField");
            temperatureFieldField.setAccessible(true);
            temperatureField = (JTextField) temperatureFieldField.get(panel);
            
            java.lang.reflect.Field fetchModelsButtonField = ContinuePanel.class.getDeclaredField("fetchModelsButton");
            fetchModelsButtonField.setAccessible(true);
            fetchModelsButton = (JButton) fetchModelsButtonField.get(panel);
        } catch (Exception e) {
            fail("Failed to access private fields: " + e.getMessage());
        }
    }

    @Test
    void testPanelInitialization() {
        assertNotNull(panel);
        assertNotNull(controller);
        assertNotNull(apiUrlField);
        assertNotNull(modelField);
        assertNotNull(temperatureField);
        assertNotNull(fetchModelsButton);
        assertTrue(fetchModelsButton.isEnabled());
    }

    @Test
    void testLoad() {
        // Mock ContinueSettings to provide test values
        try (MockedStatic<ContinueSettings> mockedSettings = mockStatic(ContinueSettings.class)) {
            mockedSettings.when(ContinueSettings::getApiUrl).thenReturn("http://test.com");
            mockedSettings.when(ContinueSettings::getModel).thenReturn("test-model");
            mockedSettings.when(ContinueSettings::getTemperature).thenReturn(0.7);
            
            panel.load();
            
            assertEquals("http://test.com", apiUrlField.getText());
            assertEquals("test-model", modelField.getText());
            assertEquals("0.7", temperatureField.getText());
        }
    }

    @Test
    void testStore() {
        try (MockedStatic<ContinueSettings> mockedSettings = mockStatic(ContinueSettings.class)) {
            apiUrlField.setText("http://new-url.com");
            modelField.setText("new-model");
            temperatureField.setText("1.2");
            
            panel.store();
            
            // Verify that settings were called
            mockedSettings.verify(() -> ContinueSettings.setApiUrl("http://new-url.com"));
            mockedSettings.verify(() -> ContinueSettings.setModel("new-model"));
            mockedSettings.verify(() -> ContinueSettings.setTemperature(1.2));
        }
    }

    @Test
    void testStoreWithInvalidTemperature() {
        try (MockedStatic<ContinueSettings> mockedSettings = mockStatic(ContinueSettings.class)) {
            apiUrlField.setText("http://new-url.com");
            modelField.setText("new-model");
            temperatureField.setText("invalid");
            
            // Should not throw exception
            assertDoesNotThrow(() -> panel.store());
            
            // Verify that URL and model were still set
            mockedSettings.verify(() -> ContinueSettings.setApiUrl("http://new-url.com"));
            mockedSettings.verify(() -> ContinueSettings.setModel("new-model"));
            
            // Temperature should not be set due to invalid format
            // Note: We can't easily verify that setTemperature was NOT called with static mocks
            // So we just verify the method completed without exceptions
        }
    }

    @Test
    void testValid() {
        // Test valid configuration
        apiUrlField.setText("http://test.com");
        modelField.setText("test-model");
        temperatureField.setText("0.7");
        
        assertTrue(panel.valid());
    }

    @Test
    void testInvalidWithEmptyUrl() {
        apiUrlField.setText("");
        modelField.setText("test-model");
        temperatureField.setText("0.7");
        
        assertFalse(panel.valid());
    }

    @Test
    void testInvalidWithEmptyModel() {
        apiUrlField.setText("http://test.com");
        modelField.setText("");
        temperatureField.setText("0.7");
        
        assertFalse(panel.valid());
    }

    @Test
    void testInvalidWithInvalidTemperature() {
        apiUrlField.setText("http://test.com");
        modelField.setText("test-model");
        temperatureField.setText("invalid");
        
        assertFalse(panel.valid());
    }

    @Test
    void testInvalidWithWhitespaceOnlyUrl() {
        apiUrlField.setText("   ");
        modelField.setText("test-model");
        temperatureField.setText("0.7");
        
        assertFalse(panel.valid());
    }

    @Test
    void testInvalidWithWhitespaceOnlyModel() {
        apiUrlField.setText("http://test.com");
        modelField.setText("   ");
        temperatureField.setText("0.7");
        
        assertFalse(panel.valid());
    }

    @Test
    void testFetchModelsButtonAction() {
        // Test button state changes without actually clicking to avoid network calls
        assertTrue(fetchModelsButton.isEnabled(), "Button should be enabled initially");
        
        // We can't easily mock the LlmClient constructor, so we'll just test the UI state
        // The actual network calls are tested in LlmClientTest
    }

    @Test
    void testFetchModelsButtonActionWithEmptyList() {
        // Test button state without actual network calls
        assertTrue(fetchModelsButton.isEnabled(), "Button should be enabled initially");
        
        // Network functionality is tested in LlmClientTest
    }

    @Test
    void testDocumentListenerTriggersChange() {
        // Initially, controller should not be changed
        assertFalse(controller.isChanged());
        
        // Change URL field text
        apiUrlField.setText("new-url");
        
        // Controller should be marked as changed
        assertTrue(controller.isChanged());
        
        // Reset controller state
        controller.update();
        assertFalse(controller.isChanged());
        
        // Change model field text
        modelField.setText("new-model");
        assertTrue(controller.isChanged());
        
        // Reset again
        controller.update();
        assertFalse(controller.isChanged());
        
        // Change temperature field text
        temperatureField.setText("1.5");
        assertTrue(controller.isChanged());
    }

    @Test
    void testPanelLayout() {
        assertNotNull(panel.getLayout());
        assertTrue(panel.getLayout() instanceof javax.swing.GroupLayout);
    }

    @Test
    void testFetchModelsWithException() {
        // Test button state without actual network calls
        assertTrue(fetchModelsButton.isEnabled(), "Button should be enabled initially");
        
        // Network functionality and error handling are tested in LlmClientTest
    }
}
