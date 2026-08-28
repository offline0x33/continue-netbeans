package com.bajinho.continuebeans;

import com.bajinho.continuebeans.task.AgentTask;
import com.bajinho.continuebeans.task.TaskOrchestrator;
import com.bajinho.continuebeans.task.TaskPlan;
import com.bajinho.continuebeans.task.TaskStatus;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Remaining behavioral branches for the canonical dark ChatPanel (#88). */
class ChatPanelIssue88CoverageTest {

    @Test
    void refreshModelsSelectsFirstModelWhenSavedModelIsAbsent() throws Exception {
        withPanel("", "saved-model", (panel, client, orchestrator) -> {
            ContinueSettings.setApiUrl("http://mock/api");
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
    void refreshModelsTreatsNullModelsAsEmpty() throws Exception {
        withPanel("", "", (panel, client, orchestrator) -> {
            ContinueSettings.setApiUrl("http://mock/api");
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
    void successfulTaskLifecycleExercisesAllSuccessCallbacks() throws Exception {
        withPanel("", "test-model", (panel, client, orchestrator) -> {
            JTextField input = field(panel, "promptInput", JTextField.class);
            input.setText("refactor src/Main.java");
            AtomicReference<TaskOrchestrator.Listener> listener = new AtomicReference<>();
            when(orchestrator.executeGoal(anyString(), anyString(), any(TaskOrchestrator.Listener.class)))
                    .thenAnswer(invocation -> {
                        listener.set(invocation.getArgument(2));
                        return new CompletableFuture<>();
                    });
            invoke(panel, "sendPrompt");
            assertTrue((Boolean) getField(panel, "isProcessing"));
            AgentTask task = new AgentTask("Write src/Main.java", "write", "done", List.of());
            TaskPlan plan = new TaskPlan("refactor", List.of(task));
            TaskOrchestrator.Listener callbacks = listener.get();
            assertNotNull(callbacks);
            callbacks.onPlanCreated(plan);
            flushEdt();
            task.start();
            callbacks.onTaskStarted(task);
            flushEdt();
            task.verifying("+2 -1\nsrc/Main.java\n");
            callbacks.onTaskVerifying(task);
            flushEdt();
            task.complete("+2 -1\nsrc/Main.java\nnew file\npublic class Main {}\n");
            callbacks.onTaskCompleted(task);
            flushEdt();
            callbacks.onCompleted(plan);
            flushEdt();
            assertFalse((Boolean) getField(panel, "isProcessing"));
            assertEquals("Completed", ((JLabel) getField(panel, "statusLabel")).getText());
            assertNotNull(getField(panel, "activePlan"));
        });
    }

    @Test
    void failedTaskLifecycleExercisesFailureAndReplanningCallbacks() throws Exception {
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
                    List.of(new AgentTask("run tests", "execute", "pass", List.of())));
            TaskOrchestrator.Listener callbacks = listener.get();
            AgentTask task = plan.getTasks().get(0);
            task.fail("compiler failure");
            callbacks.onTaskFailed(task);
            flushEdt();
            callbacks.onReplanning(plan);
            flushEdt();
            callbacks.onFailed("Unable to complete", plan);
            flushEdt();
            assertFalse((Boolean) getField(panel, "isProcessing"));
            assertEquals("Failed", ((JLabel) getField(panel, "statusLabel")).getText());
        });
    }

    @Test
    void rendererVariantsCoverEveryTaskStatusAndActivityKind() throws Exception {
        withPanel("", "test-model", (panel, client, orchestrator) -> {
            for (String kind : List.of("File", "Tool", "Verify", "Planner", "Error", "Other")) {
                invoke(panel, "appendActivity", kind, "payload", Color.BLUE);
                assertTrue(((JPanel) getField(panel, "conversationPanel")).getComponentCount() > 0);
            }

            for (TaskStatus status : TaskStatus.values()) {
                AgentTask task = new AgentTask("task-" + status, "instruction", "criteria", List.of());
                switch (status) {
                    case RUNNING:
                        task.start();
                        break;
                    case VERIFYING:
                        task.verifying("result");
                        break;
                    case DONE:
                        task.complete("result");
                        break;
                    case FAILED:
                        task.fail("failure");
                        break;
                    case BLOCKED:
                        task.block("blocked");
                        break;
                    default:
                        break;
                }
                assertNotNull(invoke(panel, "taskIcon", status));
                assertNotNull(invoke(panel, "taskColor", status));
                invoke(panel, "taskRow", task);
            }
            invoke(panel, "appendWarning", "warning <message>");
        });
    }

    @Test
    void parsingEscapingAndResultRenderingCoverFallbacks() throws Exception {
        withPanel("", "test-model", (panel, client, orchestrator) -> {
            assertEquals("&lt;x&gt; &amp; y", invokeStatic("escape", "<x> & y"));
            assertEquals("", invokeStatic("escape", (Object) null));
            assertEquals("src/Main.java", invoke(panel, "extractFileReference", "title", "changed src/Main.java"));
            assertEquals("title", invoke(panel, "extractFileReference", "title", "no file here"));
            Object explicit = invokeStatic("from", "+4 -2\nnew file\n");
            assertEquals(4, inner(explicit, "added"));
            assertEquals(2, inner(explicit, "removed"));
            assertTrue((Boolean) inner(explicit, "newFile"));
            Object fallback = invokeStatic("from", "plain\n+one\n+two\n-three\n");
            assertEquals(2, inner(fallback, "added"));
            assertEquals(1, inner(fallback, "removed"));
            invoke(panel, "appendCodeResult", "Result.java", "+4 -2\nclass Result {}\n");
            invoke(panel, "appendCodeResult", "Result.java", "plain\n+one\n-two\n");
        });
    }

    @Test
    void resetClearDetailsAndActionPathsAreObservable() throws Exception {
        withPanel("", "test-model", (panel, client, orchestrator) -> {
            setField(panel, "lastTitle", "Result.java");
            setField(panel, "lastResult", "result");
            invoke(panel, "showDetails");
            assertTrue(((JLabel) getField(panel, "statusLabel")).getText().startsWith("Response details:"));
            setField(panel, "lastResult", "");
            invoke(panel, "copyLastResult");
            assertEquals("Nothing to copy", ((JLabel) getField(panel, "statusLabel")).getText());
            invoke(panel, "appendActions");
            invoke(panel, "rebuildTaskPanel");
            invoke(panel, "refreshConversation");
            setField(panel, "isProcessing", true);
            invoke(panel, "resetInputState");
            assertFalse((Boolean) getField(panel, "isProcessing"));
            invoke(panel, "clearChat");
            assertNull(getField(panel, "activePlan"));
            assertEquals("", getField(panel, "lastResult"));
            assertEquals("", getField(panel, "lastTitle"));
        });
    }

    @Test
    void controlFactoriesInstallHoverBehaviorAndSendAvailability() throws Exception {
        withPanel("", "test-model", (panel, client, orchestrator) -> {
            JButton small = (JButton) invoke(panel, "smallControl", "x");
            JButton icon = (JButton) invoke(panel, "iconButton", "x");
            JButton round = (JButton) invoke(panel, "roundButton", "x");
            assertTrue(small.getMouseListeners().length > 0);
            assertTrue(icon.getMouseListeners().length > 0);
            assertTrue(round.getMouseListeners().length > 0);
            JPanel rounded = (JPanel) invoke(panel, "roundedPanel", Color.BLACK, Color.WHITE, 8);
            assertNotNull(rounded.getBorder());
            assertNotNull(invokeStatic("roundedBorder", Color.WHITE, 8));
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

    private static void withPanel(String apiUrl, String model, TestBody body) throws Exception {
        String originalApiUrl = ContinueSettings.getApiUrl();
        String originalModel = ContinueSettings.getModel();
        try (MockedConstruction<LlmClient> clients = mockConstruction(LlmClient.class);
             MockedConstruction<TaskOrchestrator> orchestrators = mockConstruction(TaskOrchestrator.class)) {
            ContinueSettings.setApiUrl(apiUrl);
            ContinueSettings.setModel(model);
            ChatPanel panel = new ChatPanel();
            body.run(panel, clients.constructed().get(0), orchestrators.constructed().get(0));
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
        Class<?>[] types = {ChatPanel.class, Class.forName("com.bajinho.continuebeans.ChatPanel$DiffStats")};
        for (Class<?> type : types) {
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

    private static Object inner(Object target, String name) throws Exception {
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
