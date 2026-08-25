package com.bajinho.continuebeans.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** A concrete, verifiable unit of work in an agent plan. */
public final class AgentTask {
    private final String id;
    private final String title;
    private final String instruction;
    private final String completionCriteria;
    private final List<String> dependencies;
    private TaskStatus status;
    private int attempts;
    private String lastResult;
    private String lastError;

    public AgentTask(String title, String instruction, String completionCriteria, List<String> dependencies) {
        this(UUID.randomUUID().toString(), title, instruction, completionCriteria, dependencies);
    }

    AgentTask(String id, String title, String instruction, String completionCriteria, List<String> dependencies) {
        this.id = require(id, "id");
        this.title = require(title, "title");
        this.instruction = require(instruction, "instruction");
        this.completionCriteria = require(completionCriteria, "completionCriteria");
        this.dependencies = new ArrayList<>(dependencies == null ? List.of() : dependencies);
        this.status = TaskStatus.PENDING;
    }

    private static String require(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getInstruction() { return instruction; }
    public String getCompletionCriteria() { return completionCriteria; }
    public List<String> getDependencies() { return Collections.unmodifiableList(dependencies); }
    public TaskStatus getStatus() { return status; }
    public int getAttempts() { return attempts; }
    public String getLastResult() { return lastResult; }
    public String getLastError() { return lastError; }

    public void start() {
        if (status != TaskStatus.PENDING && status != TaskStatus.FAILED) {
            throw new IllegalStateException("Task cannot start from " + status);
        }
        status = TaskStatus.RUNNING;
        attempts++;
        lastError = null;
    }

    public void verifying(String result) {
        status = TaskStatus.VERIFYING;
        lastResult = result;
    }

    public void complete(String result) {
        status = TaskStatus.DONE;
        lastResult = result;
        lastError = null;
    }

    public void fail(String error) {
        status = TaskStatus.FAILED;
        lastError = error == null ? "unknown error" : error;
    }

    public void block(String reason) {
        status = TaskStatus.BLOCKED;
        lastError = reason;
    }
}
