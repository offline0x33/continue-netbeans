package com.bajinho.continuebeans.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shared execution memory for a task plan.
 * Keeps verified results and failures available to subsequent tasks and
 * re-planning without coupling the UI to the agent lifecycle.
 */
public final class TaskExecutionContext {
    private final String originalGoal;
    private final List<String> entries = new ArrayList<>();

    public TaskExecutionContext(String originalGoal) {
        this.originalGoal = originalGoal == null ? "" : originalGoal;
    }

    public String getOriginalGoal() {
        return originalGoal;
    }

    public void recordTaskResult(AgentTask task, String result) {
        String value = result == null ? "" : result.trim();
        entries.add("DONE | " + task.getTitle() + " | " + truncate(value, 1500));
    }

    public void recordTaskFailure(AgentTask task, String failure) {
        String value = failure == null ? "" : failure.trim();
        entries.add("FAILED | " + task.getTitle() + " | " + truncate(value, 1500));
    }

    public List<String> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public String renderForPrompt() {
        if (entries.isEmpty()) {
            return "Nenhuma tarefa anterior foi concluída ainda.";
        }
        StringBuilder result = new StringBuilder("MEMÓRIA COMPARTILHADA DO PLANO:\n");
        for (String entry : entries) {
            result.append("- ").append(entry).append('\n');
        }
        return result.toString().trim();
    }

    private String truncate(String value, int limit) {
        if (value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit) + "...";
    }
}
