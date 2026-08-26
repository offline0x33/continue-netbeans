package com.bajinho.continuebeans.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class AgentTaskStateMachineTest {

    @Test
    void taskLifecycleTracksAttemptsResultsAndErrors() {
        AgentTask task = new AgentTask(" title ", " instruction ", " criterion ", null);
        assertEquals(TaskStatus.PENDING, task.getStatus());
        assertEquals("title", task.getTitle());
        assertEquals("instruction", task.getInstruction());
        assertEquals("criterion", task.getCompletionCriteria());
        assertEquals(0, task.getAttempts());

        task.start();
        assertEquals(TaskStatus.RUNNING, task.getStatus());
        assertEquals(1, task.getAttempts());
        task.verifying("result");
        assertEquals(TaskStatus.VERIFYING, task.getStatus());
        assertEquals("result", task.getLastResult());
        task.complete("done");
        assertEquals(TaskStatus.DONE, task.getStatus());
        assertEquals("done", task.getLastResult());
        assertEquals(null, task.getLastError());
        assertThrows(IllegalStateException.class, task::start);
    }

    @Test
    void failedTaskCanRetryAndBlock() {
        AgentTask task = new AgentTask("t", "i", "c", List.of());
        task.start();
        task.fail(null);
        assertEquals(TaskStatus.FAILED, task.getStatus());
        assertEquals("unknown error", task.getLastError());
        task.start();
        assertEquals(2, task.getAttempts());
        task.block("blocked");
        assertEquals(TaskStatus.BLOCKED, task.getStatus());
        assertEquals("blocked", task.getLastError());
    }
}
