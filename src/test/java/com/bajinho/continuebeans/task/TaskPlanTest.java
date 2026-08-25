package com.bajinho.continuebeans.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TaskPlanTest {

    @Test
    void executesTasksInDependencyOrder() {
        AgentTask first = new AgentTask("Criar arquivo", "criar", "arquivo existe", List.of());
        AgentTask second = new AgentTask("Validar arquivo", "validar", "arquivo válido", List.of(first.getId()));
        TaskPlan plan = new TaskPlan("Criar e validar", List.of(first, second));

        assertEquals(first, plan.nextRunnableTask());
        first.start();
        first.complete("ok");

        assertEquals(second, plan.nextRunnableTask());
        assertFalse(plan.isComplete());
        second.start();
        second.complete("ok");

        assertTrue(plan.isComplete());
        assertNull(plan.nextRunnableTask());
    }

    @Test
    void failedTaskCanBeRetried() {
        AgentTask task = new AgentTask("Executar", "executar", "resultado", List.of());
        TaskPlan plan = new TaskPlan("Executar", List.of(task));

        task.start();
        assertEquals(1, task.getAttempts());
        task.fail("falhou");
        assertEquals(TaskStatus.FAILED, task.getStatus());

        assertNotNull(plan.nextRunnableTask());
        task.start();
        task.complete("corrigido");

        assertEquals(TaskStatus.DONE, task.getStatus());
        assertEquals(2, task.getAttempts());
        assertTrue(plan.isComplete());
    }

    @Test
    void dependentTaskCannotRunBeforeDependency() {
        AgentTask dependency = new AgentTask("Base", "base", "base pronta", List.of());
        AgentTask task = new AgentTask("Dependente", "dep", "resultado", List.of(dependency.getId()));
        TaskPlan plan = new TaskPlan("Duas tarefas", List.of(dependency, task));

        assertEquals(dependency, plan.nextRunnableTask());
        assertEquals(TaskStatus.PENDING, task.getStatus());
        dependency.start();
        dependency.complete("ok");
        assertEquals(task, plan.nextRunnableTask());
    }
}
