package com.bajinho.continuebeans;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Additional deterministic coverage for the canonical dark ChatPanel. */
class ChatPanelCoverageGapTest {

    @Test
    void rejectsMissingApiUrl() throws Exception {
        withPanel("http://mock/api", "test-model", (panel, settings, client, orchestrator) -> {
            settings.when(ContinueSettings::getApiUrl).thenReturn(null);
            assertFalse((Boolean) invoke(panel, "hasConfiguredApiUrl"));

            settings.when(ContinueSettings::getApiUrl).thenReturn("   ");
            assertFalse((Boolean) invoke(panel, "hasConfiguredApiUrl"));
        });
    }

    @Test
    void rejectsMalformedAndMissingSchemeApiUrls() throws Exception {
        withPanel("http://mock/api", "test-model", (panel, settings, client, orchestrator) -> {
            settings.when(ContinueSettings::getApiUrl).thenReturn("not a url");
            assertFalse((Boolean) invoke(panel, "hasConfiguredApiUrl"));

            settings.when(ContinueSettings::getApiUrl).thenReturn("//localhost:1234");
            assertFalse((Boolean) invoke(panel, "hasConfiguredApiUrl"));
        });
    }

    @Test
    void acceptsValidConfiguredApiUrl() throws Exception {
        withPanel("http://mock/api", "test-model", (panel, settings, client, orchestrator) -> {
            settings.when(ContinueSettings::getApiUrl)
                    .thenReturn("http://127.0.0.1:1234/v1/chat/completions");
            assertTrue((Boolean) invoke(panel, "hasConfiguredApiUrl"));
        });
    }

    @Test
    void containsItemCoversPresentAndAbsentValues() throws Exception {
        withPanel("http://mock/api", "test-model", (panel, settings, client, orchestrator) -> {
            JComboBox<String> selector = field(panel, "modeSelector", JComboBox.class);
            assertTrue((Boolean) invoke(panel, "containsItem", "test-model"));
            selector.addItem("second-model");
            assertTrue((Boolean) invoke(panel, "containsItem", "second-model"));
            assertFalse((Boolean) invoke(panel, "containsItem", "missing-model"));
        });
    }

    @Test
    void selectedModelFallsBackToSettingsForPlaceholders() throws Exception {
        withPanel("http://mock/api", "test-model", (panel, settings, client, orchestrator) -> {
            JComboBox<String> selector = field(panel, "modeSelector", JComboBox.class);

            selector.setSelectedItem(null);
            settings.when(ContinueSettings::getModel).thenReturn("fallback-model");
            assertEquals("fallback-model", invoke(panel, "getSelectedModel"));

            selector.removeAllItems();
            selector.addItem("Loading models…");
            assertEquals("fallback-model", invoke(panel, "getSelectedModel"));

            selector.removeAllItems();
            selector.addItem("No models available");
            assertEquals("fallback-model", invoke(panel, "getSelectedModel"));

            selector.removeAllItems();
            selector.addItem("real-model");
            assertEquals("real-model", invoke(panel, "getSelectedModel"));
        });
    }

    @Test
    void persistSelectedModelIgnoresPlaceholdersAndPersistsRealModels() throws Exception {
        withPanel("http://mock/api", "test-model", (panel, settings, client, orchestrator) -> {
            JComboBox<String> selector = field(panel, "modeSelector", JComboBox.class);

            selector.removeAllItems();
            selector.addItem("Loading models…");
            selector.setSelectedItem("Loading models…");
            invoke(panel, "persistSelectedModel");
            settings.verify(() -> ContinueSettings.setModel("Loading models…"), never());

            selector.removeAllItems();
            selector.addItem("No models available");
            selector.setSelectedItem("No models available");
            invoke(panel, "persistSelectedModel");
            settings.verify(() -> ContinueSettings.setModel("No models available"), never());

            selector.removeAllItems();
            selector.addItem("production-model");
            selector.setSelectedItem("production-model");
            invoke(panel, "persistSelectedModel");
            settings.verify(() -> ContinueSettings.setModel("production-model"));
        });
    }

    @Test
    void sendAvailabilityTracksTextAndProcessingState() throws Exception {
        withPanel("http://mock/api", "test-model", (panel, settings, client, orchestrator) -> {
            JTextField input = field(panel, "promptInput", JTextField.class);
            JButton send = field(panel, "sendButton", JButton.class);

            input.setText("");
            invoke(panel, "updateSendAvailability");
            assertFalse(send.isEnabled());

            input.setText("hello");
            invoke(panel, "updateSendAvailability");
            assertTrue(send.isEnabled());

            setField(panel, "isProcessing", true);
            invoke(panel, "updateSendAvailability");
            assertFalse(send.isEnabled());
        });
    }

    @Test
    void sendPromptStopsOnEmptyInput() throws Exception {
        withPanel("http://mock/api", "test-model", (panel, settings, client, orchestrator) -> {
            JTextField input = field(panel, "promptInput", JTextField.class);
            input.setText("   ");
            invoke(panel, "sendPrompt");
            verifyNoInteractions(orchestrator);
        });
    }

