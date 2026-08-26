package com.bajinho.continuebeans.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TaskPlanStateMachineTest {

    @Test
    void nextRunnableTaskHonorsDependencies() {
        AgentTask first = new AgentTask("first", "do first", "done", List.of());
        AgentTask second = new AgentTask("second", "do second", "done", List.of(first.getId()));
        TaskPlan plan = new TaskPlan("goal", List.of(first, second));

        assertEquals(first, plan.nextRunnableTask());
        assertFalse(plan.isComplete());
        assertFalse(plan.hasBlockedTask());

        first.complete("done");
        assertEquals(second, plan.nextRunnableTask());

        second.complete("done");
        assertTrue(plan.isComplete());
        assertNull(plan.nextRunnableTask());
    }

    @Test
    void blockedTasksAreDetectedAndUnknownDependencyIsNotRunnable() {
        AgentTask blockedDependency = new AgentTask("blocked", "x", "y", List.of());
        blockedDependency.block("reason");
        AgentTask dependent = new AgentTask("dependent", "x", "y", List.of(blockedDependency.getId()));
        TaskPlan plan = new TaskPlan("goal", List.of(blockedDependency, dependent));

        assertTrue(plan.hasBlockedTask());
        assertEquals(null, plan.nextRunnableTask());
        assertEquals(blockedDependency, plan.findById(blockedDependency.getId()));
        assertEquals(null, plan.findById("missing"));
    }
}
