package com.bajinho.continuebeans.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Ordered execution plan for a user goal. */
public final class TaskPlan {
    private final String id = UUID.randomUUID().toString();
    private final String goal;
    private final List<AgentTask> tasks;

    public TaskPlan(String goal, List<AgentTask> tasks) {
        if (goal == null || goal.isBlank()) {
            throw new IllegalArgumentException("goal is required");
        }
        if (tasks == null || tasks.isEmpty()) {
            throw new IllegalArgumentException("At least one task is required");
        }
        this.goal = goal.trim();
        this.tasks = new ArrayList<>(tasks);
    }

    public String getId() { return id; }
    public String getGoal() { return goal; }
    public List<AgentTask> getTasks() { return Collections.unmodifiableList(tasks); }

    public boolean isComplete() {
        return tasks.stream().allMatch(t -> t.getStatus() == TaskStatus.DONE);
    }

    public boolean hasBlockedTask() {
        return tasks.stream().anyMatch(t -> t.getStatus() == TaskStatus.BLOCKED);
    }

    public AgentTask nextRunnableTask() {
        for (AgentTask task : tasks) {
            if (task.getStatus() != TaskStatus.PENDING && task.getStatus() != TaskStatus.FAILED) {
                continue;
            }
            boolean dependenciesDone = task.getDependencies().stream()
                    .map(this::findById)
                    .allMatch(t -> t != null && t.getStatus() == TaskStatus.DONE);
            if (dependenciesDone) {
                return task;
            }
        }
        return null;
    }

    public AgentTask findById(String taskId) {
        return tasks.stream().filter(t -> t.getId().equals(taskId)).findFirst().orElse(null);
    }
}
