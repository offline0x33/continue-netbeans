package com.bajinho.continuebeans.task;

import java.util.Optional;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileUtil;

/** Project context backed by the projects currently opened in NetBeans. */
public final class NetBeansProjectContext implements ProjectContext {

    @Override
    public Optional<String> currentProjectRoot() {
        try {
            Project[] projects = OpenProjects.getDefault().getOpenProjects();
            if (projects == null || projects.length == 0 || projects[0] == null) {
                return Optional.empty();
            }
            java.io.File directory = FileUtil.toFile(projects[0].getProjectDirectory());
            return directory == null
                    ? Optional.empty()
                    : Optional.of(directory.getAbsolutePath());
        } catch (RuntimeException ignored) {
            return Optional.empty();
        }
    }
}
