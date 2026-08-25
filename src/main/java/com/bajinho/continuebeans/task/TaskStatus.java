package com.bajinho.continuebeans.task;

/** Lifecycle states for an agent task. */
public enum TaskStatus {
    PENDING,
    RUNNING,
    VERIFYING,
    DONE,
    FAILED,
    BLOCKED
}
