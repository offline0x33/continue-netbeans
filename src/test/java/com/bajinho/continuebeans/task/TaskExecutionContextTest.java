package com.bajinho.continuebeans.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.Test;

class TaskExecutionContextTest {
    @Test
    void recordsVerifiedResultsAndFailuresForLaterTasks() {
        TaskExecutionContext context = new TaskExecutionContext("objetivo");
        AgentTask first = new AgentTask("Criar arquivo", "criar", "arquivo existe", Collections.<String>emptyList());
        AgentTask second = new AgentTask("Validar arquivo", "validar", "arquivo correto", Collections.<String>emptyList());

        context.recordTaskResult(first, "src/Main.java criado");
        context.recordTaskFailure(second, "assertion failed");

        assertEquals("objetivo", context.getOriginalGoal());
        assertEquals(2, context.getEntries().size());
        String rendered = context.renderForPrompt();
        assertTrue(rendered.contains("DONE | Criar arquivo | src/Main.java criado"));
        assertTrue(rendered.contains("FAILED | Validar arquivo | assertion failed"));
    }

    @Test
    void emptyContextProducesExplicitPromptState() {
        TaskExecutionContext context = new TaskExecutionContext("objetivo");
        assertEquals("Nenhuma tarefa anterior foi concluída ainda.", context.renderForPrompt());
    }
}
