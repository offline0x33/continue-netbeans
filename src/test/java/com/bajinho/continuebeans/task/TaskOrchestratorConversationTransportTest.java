package com.bajinho.continuebeans.task;

import com.bajinho.continuebeans.LlmClient;
import com.bajinho.continuebeans.ai.AIToolCallingIntegration;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskOrchestratorConversationTransportTest {

    @Test
    void conversationForwardsStreamingChunksToListener() {
        TaskPlanner planner = mock(TaskPlanner.class);
        AIToolCallingIntegration executor = mock(AIToolCallingIntegration.class);
        LlmClient client = mock(LlmClient.class);
        when(client.shouldUseTaskOrchestrator("Olá")).thenReturn(false);

        doAnswer(invocation -> {
            java.util.function.Consumer<String> onChunk = invocation.getArgument(4);
            Runnable onComplete = invocation.getArgument(6);
            onChunk.accept("Olá, ");
            onChunk.accept("José!");
            onComplete.run();
            return null;
        }).when(client).perguntarIAStreaming(anyString(), anyString(), anyString(), anyString(), any(), any(), any());

        TaskOrchestrator orchestrator = new TaskOrchestrator(planner, executor, client, null);
        List<String> chunks = new ArrayList<>();

        orchestrator.executeGoal("Olá", "lmstudio", listener(chunks)).join();

        assertEquals(List.of("Olá, ", "José!"), chunks);
        verify(client).perguntarIAStreaming(anyString(), anyString(), anyString(), anyString(), any(), any(), any());
    }

    private TaskOrchestrator.Listener listener(List<String> chunks) {
        return new TaskOrchestrator.Listener() {
            @Override public void onPlanCreated(TaskPlan plan) { }
            @Override public void onTaskStarted(AgentTask task) { }
            @Override public void onTaskVerifying(AgentTask task) { }
            @Override public void onTaskCompleted(AgentTask task) { }
            @Override public void onTaskFailed(AgentTask task) { }
            @Override public void onReplanning(TaskPlan failedPlan) { }
            @Override public void onCompleted(TaskPlan plan) { }
            @Override public void onFailed(String message, TaskPlan plan) { }
            @Override public void onConversationChunk(String chunk) { chunks.add(chunk); }
        };
    }
}
