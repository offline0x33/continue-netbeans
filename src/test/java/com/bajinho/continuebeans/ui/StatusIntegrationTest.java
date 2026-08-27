package com.bajinho.continuebeans.ui;

import org.junit.jupiter.api.Test;

import javax.swing.SwingUtilities;
import java.awt.Color;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatusIntegrationTest {
    @Test
    void initialStatusAndStatusLineAreAvailable() {
        StatusIntegration integration = new StatusIntegration();
        assertEquals("Ready", integration.getCurrentStatus());
        assertNotNull(integration.getStatusLineElement());
        assertTrue(integration.getStatusItems().isEmpty());
        integration.clearStatusItems();
    }

    @Test
    void statusItemsAreSortedAndCapped() {
        StatusIntegration integration = new StatusIntegration();
        for (int i = 0; i < 12; i++) {
            integration.addStatusItem(new StatusIntegration.StatusItem("id" + i, "item" + i,
                    Color.BLACK, null, i, false));
        }
        List<StatusIntegration.StatusItem> items = integration.getStatusItems();
        assertEquals(10, items.size());
        assertEquals(11, items.get(0).getPriority());
        assertEquals(2, items.get(9).getPriority());
        integration.clearStatusItems();
    }

    @Test
    void progressTaskClampsValuesAndComputesProgress() {
        StatusIntegration.ProgressTask task =
                new StatusIntegration.ProgressTask("t", "Build", 100, false, "running");
        assertEquals(0.0, task.getProgress());
        task.setCurrent(40);
        assertEquals(40, task.getCurrent());
        assertEquals(0.4, task.getProgress(), 0.0001);
        task.setCurrent(200);
        assertEquals(100, task.getCurrent());
        assertTrue(task.isCompleted());
        task.setCompleted(false);
        assertFalse(task.isCompleted());
        task.setCurrent(-10);
        assertEquals(0, task.getCurrent());
    }

    @Test
    void progressLifecycleAndFiltersWork() throws Exception {
        StatusIntegration integration = new StatusIntegration();
        StatusIntegration.ProgressTask task = integration.startProgress("t", "Task", 10, false, "running");
        assertEquals(task, integration.getProgressTasks().get("t"));
        integration.updateProgress("t", 5);
        assertEquals(5, integration.getProgressTasks().get("t").getCurrent());
        integration.setShowProgress(false);
        integration.setShowClock(false);
        integration.completeProgress("t");
        Thread.sleep(100);
        assertTrue(integration.getProgressTasks().containsKey("t"));
        integration.clearCompletedTasks();
        assertFalse(integration.getProgressTasks().containsKey("t"));
        integration.refreshTheme();
        SwingUtilities.invokeAndWait(() -> { });
    }
}
