package com.bajinho.continuebeans.ui;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.KeyboardFocusManager;
import java.awt.KeyEventDispatcher;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

/**
 * Shortcut manager for Continue Beans with global and context-sensitive
 * keyboard shortcuts, custom key bindings, and intelligent shortcut conflict resolution.
 * 
 * @author Continue Beans Team
 */
public class ShortcutManager {
    
    private static final Logger LOG = Logger.getLogger(ShortcutManager.class.getName());
    
    private static ShortcutManager instance;
    
    private final Map<String, Shortcut> shortcuts;
    private final Map<String, List<Shortcut>> contextShortcuts;
    private final List<ShortcutListener> listeners;
    private final GlobalKeyDispatcher globalDispatcher;
    private boolean globalShortcutsEnabled;
    
    /**
     * Represents a keyboard shortcut.
     */
    public static class Shortcut {
        private final String id;
        private final String displayName;
        private final String description;
        private final KeyStroke keyStroke;
        private final ActionListener action;
        private final String[] contexts;
        private final int priority;
        private final boolean global;
        private final boolean enabled;
        
        public Shortcut(String id, String displayName, String description, 
                       KeyStroke keyStroke, ActionListener action, String[] contexts, 
                       int priority, boolean global, boolean enabled) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.keyStroke = keyStroke;
            this.action = action;
            this.contexts = contexts != null ? contexts : new String[0];
            this.priority = priority;
            this.global = global;
            this.enabled = enabled;
        }
        
        // Getters
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public KeyStroke getKeyStroke() { return keyStroke; }
        public ActionListener getAction() { return action; }
        public String[] getContexts() { return contexts; }
        public int getPriority() { return priority; }
        public boolean isGlobal() { return global; }
        public boolean isEnabled() { return enabled; }
        
