package com.bajinho.continuebeans.task;

import java.util.Optional;

/** Resolves the project context currently available to the NetBeans session. */
@FunctionalInterface
public interface ProjectContext {
    Optional<String> currentProjectRoot();
}
