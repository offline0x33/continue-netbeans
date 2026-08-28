package com.bajinho.continuebeans;

import com.bajinho.continuebeans.task.AgentTask;
import com.bajinho.continuebeans.task.TaskOrchestrator;
import com.bajinho.continuebeans.task.TaskPlan;
import com.bajinho.continuebeans.task.TaskStatus;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Behavioral coverage for the canonical dark ChatPanel. */
class ChatPanelBehaviorCoverageTest {

    @Test
    void refreshModelsSelectsFirstAvailableModelWhenSavedModelIsMissing() throws Exception {
        withPanel("http://mock/api", "missing-model", (panel, client, orchestrator) -> {
            when(client.getModelosDisponiveisAsync())
                    .thenReturn(CompletableFuture.completedFuture(List.of("alpha", "beta")));

            invoke(panel, "refreshModels");
            flushEdt();

            JComboBox<String> selector = field(panel, "modeSelector", JComboBox.class);
            assertEquals("alpha", selector.getSelectedItem());
            assertEquals("alpha", ContinueSettings.getModel());
        });
    }

    @Test
    void refreshModelsTreatsNullResultAsNoModels() throws Exception {
        withPanel("http://mock/api", "", (panel, client, orchestrator) -> {
            when(client.getModelosDisponiveisAsync())
                    .thenReturn(CompletableFuture.completedFuture(null));

            invoke(panel, "refreshModels");
            flushEdt();

            JComboBox<String> selector = field(panel, "modeSelector", JComboBox.class);
            assertEquals(1, selector.getItemCount());
            assertEquals("No models available", selector.getSelectedItem());
        });
    }

    @Test
    void sendPromptLifecycleRendersPlanTaskResultsAndCompletion() throws Exception {
        withPanel("", "test-model", (panel, client, orchestrator) -> {
            JTextField input = field(panel, "promptInput", JTextField.class);
            JComboBox<String> selector = field(panel, "modeSelector", JComboBox.class);
            input.setText("refactor src/Main.java");
            selector.setSelectedItem("test-model");

            AtomicReference<TaskOrchestrator.Listener> listener = new AtomicReference<>();
            when(orchestrator.executeGoal(anyString(), anyString(), any(TaskOrchestrator.Listener.class)))
                    .thenAnswer(invocation -> {
                        listener.set(invocation.getArgument(2));
                        return new CompletableFuture<>();
                    });

            invoke(panel, "sendPrompt");
            assertTrue((Boolean) getField(panel, "isProcessing"));

            AgentTask task = new AgentTask("Write src/Main.java", "write file", "file exists", List.of());
            TaskPlan plan = new TaskPlan("refactor", List.of(task));
            TaskOrchestrator.Listener callbacks = listener.get();
            assertNotNull(callbacks);

            callbacks.onPlanCreated(plan);
            flushEdt();
            callbacks.onTaskStarted(task);
            flushEdt();
            callbacks.onTaskVerifying(task);
            flushEdt();
            task.complete("+3 -1\nsrc/Main.java\nnew file\npublic class Main {}");
            callbacks.onTaskCompleted(task);
            flushEdt();
            callbacks.onCompleted(plan);
            flushEdt();

            assertFalse((Boolean) getField(panel, "isProcessing"));
            assertTrue((Boolean) invoke(panel, "containsItem", "test-model"));
            JPanel conversation = field(panel, "conversationPanel", JPanel.class);
            assertTrue(conversation.getComponentCount() > 2);
        });
    }