        /**
         * Checks if this shortcut is relevant for the given context.
         * @param context The context to check
         * @return True if relevant
         */
        public boolean isRelevantForContext(String context) {
            if (contexts.length == 0) {
                return true; // Shortcut is relevant for all contexts
            }
            for (String ctx : contexts) {
                if (ctx.equals(context)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    /**
     * Shortcut listener interface.
     */
    public interface ShortcutListener {
        void shortcutTriggered(Shortcut shortcut);
        void shortcutRegistered(Shortcut shortcut);
        void shortcutUnregistered(String shortcutId);
    }
    
    /**
     * Global key dispatcher for handling global shortcuts.
     */
    private class GlobalKeyDispatcher implements KeyEventDispatcher {
        
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (!globalShortcutsEnabled) {
                return false;
            }
            
            // Only process key press events
            if (e.getID() != KeyEvent.KEY_PRESSED) {
                return false;
            }
            
            KeyStroke keyStroke = KeyStroke.getKeyStrokeForEvent(e);
            
            // Check for global shortcuts
            for (Shortcut shortcut : shortcuts.values()) {
                if (shortcut.isGlobal() && shortcut.isEnabled() && 
                    shortcut.getKeyStroke().equals(keyStroke)) {
                    
                    // Check context relevance
                    String currentContext = getCurrentContext();
                    if (shortcut.isRelevantForContext(currentContext)) {
                        e.consume();
                        triggerShortcut(shortcut);
                        return true;
                    }
                }
            }
            
            return false;
        }
    }
    
    /**
     * Private constructor for singleton.
     */
    private ShortcutManager() {
        this.shortcuts = new HashMap<>();
        this.contextShortcuts = new HashMap<>();
        this.listeners = new ArrayList<>();
        this.globalDispatcher = new GlobalKeyDispatcher();
        this.globalShortcutsEnabled = true;
        
        setupDefaultShortcuts();
        installGlobalDispatcher();
        
        LOG.info("ShortcutManager initialized");
    }
    
    /**
     * Gets the singleton instance.
     * @return The ShortcutManager instance
     */
    public static synchronized ShortcutManager getInstance() {
        if (instance == null) {
            instance = new ShortcutManager();
        }
        return instance;
    }
    
    /**
     * Sets up default shortcuts.
     */
    private void setupDefaultShortcuts() {
        // Chat shortcuts
        registerShortcut(new Shortcut(
            "open_chat", "Open Chat", "Open AI Chat window",
            KeyStroke.getKeyStroke("ctrl shift C"),
            e -> openChatWindow(),
            new String[]{"all"}, 100, true, true
        ));
        
        registerShortcut(new Shortcut(
            "clear_chat", "Clear Chat", "Clear chat history",
            KeyStroke.getKeyStroke("ctrl shift L"),
            e -> clearChatHistory(),
            new String[]{"chat"}, 90, true, true
        ));
        
        // Context shortcuts
        registerShortcut(new Shortcut(
            "show_context", "Show Context", "Show current project context",
            KeyStroke.getKeyStroke("ctrl shift X"),
            e -> showProjectContext(),
            new String[]{"project", "editor"}, 100, true, true
        ));
        
        // AI operations shortcuts
        registerShortcut(new Shortcut(
            "analyze_code", "Analyze Code", "Analyze current code",
            KeyStroke.getKeyStroke("ctrl shift A"),
            e -> analyzeCurrentCode(),
            new String[]{"editor"}, 100, true, true
        ));
        
        registerShortcut(new Shortcut(
            "refactor_code", "Refactor Code", "Suggest refactoring",
            KeyStroke.getKeyStroke("ctrl shift R"),
            e -> suggestRefactoring(),
            new String[]{"editor"}, 90, true, true
        ));
        
        registerShortcut(new Shortcut(
            "optimize_code", "Optimize Code", "Optimize code performance",
            KeyStroke.getKeyStroke("ctrl shift O"),
            e -> optimizeCode(),
            new String[]{"editor"}, 85, true, true
        ));
        
        // Tools shortcuts
        registerShortcut(new Shortcut(
            "project_info", "Project Info", "Show project information",
            KeyStroke.getKeyStroke("ctrl shift I"),
            e -> showProjectInfo(),
            new String[]{"project"}, 100, true, true
        ));
        
        registerShortcut(new Shortcut(
            "code_metrics", "Code Metrics", "Show code quality metrics",
            KeyStroke.getKeyStroke("ctrl shift M"),
            e -> showCodeMetrics(),
            new String[]{"project"}, 90, true, true
        ));
        
        // Settings shortcuts
        registerShortcut(new Shortcut(
            "preferences", "Preferences", "Open Continue Beans preferences",
            KeyStroke.getKeyStroke("ctrl shift P"),
            e -> openPreferences(),
            new String[]{"all"}, 100, true, true
        ));
        
        registerShortcut(new Shortcut(
            "toggle_ai", "Toggle AI", "Toggle AI features",
            KeyStroke.getKeyStroke("ctrl shift E"),
            e -> toggleAIFeatures(),
            new String[]{"all"}, 90, true, true
        ));
    }
    
    /**
     * Installs the global key dispatcher.
     */
    private void installGlobalDispatcher() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher(globalDispatcher);
    }
    
    /**
     * Registers a shortcut.
     * @param shortcut The shortcut to register
     */
    public void registerShortcut(Shortcut shortcut) {
        shortcuts.put(shortcut.getId(), shortcut);
        
        // Add to context mappings
        for (String context : shortcut.getContexts()) {
            contextShortcuts.computeIfAbsent(context, k -> new ArrayList<>()).add(shortcut);
        }
        
        notifyShortcutRegistered(shortcut);
        LOG.info("Shortcut registered: " + shortcut.getDisplayName());
    }
    
    /**
     * Unregisters a shortcut.
     * @param shortcutId The shortcut ID to unregister
     */
    public void unregisterShortcut(String shortcutId) {
        Shortcut shortcut = shortcuts.remove(shortcutId);
        if (shortcut != null) {
            // Remove from context mappings
            for (String context : shortcut.getContexts()) {
                List<Shortcut> contextList = contextShortcuts.get(context);
                if (contextList != null) {
                    contextList.remove(shortcut);
                }
            }
            
            notifyShortcutUnregistered(shortcutId);
            LOG.info("Shortcut unregistered: " + shortcutId);
        }
    }
    
    /**
     * Triggers a shortcut.
     * @param shortcut The shortcut to trigger
     */
    public void triggerShortcut(Shortcut shortcut) {
        if (shortcut.isEnabled()) {
            try {
                shortcut.getAction().actionPerformed(new ActionEvent(this, 
                    ActionEvent.ACTION_PERFORMED, shortcut.getId()));
                notifyShortcutTriggered(shortcut);
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Error executing shortcut: " + shortcut.getId(), e);
            }
        }
    }
    
    /**
     * Triggers a shortcut by ID.
     * @param shortcutId The shortcut ID
     */
    public void triggerShortcut(String shortcutId) {
        Shortcut shortcut = shortcuts.get(shortcutId);
        if (shortcut != null) {
            triggerShortcut(shortcut);
        } else {
            LOG.warning("Shortcut not found: " + shortcutId);
        }
    }
    
    /**
     * Gets the current context.
     * @return The current context
     */
    private String getCurrentContext() {
        // Simple context detection - can be enhanced
        if (SwingUtilities.isEventDispatchThread()) {
            java.awt.Component focusOwner = 
                KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
            
            if (focusOwner instanceof JTextComponent) {
                return "editor";
            } else if (focusOwner != null) {
                return "ui";
            }
        }
        
        return "all";
    }
    
    /**
     * Installs shortcuts on a component.
     * @param component The component to install shortcuts on
     * @param context The context for the shortcuts
     */
    public void installShortcuts(JComponent component, String context) {
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();
        
        for (Shortcut shortcut : shortcuts.values()) {
            if (!shortcut.isGlobal() && shortcut.isRelevantForContext(context)) {
                String actionKey = "shortcut_" + shortcut.getId();
                
                // Add to input map
                inputMap.put(shortcut.getKeyStroke(), actionKey);
                
                // Add to action map
                actionMap.put(actionKey, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        triggerShortcut(shortcut);
                    }
                });
            }
        }
    }
    
    /**
     * Removes shortcuts from a component.
     * @param component The component to remove shortcuts from
     */
    public void removeShortcuts(JComponent component) {
        InputMap inputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = component.getActionMap();
        
        // Remove all shortcut-related mappings
        List<KeyStroke> keysToRemove = new ArrayList<>();
        for (KeyStroke key : inputMap.keys()) {
            if (inputMap.get(key) instanceof String) {
                String actionKey = (String) inputMap.get(key);
                if (actionKey.startsWith("shortcut_")) {
                    keysToRemove.add(key);
                }
            }
        }
        
        for (KeyStroke key : keysToRemove) {
            String actionKey = (String) inputMap.get(key);
            inputMap.remove(key);
            actionMap.remove(actionKey);
        }
    }
    
    /**
     * Enables or disables global shortcuts.
     * @param enabled Whether to enable global shortcuts
     */
    public void setGlobalShortcutsEnabled(boolean enabled) {
        this.globalShortcutsEnabled = enabled;
        LOG.info("Global shortcuts " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Gets all shortcuts.
     * @return Copy of all shortcuts
     */
    public Map<String, Shortcut> getAllShortcuts() {
        return new HashMap<>(shortcuts);
    }
    
    /**
     * Gets shortcuts for a specific context.
     * @param context The context
     * @return List of shortcuts for the context
     */
    public List<Shortcut> getShortcutsForContext(String context) {
        List<Shortcut> result = new ArrayList<>();
        for (Shortcut shortcut : shortcuts.values()) {
            if (shortcut.isRelevantForContext(context)) {
                result.add(shortcut);
            }
        }
        
        // Sort by priority
        result.sort((s1, s2) -> Integer.compare(s2.getPriority(), s1.getPriority()));
        
        return result;
    }
    
    /**
     * Gets a shortcut by ID.
     * @param shortcutId The shortcut ID
     * @return The shortcut or null if not found
     */
    public Shortcut getShortcut(String shortcutId) {
        return shortcuts.get(shortcutId);
    }
    
    /**
     * Checks if a shortcut conflicts with another.
     * @param shortcut The shortcut to check
     * @return List of conflicting shortcuts
     */
    public List<Shortcut> checkConflicts(Shortcut shortcut) {
        List<Shortcut> conflicts = new ArrayList<>();
        
        for (Shortcut existing : shortcuts.values()) {
            if (!existing.getId().equals(shortcut.getId()) &&
                existing.getKeyStroke().equals(shortcut.getKeyStroke())) {
                
                // Check if they're relevant for the same context
                for (String context : shortcut.getContexts()) {
                    if (existing.isRelevantForContext(context)) {
                        conflicts.add(existing);
                        break;
                    }
                }
            }
        }
        
        return conflicts;
    }
    
    /**
     * Adds a shortcut listener.
     * @param listener The listener to add
     */
    public void addShortcutListener(ShortcutListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes a shortcut listener.
     * @param listener The listener to remove
     */
    public void removeShortcutListener(ShortcutListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notifies listeners about shortcut registration.
     * @param shortcut The registered shortcut
     */
    private void notifyShortcutRegistered(Shortcut shortcut) {
        for (ShortcutListener listener : listeners) {
            try {
                listener.shortcutRegistered(shortcut);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying shortcut listener", e);
            }
        }
    }
    
    /**
     * Notifies listeners about shortcut unregistration.
     * @param shortcutId The unregistered shortcut ID
     */
    private void notifyShortcutUnregistered(String shortcutId) {
        for (ShortcutListener listener : listeners) {
            try {
                listener.shortcutUnregistered(shortcutId);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying shortcut listener", e);
            }
        }
    }
    
    /**
     * Notifies listeners about shortcut triggering.
     * @param shortcut The triggered shortcut
     */
    private void notifyShortcutTriggered(Shortcut shortcut) {
        for (ShortcutListener listener : listeners) {
            try {
                listener.shortcutTriggered(shortcut);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying shortcut listener", e);
            }
        }
    }
    
    // Action implementations
    
    private void openChatWindow() {
        CompletableFuture.runAsync(() -> {
            try {
                org.openide.windows.TopComponent chatTC = 
                    org.openide.windows.WindowManager.getDefault()
                        .findTopComponent("ContinueTopComponent");
                if (chatTC != null) {
                    SwingUtilities.invokeLater(() -> {
                        chatTC.open();
                        chatTC.requestActive();
                    });
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to open chat window", e);
            }
        });
    }
    
    private void clearChatHistory() {
        LOG.info("Chat history cleared");
    }
    
    private void showProjectContext() {
        CompletableFuture.runAsync(() -> {
            try {
                DynamicTopComponent contextWindow = new DynamicTopComponent(
                    "project-context",
                    DynamicTopComponent.commandContentProvider("pwd", "ls", "-la")
                );
                contextWindow.setWindowTitle("Project Context");
                contextWindow.setWindowSize(600, 400);
                
                NetBeansWindowManager.getInstance().openTopComponentAsync(contextWindow, "output")
                    .thenAccept(success -> {
                        if (success) {
                            LOG.info("Project context window opened");
                        }
                    });
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to show project context", e);
            }
        });
    }
    
    private void analyzeCurrentCode() {
        LOG.info("Analyzing current code");
    }
    
    private void suggestRefactoring() {
        LOG.info("Suggesting refactoring");
    }
    
    private void optimizeCode() {
        LOG.info("Optimizing code");
    }
    
    private void showProjectInfo() {
        LOG.info("Showing project info");
    }
    
    private void showCodeMetrics() {
        LOG.info("Showing code metrics");
    }
    
    private void openPreferences() {
        LOG.info("Opening preferences");
    }
    
    private void toggleAIFeatures() {
        LOG.info("Toggling AI features");
    }
}