    @Test
    void sendPromptRequiresModel() throws Exception {
        withPanel("http://mock/api", "", (panel, settings, client, orchestrator) -> {
            JComboBox<String> selector = field(panel, "modeSelector", JComboBox.class);
            selector.removeAllItems();
            selector.addItem("No models available");
            JTextField input = field(panel, "promptInput", JTextField.class);
            input.setText("do something");

            invoke(panel, "sendPrompt");

            assertFalse((Boolean) getField(panel, "isProcessing"));
            verifyNoInteractions(orchestrator);
        });
    }

    @Test
    void refreshModelsHandlesSuccessfulModelList() throws Exception {
        withPanel("http://mock/api", "preferred-model", (panel, settings, client, orchestrator) -> {
            when(client.getModelosDisponiveisAsync())
                    .thenReturn(CompletableFuture.completedFuture(
                            Arrays.asList("alpha", "preferred-model", " ", null)));

            invoke(panel, "refreshModels");
            flushEdt();

            JComboBox<String> selector = field(panel, "modeSelector", JComboBox.class);
            assertEquals(2, selector.getItemCount());
            assertEquals("preferred-model", selector.getSelectedItem());
            settings.verify(() -> ContinueSettings.setModel(anyString()), never());
        });
    }

    @Test
    void refreshModelsHandlesEmptyResultWithSelectedModel() throws Exception {
        withPanel("http://mock/api", "saved-model", (panel, settings, client, orchestrator) -> {
            when(client.getModelosDisponiveisAsync())
                    .thenReturn(CompletableFuture.completedFuture(List.of()));

            invoke(panel, "refreshModels");
            flushEdt();

            JComboBox<String> selector = field(panel, "modeSelector", JComboBox.class);
            assertEquals(1, selector.getItemCount());
            assertEquals("saved-model", selector.getSelectedItem());
        });
    }

    @Test
    void refreshModelsHandlesEmptyResultWithoutSelectedModel() throws Exception {
        withPanel("http://mock/api", "", (panel, settings, client, orchestrator) -> {
            when(client.getModelosDisponiveisAsync())
                    .thenReturn(CompletableFuture.completedFuture(List.of()));

            invoke(panel, "refreshModels");
            flushEdt();

            JComboBox<String> selector = field(panel, "modeSelector", JComboBox.class);
            assertEquals(1, selector.getItemCount());
            assertEquals("No models available", selector.getSelectedItem());
        });
    }

    @Test
    void refreshModelsHandlesProviderFailure() throws Exception {
        withPanel("http://mock/api", "saved-model", (panel, settings, client, orchestrator) -> {
            when(client.getModelosDisponiveisAsync())
                    .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("offline")));

            invoke(panel, "refreshModels");
            flushEdt();

            JComboBox<String> selector = field(panel, "modeSelector", JComboBox.class);
            assertEquals("saved-model", selector.getSelectedItem());
        });
    }

    @Test
    void sendPromptWithMockedOrchestratorStartsProcessing() throws Exception {
        withPanel("http://mock/api", "test-model", (panel, settings, client, orchestrator) -> {
            JComboBox<String> selector = field(panel, "modeSelector", JComboBox.class);
            selector.removeAllItems();
            selector.addItem("test-model");
            selector.setSelectedItem("test-model");
            JTextField input = field(panel, "promptInput", JTextField.class);
            input.setText("corrija o pom.xml");

            when(orchestrator.executeGoal(anyString(), anyString(), any()))
                    .thenReturn(CompletableFuture.completedFuture(null));

            invoke(panel, "sendPrompt");

            assertTrue((Boolean) getField(panel, "isProcessing"));
            assertFalse(input.isEnabled());
            assertFalse(selector.isEnabled());
            verify(orchestrator).executeGoal(eq("corrija o pom.xml"), eq("lmstudio"), any());
        });
    }

    private static void withPanel(String apiUrl, String model, TestBody body) throws Exception {
        try (MockedStatic<ContinueSettings> settings = mockStatic(ContinueSettings.class);
             MockedConstruction<LlmClient> clients = mockConstruction(LlmClient.class);
             MockedConstruction<com.bajinho.continuebeans.task.TaskOrchestrator> orchestrators =
                     mockConstruction(com.bajinho.continuebeans.task.TaskOrchestrator.class)) {
            settings.when(ContinueSettings::getApiUrl).thenReturn(apiUrl);
            settings.when(ContinueSettings::getModel).thenReturn(model);
            ChatPanel panel = new ChatPanel();
            LlmClient client = clients.constructed().get(0);
            com.bajinho.continuebeans.task.TaskOrchestrator orchestrator =
                    orchestrators.constructed().get(0);
            body.run(panel, settings, client, orchestrator);
        }
    }

    private static Object invoke(ChatPanel panel, String name, Object... args) throws Exception {
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
    private static <T> T field(ChatPanel panel, String name, Class<T> type) throws Exception {
        return (T) getField(panel, name);
    }

    private static Object getField(ChatPanel panel, String name) throws Exception {
        Field field = ChatPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(panel);
    }

    private static void setField(ChatPanel panel, String name, Object value) throws Exception {
        Field field = ChatPanel.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(panel, value);
    }

    private static void flushEdt() throws Exception {
        javax.swing.SwingUtilities.invokeAndWait(() -> { });
    }

    @FunctionalInterface
    private interface TestBody {
        void run(ChatPanel panel, MockedStatic<ContinueSettings> settings,
                 LlmClient client,
                 com.bajinho.continuebeans.task.TaskOrchestrator orchestrator) throws Exception;
    }
}
