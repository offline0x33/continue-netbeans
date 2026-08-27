package com.bajinho.continuebeans.ui;

import org.junit.jupiter.api.Test;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ShortcutManagerTest {
    @Test
    void shortcutValueObjectChecksContextAndPreservesMetadata() {
        ActionListener action = e -> { };
        ShortcutManager.Shortcut shortcut = new ShortcutManager.Shortcut(
                "test", "Test", "description", KeyStroke.getKeyStroke("ctrl T"),
                action, new String[]{"editor", "project"}, 10, false, true);
        assertEquals("test", shortcut.getId());
        assertEquals(KeyStroke.getKeyStroke("ctrl T"), shortcut.getKeyStroke());
        assertSame(action, shortcut.getAction());
        assertTrue(shortcut.isRelevantForContext("editor"));
        assertFalse(shortcut.isRelevantForContext("chat"));
    }

    @Test
    void registrationLookupConflictAndListenersWork() {
        ShortcutManager manager = ShortcutManager.getInstance();
        AtomicInteger registered = new AtomicInteger();
        AtomicInteger unregistered = new AtomicInteger();
        ShortcutManager.ShortcutListener listener = new ShortcutManager.ShortcutListener() {
            public void shortcutTriggered(ShortcutManager.Shortcut shortcut) { }
            public void shortcutRegistered(ShortcutManager.Shortcut shortcut) { registered.incrementAndGet(); }
            public void shortcutUnregistered(String id) { unregistered.incrementAndGet(); }
        };
        manager.addShortcutListener(listener);
        String id = "test-batch-shortcut";
        KeyStroke key = KeyStroke.getKeyStroke("ctrl alt T");
        manager.unregisterShortcut(id);
        manager.registerShortcut(new ShortcutManager.Shortcut(id, "Batch", "Batch shortcut", key,
                e -> { }, new String[]{"all"}, 200, false, true));
        assertNotNull(manager.getShortcut(id));
        assertEquals(1, registered.get());
        List<ShortcutManager.Shortcut> conflicts = manager.checkConflicts(new ShortcutManager.Shortcut(
                "other", "Other", "Other", key, e -> { }, new String[]{"all"}, 1, false, true));
        assertEquals(1, conflicts.size());
        manager.unregisterShortcut(id);
        assertEquals(1, unregistered.get());
        manager.removeShortcutListener(listener);
    }

    @Test
    void contextOrderingAndComponentInstallationWork() {
        ShortcutManager manager = ShortcutManager.getInstance();
        String id = "component-shortcut";
        KeyStroke key = KeyStroke.getKeyStroke("ctrl alt Y");
        manager.unregisterShortcut(id);
        manager.registerShortcut(new ShortcutManager.Shortcut(id, "Component", "Component shortcut", key,
                e -> { }, new String[]{"editor"}, 500, false, true));
        List<ShortcutManager.Shortcut> editor = manager.getShortcutsForContext("editor");
        assertFalse(editor.isEmpty());
        assertEquals(id, editor.get(0).getId());
        JPanel panel = new JPanel();
        manager.installShortcuts(panel, "editor");
        assertNotNull(panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(key));
        manager.removeShortcuts(panel);
        assertNull(panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).get(key));
        manager.unregisterShortcut(id);
    }

    @Test
    void triggeringKnownAndUnknownShortcutsIsSafe() {
        ShortcutManager manager = ShortcutManager.getInstance();
        AtomicInteger count = new AtomicInteger();
        String id = "trigger-shortcut";
        manager.unregisterShortcut(id);
        manager.registerShortcut(new ShortcutManager.Shortcut(id, "Trigger", "Trigger shortcut",
                KeyStroke.getKeyStroke("ctrl alt U"), e -> count.incrementAndGet(),
                new String[]{"all"}, 1, false, true));
        manager.triggerShortcut(id);
        manager.triggerShortcut("missing-shortcut");
        assertEquals(1, count.get());
        manager.unregisterShortcut(id);
    }
}