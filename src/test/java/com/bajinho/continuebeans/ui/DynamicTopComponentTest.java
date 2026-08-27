package com.bajinho.continuebeans.ui;

import org.junit.jupiter.api.Test;
import org.openide.windows.TopComponent;
import javax.swing.SwingUtilities;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;

class DynamicTopComponentTest {
    @Test
    void staticProviderAndWindowStateWork() throws Exception {
        DynamicTopComponent c = new DynamicTopComponent("test-window", DynamicTopComponent.staticContentProvider("hello"));
        SwingUtilities.invokeAndWait(() -> {});
        assertEquals("test-window", c.getWindowId());
        assertEquals("hello", c.getContent());
        c.setWindowTitle("My Window");
        assertEquals("My Window", c.getDisplayName());
        c.setContent("updated");
        SwingUtilities.invokeAndWait(() -> {});
        assertEquals("updated", c.getContent());
        c.appendContent(" + more");
        SwingUtilities.invokeAndWait(() -> {});
        assertEquals("updated + more", c.getContent());
        c.clearContent();
        SwingUtilities.invokeAndWait(() -> {});
        assertEquals("", c.getContent());
    }

    @Test
    void providerRefreshHandlesSuccessAndFailure() throws Exception {
        DynamicTopComponent c = new DynamicTopComponent("async-window", null);
        assertTrue(c.getContent().contains("No content provider configured"));
        c.setContentProvider(() -> CompletableFuture.completedFuture("async content"));
        SwingUtilities.invokeAndWait(() -> {});
        assertEquals("async content", c.getContent());
        c.setContentProvider(() -> CompletableFuture.failedFuture(new IllegalStateException("boom")));
        SwingUtilities.invokeAndWait(() -> {});
        assertTrue(c.getContent().contains("Error loading content: boom"));
    }

    @Test
    void controlsSizeAndCommandProviderWork() throws Exception {
        DynamicTopComponent c = new DynamicTopComponent("state-window", DynamicTopComponent.staticContentProvider("content"));
        c.setControlButtonsVisible(false);
        c.setWindowSize(800, 600);
        SwingUtilities.invokeAndWait(() -> {});
        assertEquals(TopComponent.PERSISTENCE_ONLY_OPENED, c.getPersistenceType());
        assertEquals(800, c.getPreferredSize().width);
        assertEquals(600, c.getPreferredSize().height);
        String output = DynamicTopComponent.commandContentProvider("sh", "-c", "printf hello").loadContentAsync().get();
        assertEquals("hello\n", output);
        String failure = DynamicTopComponent.commandContentProvider("sh", "-c", "printf fail; exit 3").loadContentAsync().get();
        assertTrue(failure.contains("fail"));
        assertTrue(failure.contains("Command exited with code: 3"));
    }
}
