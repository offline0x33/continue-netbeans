package com.bajinho.continuebeans.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bajinho.continuebeans.ai.AIToolCallingIntegration;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class TaskOrchestratorCoverageGapTest {

    @Test
    void missingProjectFailsBeforePlannerOrExecutor() {
        TaskPlanner planner = mock(TaskPlanner.class);
        RecordingExecutor executor = new RecordingExecutor(AIToolCallingIntegration.AIResponse.text("unexpected"));
        TaskOrchestrator orchestrator = new TaskOrchestrator(planner, executor, null, () -> Optional.empty());
        RecordingListener listener = new RecordingListener();

        TaskPlan plan = orchestrator.executeGoal("analise o projeto atual", "lmstudio", listener).join();

        assertTrue(plan.hasBlockedTask());
        assertEquals(TaskStatus.BLOCKED, plan.getTasks().get(0).getStatus());
        assertEquals(0, executor.calls.get());
        assertEquals(1, listener.failed.get());
    }

    @Test
    void verificationNotDoneThreeTimesBlocksTask() throws Exception {
        TaskPlanner planner = mock(TaskPlanner.class);
        AgentTask task = new AgentTask("task", "do", "done", Collections.emptyList());
        TaskPlan plan = new TaskPlan("goal", Collections.singletonList(task));
        when(planner.createPlan(anyString())).thenReturn(plan);
        RecordingExecutor executor = new RecordingExecutor(AIToolCallingIntegration.AIResponse.text("NOT_DONE"),
                true);
        TaskOrchestrator orchestrator = new TaskOrchestrator(planner, executor);
        RecordingListener listener = new RecordingListener();

        TaskPlan result = orchestrator.executeGoal("execute", "lmstudio", listener).join();

        assertEquals(TaskStatus.BLOCKED, result.getTasks().get(0).getStatus());
        assertEquals(3, result.getTasks().get(0).getAttempts());
        assertTrue(result.getTasks().get(0).getLastError().contains("3 tentativas"));
    }

    @Test
    void replanningStopsAtMaximumAndNotifiesFailure() throws Exception {
        TaskPlanner planner = mock(TaskPlanner.class);
        when(planner.createPlan(anyString()))
                .thenReturn(plan("one"), plan("two"), plan("three"));
        RecordingExecutor executor = new RecordingExecutor(AIToolCallingIntegration.AIResponse.error("boom"));
        TaskOrchestrator orchestrator = new TaskOrchestrator(planner, executor);
        RecordingListener listener = new RecordingListener();

        TaskPlan result = orchestrator.executeGoal("corrigir falha", "lmstudio", listener).join();

        assertTrue(result.hasBlockedTask());
        assertEquals(3, listener.created.get());
        assertEquals(2, listener.replans.get());
        assertEquals(1, listener.failed.get());
        assertTrue(listener.lastFailure.contains("Limite de replanejamentos"));
    }

    @Test
    void nullExecutorResponseProducesNonNullFailure() throws Exception {
        TaskPlanner planner = mock(TaskPlanner.class);
        when(planner.createPlan(anyString())).thenReturn(plan("null"));
        RecordingExecutor executor = new RecordingExecutor(null);
        TaskOrchestrator orchestrator = new TaskOrchestrator(planner, executor);
        RecordingListener listener = new RecordingListener();

        TaskPlan result = orchestrator.executeGoal("execute", "lmstudio", listener).join();

        assertEquals(TaskStatus.BLOCKED, result.getTasks().get(0).getStatus());
        assertTrue(result.getTasks().get(0).getLastError().contains("resposta vazia"));
    }

    private static TaskPlan plan(String suffix) {
        return new TaskPlan("goal-" + suffix,
                Collections.singletonList(new AgentTask("task-" + suffix, "do", "done", Collections.emptyList())));
    }

    private static final class RecordingExecutor extends AIToolCallingIntegration {
        private final AtomicInteger calls = new AtomicInteger();
        private final AIResponse response;
        private final boolean alternateExecutionAndVerification;

        private RecordingExecutor(AIResponse response) {
            this(response, false);
        }

        private RecordingExecutor(AIResponse response, boolean alternateExecutionAndVerification) {
            this.response = response;
            this.alternateExecutionAndVerification = alternateExecutionAndVerification;
        }

        @Override
        public CompletableFuture<AIResponse> processRequestWithToolCalling(String userMessage, String provider) {
            int call = calls.getAndIncrement();
            if (alternateExecutionAndVerification && call % 2 == 0) {
                return CompletableFuture.completedFuture(AIResponse.text("DONE"));
            }
            return CompletableFuture.completedFuture(response);
        }
    }

    private static final class RecordingListener implements TaskOrchestrator.Listener {
        private final AtomicInteger created = new AtomicInteger();
        private final AtomicInteger replans = new AtomicInteger();
        private final AtomicInteger failed = new AtomicInteger();
        private volatile String lastFailure = "";

        @Override public void onPlanCreated(TaskPlan plan) { created.incrementAndGet(); }
        @Override public void onTaskStarted(AgentTask task) { }
        @Override public void onTaskVerifying(AgentTask task) { }
        @Override public void onTaskCompleted(AgentTask task) { }
        @Override public void onTaskFailed(AgentTask task) { }
        @Override public void onReplanning(TaskPlan failedPlan) { replans.incrementAndGet(); }
        @Override public void onCompleted(TaskPlan plan) { }
        @Override public void onFailed(String message, TaskPlan plan) { failed.incrementAndGet(); lastFailure = message; }
    }
}
