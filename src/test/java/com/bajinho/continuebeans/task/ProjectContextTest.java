package com.bajinho.continuebeans.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bajinho.continuebeans.ai.AIToolCallingIntegration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ProjectContextTest {

    @Test
    void projectRequiredWithoutOpenProjectFailsWithoutRetries() {
        ProjectContext context = mock(ProjectContext.class);
        when(context.currentProjectRoot()).thenReturn(Optional.empty());
        RecordingAgent agent = new RecordingAgent();
        TaskOrchestrator orchestrator = new TaskOrchestrator(
                new TaskPlanner(), agent, null, context);
        RecordingListener listener = new RecordingListener();

        TaskPlan plan = orchestrator.executeGoal("analise o projeto aberto no NetBeans", "test", listener).join();

        assertEquals(TaskStatus.BLOCKED, plan.getTasks().get(0).getStatus());
        assertEquals(0, agent.calls.get());
        assertEquals(0, listener.replanningCount.get());
        assertTrue(listener.failureMessage.get().contains("Nenhum projeto aberto"));
    }

    @Test
    void projectContextRootIsForwardedToExecutor() {
        ProjectContext context = mock(ProjectContext.class);
        when(context.currentProjectRoot()).thenReturn(Optional.of("/workspace/school-erp"));
        InspectableAgent agent = new InspectableAgent();
        TaskOrchestrator orchestrator = new TaskOrchestrator(
                new TaskPlanner(), agent, null, context);

        orchestrator.refreshProjectContext();

        assertEquals("/workspace/school-erp", agent.workspaceRoot);
    }

    private static final class RecordingAgent extends AIToolCallingIntegration {
        private final AtomicInteger calls = new AtomicInteger();

        @Override
        public CompletableFuture<AIResponse> processRequestWithToolCalling(String userMessage, String provider) {
            calls.incrementAndGet();
            return CompletableFuture.completedFuture(AIResponse.text("unexpected"));
        }
    }

    private static final class InspectableAgent extends AIToolCallingIntegration {
        private String workspaceRoot;

        @Override
        public void setWorkspaceRoot(String workspaceRoot) {
            this.workspaceRoot = workspaceRoot;
            super.setWorkspaceRoot(workspaceRoot);
        }
    }

    private static final class RecordingListener implements TaskOrchestrator.Listener {
        private final AtomicInteger replanningCount = new AtomicInteger();
        private final java.util.concurrent.atomic.AtomicReference<String> failureMessage = new java.util.concurrent.atomic.AtomicReference<>();

        @Override public void onPlanCreated(TaskPlan plan) { }
        @Override public void onTaskStarted(AgentTask task) { }
        @Override public void onTaskVerifying(AgentTask task) { }
        @Override public void onTaskCompleted(AgentTask task) { }
        @Override public void onTaskFailed(AgentTask task) { }
        @Override public void onReplanning(TaskPlan failedPlan) { replanningCount.incrementAndGet(); }
        @Override public void onCompleted(TaskPlan plan) { }
        @Override public void onFailed(String message, TaskPlan plan) { failureMessage.set(message); }
    }
}