    @Test
    void sendPromptLifecycleRendersFailureAndReplanning() throws Exception {
        withPanel("", "test-model", (panel, client, orchestrator) -> {
            JTextField input = field(panel, "promptInput", JTextField.class);
            input.setText("run tests");

            AtomicReference<TaskOrchestrator.Listener> listener = new AtomicReference<>();
            when(orchestrator.executeGoal(anyString(), anyString(), any(TaskOrchestrator.Listener.class)))
                    .thenAnswer(invocation -> {
                        listener.set(invocation.getArgument(2));
                        return new CompletableFuture<>();
                    });

            invoke(panel, "sendPrompt");
            TaskPlan plan = new TaskPlan("run tests",
                    List.of(new AgentTask("run tests", "execute", "tests pass", List.of())));
            TaskOrchestrator.Listener callbacks = listener.get();

            AgentTask failed = plan.getTasks().get(0);
            failed.fail("compiler failure");
            callbacks.onTaskFailed(failed);
            flushEdt();
            callbacks.onReplanning(plan);
            flushEdt();
            callbacks.onFailed("Unable to complete objective", plan);
            flushEdt();

            assertFalse((Boolean) getField(panel, "isProcessing"));
            assertEquals("Failed", ((javax.swing.JLabel) getField(panel, "statusLabel")).getText());
        });
    }

    @Test
    void renderersCoverActivityTaskAndWarningVariants() throws Exception {
        withPanel("", "test-model", (panel, client, orchestrator) -> {
            for (String kind : List.of("File", "Tool", "Verify", "Planner", "Error", "Other")) {
                invoke(panel, "appendActivity", kind, "payload", Color.BLUE);
            }
            for (TaskStatus status : TaskStatus.values()) {
                AgentTask task = new AgentTask("task-" + status, "do", "done", List.of());
                switch (status) {
                    case RUNNING -> task.start();
                    case VERIFYING -> task.verifying("result");
                    case DONE -> task.complete("result");
                    case FAILED -> task.fail("failure");
                    case BLOCKED -> task.block("blocked");
                    default -> { }
                }
                invoke(panel, "taskIcon", status);
                invoke(panel, "taskColor", status);
                invoke(panel, "taskRow", task);
            }
            invoke(panel, "appendWarning", "warning <message>");

            JPanel conversation = field(panel, "conversationPanel", JPanel.class);
            assertTrue(conversation.getComponentCount() > 10);
        });
    }

    @Test
    void diffAndTextUtilitiesCoverNormalFallbackAndEscaping() throws Exception {
        withPanel("", "test-model", (panel, client, orchestrator) -> {
            assertEquals("&lt;tag&gt; &amp; value", invokeStatic("escape", "<tag> & value"));
            assertEquals("", invokeStatic("escape", (Object) null));

            assertEquals("▱", invoke(panel, "activityIcon", "File"));
            assertEquals("•", invoke(panel, "activityIcon", "unknown"));
            assertEquals("src/Main.java", invoke(panel, "extractFileReference", "title", "changed src/Main.java"));
            assertEquals("fallback", invoke(panel, "extractFileReference", "fallback", "no-file-reference"));

            Object stats = invokeStatic("from", "@@ -1 +1 @@\n+one\n-two");
            assertEquals(1, getInnerField(stats, "added"));
            assertEquals(1, getInnerField(stats, "removed"));
            assertTrue((Boolean) getInnerField(stats, "newFile"));

            Object fallbackStats = invokeStatic("from", "plain\n+line-one\n+line-two\n-line-three");
            assertEquals(2, getInnerField(fallbackStats, "added"));
            assertEquals(1, getInnerField(fallbackStats, "removed"));

            invoke(panel, "appendCodeResult", "Result.java", "+2 -1\npublic class Result {}\n");
            invoke(panel, "appendCodeResult", "Result.java", "plain\n+one\n-two\n");
            invoke(panel, "appendMessage", "You", "hello <world>", Color.WHITE);
            invoke(panel, "appendThought", "Planning", "details");
            invoke(panel, "appendTaskPanel");
        });
    }

