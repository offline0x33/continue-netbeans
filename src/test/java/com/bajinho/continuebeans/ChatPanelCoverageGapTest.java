package com.bajinho.continuebeans;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Additional deterministic coverage for the canonical dark ChatPanel. */
class ChatPanelCoverageGapTest {

    private MockedStatic<ContinueSettings> settings;
    private MockedConstruction<LlmClient> clients;
    private MockedConstruction<com.bajinho.continuebeans.task.TaskOrchestrator> orchestrators;
    private LlmClient client;
    private com.bajinho.continuebeans.task.TaskOrchestrator orchestrator;
    private ChatPanel panel;

    @BeforeEach
    void setUp() {
        settings = mockStatic(ContinueSettings.class);
        settings.when(ContinueSettings::getApiUrl).thenReturn("");
        settings.when(ContinueSettings::getModel).thenReturn("test-model");
        clients = mockConstruction(LlmClient.class);
        orchestrators = mockConstruction(com.bajinho.continuebeans.task.TaskOrchestrator.class);
        panel = new ChatPanel();
        client = clients.constructed().get(0);
        orchestrator = orchestrators.constructed().get(0);
    }

    @AfterEach
    void tearDown() {
        orchestrators.close();
        clients.close();
        settings.close();
    }

    @Test
    void rejectsMissingApiUrl() throws Exception {
        settings.when(ContinueSettings::getApiUrl).thenReturn(null);
        assertFalse((Boolean) invoke("hasConfiguredApiUrl"));

        settings.when(ContinueSettings::getApiUrl).thenReturn("   ");
        assertFalse((Boolean) invoke("hasConfiguredApiUrl"));
    }

    @Test
    void rejectsMalformedAndMissingSchemeApiUrls() throws Exception {
        settings.when(ContinueSettings::getApiUrl).thenReturn("not a url");
        assertFalse((Boolean) invoke("hasConfiguredApiUrl"));

        settings.when(ContinueSettings::getApiUrl).thenReturn("localhost:1234");
        assertFalse((Boolean) invoke("hasConfiguredApiUrl"));
    }

    @Test
    void acceptsValidConfiguredApiUrl() throws Exception {
        settings.when(ContinueSettings::getApiUrl).thenReturn("http://127.0.0.1:1234/v1/chat/completions");
        assertTrue((Boolean) invoke("hasConfiguredApiUrl"));
    }

    @Test
    void containsItemCoversPresentAndAbsentValues() throws Exception {
        JComboBox<String> selector = field("modeSelector", JComboBox.class);
        assertTrue((Boolean) invoke("containsItem", "test-model"));
        selector.addItem("second-model");
        assertTrue((Boolean) invoke("containsItem", "second-model"));
        assertFalse((Boolean) invoke("containsItem", "missing-model"));
    }

    @Test
    void selectedModelFallsBackToSettingsForPlaceholders() throws Exception {
        JComboBox<String> selector = field("modeSelector", JComboBox.class);

        selector.setSelectedItem(null);
        settings.when(ContinueSettings::getModel).thenReturn("fallback-model");
        assertEquals("fallback-model", invoke("getSelectedModel"));

        selector.removeAllItems();
        selector.addItem("Loading models…");
        assertEquals("fallback-model", invoke("getSelectedModel"));

        selector.removeAllItems();
        selector.addItem("No models available");
        assertEquals("fallback-model", invoke("getSelectedModel"));

        selector.removeAllItems();
        selector.addItem("real-model");
        assertEquals("real-model", invoke("getSelectedModel"));
    }

    @Test
    void persistSelectedModelIgnoresPlaceholdersAndPersistsRealModels() throws Exception {
        JComboBox<String> selector = field("modeSelector", JComboBox.class);

        selector.removeAllItems();
        selector.addItem("Loading models…");
        selector.setSelectedItem("Loading models…");
        invoke("persistSelectedModel");
        settings.verify(() -> ContinueSettings.setModel(anyString()), never());

        selector.removeAllItems();
        selector.addItem("No models available");
        selector.setSelectedItem("No models available");
        invoke("persistSelectedModel");
        settings.verify(() -> ContinueSettings.setModel(anyString()), never());

        selector.removeAllItems();
        selector.addItem("production-model");
        selector.setSelectedItem("production-model");
        invoke("persistSelectedModel");
        settings.verify(() -> ContinueSettings.setModel("production-model"));
    }

    @Test
    void sendAvailabilityTracksTextAndProcessingState() throws Exception {
        JTextField input = field("promptInput", JTextField.class);
        JButton send = field("sendButton", JButton.class);

        input.setText("");
        invoke("updateSendAvailability");
        assertFalse(send.isEnabled());

        input.setText("hello");
        invoke("updateSendAvailability");
        assertTrue(send.isEnabled());

        setField("isProcessing", true);
        invoke("updateSendAvailability");
        assertFalse(send.isEnabled());
    }

