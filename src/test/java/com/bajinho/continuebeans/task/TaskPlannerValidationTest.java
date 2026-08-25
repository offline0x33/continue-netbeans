package com.bajinho.continuebeans.task;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.http.HttpClient;
import org.junit.jupiter.api.Test;

class TaskPlannerValidationTest {
    @Test
    void rejectsDependencyIndexOutsidePlan() {
        TaskPlanner planner = new TaskPlanner(HttpClient.newHttpClient(), "http://unused", "test");
        String json = "{\"tasks\":["
                + "{\"title\":\"Criar\",\"instruction\":\"criar\",\"completionCriteria\":\"ok\",\"dependsOn\":[4]}]}";

        assertThrows(IllegalStateException.class, () -> planner.parsePlan("goal", json));
    }

    @Test
    void rejectsDependencyOnFutureTask() {
        TaskPlanner planner = new TaskPlanner(HttpClient.newHttpClient(), "http://unused", "test");
        String json = "{\"tasks\":["
                + "{\"title\":\"Criar\",\"instruction\":\"criar\",\"completionCriteria\":\"ok\",\"dependsOn\":[1]},"
                + "{\"title\":\"Validar\",\"instruction\":\"validar\",\"completionCriteria\":\"ok\",\"dependsOn\":[]}]}";

        assertThrows(IllegalStateException.class, () -> planner.parsePlan("goal", json));
    }
}
