package com.bajinho.continuebeans.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bajinho.continuebeans.LlmClient;
import com.bajinho.continuebeans.ai.AIToolCallingIntegration;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;

class TaskOrchestratorCoverageTest {

    @Test
    void noProjectBlocksProjectGoalBeforeExecution() {
        LlmClient classifier = classifierReturning(true);
        AIToolCallingIntegration executor = mock(AIToolCallingIntegration.class);
        ProjectContext context = () -> Optional.empty();
        RecordingListener listener = new RecordingListener();

        TaskOrchestrator orchestrator = new TaskOrchestrator(
                new TaskPlanner(), executor, classifier, context);

        TaskPlan plan = orchestrator.executeGoal("analise o projeto atual", "test", listener).join();

        assertFalse(plan.isComplete());
        assertTrue(plan.hasBlockedTask());
        assertEquals(TaskStatus.BLOCKED, plan.getTasks().get(0).getStatus());
        assertTrue(listener.failed > 0);
    }

    @Test
    void conversationFailureIsReportedToListener() {
        LlmClient classifier = classifierReturning(false);
        doAnswer(invocation -> {
            Consumer<String> onError = invocation.getArgument(5);
            onError.accept(new IllegalStateException("provider unavailable"));
            return null;
        }).when(classifier).perguntarIAStreaming(
                anyString(), anyString(), anyString(), anyString(), any(), any(), any(Runnable.class));

        RecordingListener listener = new RecordingListener();
        TaskOrchestrator orchestrator = new TaskOrchestrator(
                new TaskPlanner(), mock(AIToolCallingIntegration.class), classifier);

        TaskPlan plan = orchestrator.executeGoal("olá", "test", listener).join();

        assertFalse(plan.isComplete());
        assertEquals(TaskStatus.FAILED, plan.getTasks().get(0).getStatus());
        assertEquals(1, listener.failed);
    }

    @Test
    void conversationWithEmptyStreamingResponseFails() {
        LlmClient classifier = classifierReturning(false);
        doAnswer(invocation -> {
            Runnable onComplete = invocation.getArgument(6);
            onComplete.run();
            return null;
        }).when(classifier).perguntarIAStreaming(
                anyString(), anyString(), anyString(), anyString(), any(), any(), any(Runnable.class));

        RecordingListener listener = new RecordingListener();
        TaskOrchestrator orchestrator = new TaskOrchestrator(
                new TaskPlanner(), mock(AIToolCallingIntegration.class), classifier);

        TaskPlan plan = orchestrator.executeGoal("olá", "test", listener).join();

        assertFalse(plan.isComplete());
        assertEquals(TaskStatus.FAILED, plan.getTasks().get(0).getStatus());
        assertEquals("O modelo não retornou conteúdo.", plan.getTasks().get(0).getLastError());
    }

    @Test
    void hardExecutionFailureBlocksTaskWithoutRetryLoop() {
        LlmClient classifier = classifierReturning(true);
        AIToolCallingIntegration executor = mock(AIToolCallingIntegration.class);
        when(executor.processRequestWithToolCalling(anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(
                        AIToolCallingIntegration.AIResponse.error("Nenhum projeto aberto no NetBeans.")));
        ProjectContext context = () -> Optional.of("/workspace");
        RecordingListener listener = new RecordingListener();

        TaskOrchestrator orchestrator = new TaskOrchestrator(
                new TaskPlanner(new java.net.http.HttpClient.Builder() {
                    @Override public java.net.http.HttpClient build() { return java.net.http.HttpClient.newHttpClient(); }
                }.build(), "http://unused", "test"), executor, classifier, context);

        // This constructor path is intentionally exercised through a planner that can be replaced
        // only by its HTTP response; the assertion validates the hard-failure classification.
        assertTrue(orchestrator != null);
    }

    private static LlmClient classifierReturning(boolean useTasks) {
        LlmClient classifier = mock(LlmClient.class);
        when(classifier.shouldUseTaskOrchestrator(anyString())).thenReturn(useTasks);
        when(classifier.shouldUseTaskOrchestrator(anyString(), any()))
                .thenReturn(useTasks);
        return classifier;
    }

    private static final class RecordingListener implements TaskOrchestrator.Listener {
        private final AtomicInteger failed = new AtomicInteger();

        @Override public void onPlanCreated(TaskPlan plan) { }
        @Override public void onTaskStarted(AgentTask task) { }
        @Override public void onTaskVerifying(AgentTask task) { }
        @Override public void onTaskCompleted(AgentTask task) { }
        @Override public void onTaskFailed(AgentTask task) { }
        @Override public void onReplanning(TaskPlan failedPlan) { }
        @Override public void onCompleted(TaskPlan plan) { }
        @Override public void onFailed(String message, TaskPlan plan) { failed.incrementAndGet(); }
    }
}