    @Test
    void sendPromptStopsOnEmptyInput() throws Exception {
        JTextField input = field("promptInput", JTextField.class);
        input.setText("   ");
        invoke("sendPrompt");
        verifyNoInteractions(orchestrator);
    }

    @Test
    void sendPromptRequiresModel() throws Exception {
        settings.when(ContinueSettings::getModel).thenReturn("");
        JComboBox<String> selector = field("modeSelector", JComboBox.class);
        selector.removeAllItems();
        selector.addItem("No models available");
        JTextField input = field("promptInput", JTextField.class);
        input.setText("do something");

        invoke("sendPrompt");

        assertFalse((Boolean) getField("isProcessing"));
        verifyNoInteractions(orchestrator);
    }

    @Test
    void refreshModelsHandlesSuccessfulModelList() throws Exception {
        settings.when(ContinueSettings::getApiUrl).thenReturn("http://mock/api");
        settings.when(ContinueSettings::getModel).thenReturn("preferred-model");
        when(client.getModelosDisponiveisAsync())
                .thenReturn(CompletableFuture.completedFuture(List.of("alpha", "preferred-model", " ", null)));

        invoke("refreshModels");
        flushEdt();

        JComboBox<String> selector = field("modeSelector", JComboBox.class);
        assertEquals(2, selector.getItemCount());
        assertEquals("preferred-model", selector.getSelectedItem());
        settings.verify(() -> ContinueSettings.setModel(anyString()), never());
    }

    @Test
    void refreshModelsHandlesEmptyResultWithSelectedModel() throws Exception {
        settings.when(ContinueSettings::getApiUrl).thenReturn("http://mock/api");
        settings.when(ContinueSettings::getModel).thenReturn("saved-model");
        when(client.getModelosDisponiveisAsync())
                .thenReturn(CompletableFuture.completedFuture(List.of()));

        invoke("refreshModels");
        flushEdt();

        JComboBox<String> selector = field("modeSelector", JComboBox.class);
        assertEquals(1, selector.getItemCount());
        assertEquals("saved-model", selector.getSelectedItem());
    }

    @Test
    void refreshModelsHandlesEmptyResultWithoutSelectedModel() throws Exception {
        settings.when(ContinueSettings::getApiUrl).thenReturn("http://mock/api");
        settings.when(ContinueSettings::getModel).thenReturn("");
        when(client.getModelosDisponiveisAsync())
                .thenReturn(CompletableFuture.completedFuture(List.of()));

        invoke("refreshModels");
        flushEdt();

        JComboBox<String> selector = field("modeSelector", JComboBox.class);
        assertEquals(1, selector.getItemCount());
        assertEquals("No models available", selector.getSelectedItem());
    }

    @Test
    void refreshModelsHandlesProviderFailure() throws Exception {
        settings.when(ContinueSettings::getApiUrl).thenReturn("http://mock/api");
        settings.when(ContinueSettings::getModel).thenReturn("saved-model");
        when(client.getModelosDisponiveisAsync())
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("offline")));

        invoke("refreshModels");
        flushEdt();

        JComboBox<String> selector = field("modeSelector", JComboBox.class);
        assertEquals("saved-model", selector.getSelectedItem());
    }

    @Test
    void sendPromptWithMockedOrchestratorStartsProcessing() throws Exception {
        settings.when(ContinueSettings::getModel).thenReturn("test-model");
        JComboBox<String> selector = field("modeSelector", JComboBox.class);
        selector.removeAllItems();
        selector.addItem("test-model");
        selector.setSelectedItem("test-model");
        JTextField input = field("promptInput", JTextField.class);
        input.setText("corrija o pom.xml");

        when(orchestrator.executeGoal(anyString(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        invoke("sendPrompt");

        assertTrue((Boolean) getField("isProcessing"));
        assertFalse(input.isEnabled());
        assertFalse(selector.isEnabled());
        verify(orchestrator).executeGoal(eq("corrija o pom.xml"), eq("lmstudio"), any());
    }

    private Object invoke(String name, Object... args) throws Exception {
        Method target = null;
        for (Method method : ChatPanel.class.getDeclaredMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == args.length) {
                target = method;
                break;
            }
        }
        assertNotNull(target, "Method not found: " + name);
        target.setAccessible(true);
        return target.invoke(panel, args);
    }

    @SuppressWarnings("unchecked")
    private <T> T field(String name, Class<T> type) throws Exception {
        return (T) getField(name);
    }

    private Object getField(String name) throws Exception {
        Field field = ChatPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(panel);
    }

    private void setField(String name, Object value) throws Exception {
        Field field = ChatPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(panel, value);
    }

    private static void flushEdt() throws Exception {
        javax.swing.SwingUtilities.invokeAndWait(() -> { });
    }
}