    @Test
    void inputResetAndClearChatRestoreInitialState() throws Exception {
        withPanel("", "test-model", (panel, client, orchestrator) -> {
            setField(panel, "isProcessing", true);
            JTextField input = field(panel, "promptInput", JTextField.class);
            JButton send = field(panel, "sendButton", JButton.class);
            JComboBox<String> selector = field(panel, "modeSelector", JComboBox.class);
            input.setEnabled(false);
            send.setEnabled(false);
            selector.setEnabled(false);

            invoke(panel, "resetInputState");
            assertFalse((Boolean) getField(panel, "isProcessing"));
            assertTrue(input.isEnabled());
            assertTrue(send.isEnabled());
            assertTrue(selector.isEnabled());

            setField(panel, "lastResult", "something");
            setField(panel, "lastTitle", "Result.java");
            invoke(panel, "clearChat");

            assertEquals("", getField(panel, "lastResult"));
            assertEquals("", getField(panel, "lastTitle"));
            assertNull(getField(panel, "activePlan"));
            assertEquals("Ready", ((javax.swing.JLabel) getField(panel, "statusLabel")).getText());
        });
    }

    @Test
    void detailAndEmptyCopyPathsUpdateStatusWithoutExternalSideEffects() throws Exception {
        withPanel("", "test-model", (panel, client, orchestrator) -> {
            invoke(panel, "showDetails");
            assertTrue(((javax.swing.JLabel) getField(panel, "statusLabel")).getText().startsWith("Response details:"));

            invoke(panel, "copyLastResult");
            assertEquals("Nothing to copy", ((javax.swing.JLabel) getField(panel, "statusLabel")).getText());

            invoke(panel, "appendActions");
            invoke(panel, "rebuildTaskPanel");
            invoke(panel, "refreshConversation");
        });
    }

    @Test
    void roundedControlsAndHoverListenersAreCreated() throws Exception {
        withPanel("", "test-model", (panel, client, orchestrator) -> {
            JButton small = (JButton) invoke(panel, "smallControl", "x");
            JButton icon = (JButton) invoke(panel, "iconButton", "x");
            JButton round = (JButton) invoke(panel, "roundButton", "x");
            assertTrue(small.getMouseListeners().length > 0);
            assertTrue(icon.getMouseListeners().length > 0);
            assertTrue(round.getMouseListeners().length > 0);
            invoke(panel, "installHover", small, Color.BLACK, Color.WHITE);
            JPanel rounded = (JPanel) invoke(panel, "roundedPanel", Color.BLACK, Color.WHITE, 8);
            assertNotNull(rounded.getBorder());
            assertNotNull(invokeStatic("roundedBorder", Color.WHITE, 8));
        });
    }

    private static void withPanel(String apiUrl, String model, TestBody body) throws Exception {
        String originalApiUrl = ContinueSettings.getApiUrl();
        String originalModel = ContinueSettings.getModel();
        try (MockedConstruction<LlmClient> clients = mockConstruction(LlmClient.class);
             MockedConstruction<TaskOrchestrator> orchestrators = mockConstruction(TaskOrchestrator.class)) {
            ContinueSettings.setApiUrl(apiUrl);
            ContinueSettings.setModel(model);
            ChatPanel panel = new ChatPanel();
            LlmClient client = clients.constructed().get(0);
            TaskOrchestrator orchestrator = orchestrators.constructed().get(0);
            body.run(panel, client, orchestrator);
        } finally {
            ContinueSettings.setApiUrl(originalApiUrl);
            ContinueSettings.setModel(originalModel);
        }
    }

    private static Object invoke(ChatPanel panel, String name, Object... args) throws Exception {
        for (Method method : ChatPanel.class.getDeclaredMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == args.length) {
                method.setAccessible(true);
                return method.invoke(panel, args);
            }
        }
        fail("Method not found: " + name);
        return null;
    }

    private static Object invokeStatic(String name, Object... args) throws Exception {
        for (Class<?> type : List.of(ChatPanel.class, Class.forName("com.bajinho.continuebeans.ChatPanel$DiffStats"))) {
            for (Method method : type.getDeclaredMethods()) {
                if (method.getName().equals(name) && java.lang.reflect.Modifier.isStatic(method.getModifiers())
                        && method.getParameterCount() == args.length) {
                    method.setAccessible(true);
                    return method.invoke(null, args);
                }
            }
        }
        fail("Static method not found: " + name);
        return null;
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

    private static Object getInnerField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
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
        void run(ChatPanel panel, LlmClient client, TaskOrchestrator orchestrator) throws Exception;
    }
}
