package com.bajinho.continuebeans.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.ActionInvoker;
import org.openide.util.actions.SystemAction;

/**
 * Menu integration for Continue Beans with dynamic menu creation,
 * contextual menus, and intelligent menu organization.
 * 
 * @author Continue Beans Team
 */
public class MenuIntegration {
    
    private static final Logger LOG = Logger.getLogger(MenuIntegration.class.getName());
    
    private final Map<String, MenuCategory> categories;
    private final Map<String, MenuItem> items;
    private final List<MenuListener> listeners;
    private JMenuBar menuBar;
    private boolean contextMenuEnabled;
    
    /**
     * Represents a menu category.
     */
    public static class MenuCategory {
        private final String id;
        private final String displayName;
        private final int position;
        private final boolean enabled;
        private final List<MenuItem> items;
        
        public MenuCategory(String id, String displayName, int position, boolean enabled) {
            this.id = id;
            this.displayName = displayName;
            this.position = position;
            this.enabled = enabled;
            this.items = new ArrayList<>();
        }
        
        public void addItem(MenuItem item) {
            items.add(item);
            items.sort((i1, i2) -> Integer.compare(i2.getPriority(), i1.getPriority()));
        }
        
        // Getters
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public int getPosition() { return position; }
        public boolean isEnabled() { return enabled; }
        public List<MenuItem> getItems() { return items; }
    }
    
    /**
     * Represents a menu item.
     */
    public static class MenuItem {
        private final String id;
        private final String displayName;
        private final String description;
        private final ActionListener action;
        private final String shortcut;
        private final int priority;
        private final boolean enabled;
        private final boolean checkable;
        private final MenuCategory category;
        private boolean selected;
        
        public MenuItem(String id, String displayName, String description, 
                      ActionListener action, String shortcut, int priority, 
                      boolean enabled, boolean checkable, MenuCategory category) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.action = action;
            this.shortcut = shortcut;
            this.priority = priority;
            this.enabled = enabled;
            this.checkable = checkable;
            this.category = category;
            this.selected = false;
        }
        
        // Getters and setters
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public ActionListener getAction() { return action; }
        public String getShortcut() { return shortcut; }
        public int getPriority() { return priority; }
        public boolean isEnabled() { return enabled; }
        public boolean isCheckable() { return checkable; }
        public MenuCategory getCategory() { return category; }
        public boolean isSelected() { return selected; }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }
    
    /**
     * Menu listener interface.
     */
    public interface MenuListener {
        void menuItemSelected(MenuItem item);
        void menuCategorySelected(MenuCategory category);
        void menuShown(String menuId);
    }
    
    /**
     * Creates a new MenuIntegration.
     */
    public MenuIntegration() {
        this.categories = new HashMap<>();
        this.items = new HashMap<>();
        this.listeners = new ArrayList<>();
        this.contextMenuEnabled = true;
        
        initializeCategories();
        setupDefaultItems();
        
        LOG.info("MenuIntegration initialized");
    }
    
    /**
     * Initializes default menu categories.
     */
    private void initializeCategories() {
        // Continue Beans main category
        MenuCategory continueCategory = new MenuCategory("continue", "Continue Beans", 1, true);
        categories.put("continue", continueCategory);
        
        // AI Operations category
        MenuCategory aiCategory = new MenuCategory("ai", "AI Operations", 2, true);
        categories.put("ai", aiCategory);
        
        // Tools category
        MenuCategory toolsCategory = new MenuCategory("tools", "Tools", 3, true);
        categories.put("tools", toolsCategory);
        
        // Settings category
        MenuCategory settingsCategory = new MenuCategory("settings", "Settings", 4, true);
        categories.put("settings", settingsCategory);
    }
    
    /**
     * Sets up default menu items.
     */
    private void setupDefaultItems() {
        // Continue Beans category items
        MenuCategory continueCategory = categories.get("continue");
        
        MenuItem chatItem = new MenuItem(
            "chat", "Open Chat", "Open AI Chat window",
            e -> openChatWindow(), "ctrl+shift+c", 100, true, false, continueCategory
        );
        addItem(chatItem);
        
        MenuItem contextItem = new MenuItem(
            "context", "Show Context", "Show current project context",
            e -> showProjectContext(), "ctrl+shift+x", 90, true, false, continueCategory
        );
        addItem(contextItem);
        
        MenuItem clearChatItem = new MenuItem(
            "clear_chat", "Clear Chat", "Clear chat history",
            e -> clearChatHistory(), "ctrl+shift+l", 80, true, false, continueCategory
        );
        addItem(clearChatItem);
        
        // AI Operations category items
        MenuCategory aiCategory = categories.get("ai");
        
        MenuItem analyzeItem = new MenuItem(
            "analyze", "Analyze Code", "Analyze current code",
            e -> analyzeCurrentCode(), "ctrl+shift+a", 100, true, false, aiCategory
        );
        addItem(analyzeItem);
        
        MenuItem refactorItem = new MenuItem(
            "refactor", "Refactor", "Suggest refactoring",
            e -> suggestRefactoring(), "ctrl+shift+r", 90, true, false, aiCategory
        );
        addItem(refactorItem);
        
        MenuItem optimizeItem = new MenuItem(
            "optimize", "Optimize", "Optimize code performance",
            e -> optimizeCode(), "ctrl+shift+o", 85, true, false, aiCategory
        );
        addItem(optimizeItem);
        
        // Tools category items
        MenuCategory toolsCategory = categories.get("tools");
        
        MenuItem projectInfoItem = new MenuItem(
            "project_info", "Project Info", "Show project information",
            e -> showProjectInfo(), "ctrl+shift+i", 100, true, false, toolsCategory
        );
        addItem(projectInfoItem);
        
        MenuItem dependenciesItem = new MenuItem(
            "dependencies", "Dependencies", "Show project dependencies",
            e -> showDependencies(), "ctrl+shift+d", 90, true, false, toolsCategory
        );
        addItem(dependenciesItem);
        
        MenuItem metricsItem = new MenuItem(
            "metrics", "Code Metrics", "Show code quality metrics",
            e -> showCodeMetrics(), "ctrl+shift+m", 80, true, false, toolsCategory
        );
        addItem(metricsItem);
        
        // Settings category items
        MenuCategory settingsCategory = categories.get("settings");
        
        MenuItem preferencesItem = new MenuItem(
            "preferences", "Preferences", "Open Continue Beans preferences",
            e -> openPreferences(), "ctrl+shift+p", 100, true, false, settingsCategory
        );
        addItem(preferencesItem);
        
        MenuItem themeItem = new MenuItem(
            "theme", "Theme Settings", "Configure theme settings",
            e -> configureTheme(), "ctrl+shift+t", 90, true, false, settingsCategory
        );
        addItem(themeItem);
        
        MenuItem enableAIItem = new MenuItem(
            "enable_ai", "Enable AI", "Toggle AI features",
            e -> toggleAIFeatures(), "ctrl+shift+e", 85, true, true, settingsCategory
        );
        enableAIItem.setSelected(true);
        addItem(enableAIItem);
    }
    
    /**
     * Adds a menu category.
     * @param category The category to add
     */
    public void addCategory(MenuCategory category) {
        categories.put(category.getId(), category);
        rebuildMenuBar();
    }
    
    /**
     * Removes a menu category.
     * @param categoryId The category ID to remove
     */
    public void removeCategory(String categoryId) {
        categories.remove(categoryId);
        rebuildMenuBar();
    }
    
    /**
     * Adds a menu item.
     * @param item The item to add
     */
    public void addItem(MenuItem item) {
        items.put(item.getId(), item);
        item.getCategory().addItem(item);
        rebuildMenuBar();
    }
    
    /**
     * Removes a menu item.
     * @param itemId The item ID to remove
     */
    public void removeItem(String itemId) {
        MenuItem item = items.remove(itemId);
        if (item != null) {
            item.getCategory().getItems().remove(item);
            rebuildMenuBar();
        }
    }
    
    /**
     * Rebuilds the menu bar.
     */
    private void rebuildMenuBar() {
        if (menuBar == null) {
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            menuBar.removeAll();
            
            // Sort categories by position
            List<MenuCategory> sortedCategories = new ArrayList<>(categories.values());
            sortedCategories.sort((c1, c2) -> Integer.compare(c1.getPosition(), c2.getPosition()));
            
            for (MenuCategory category : sortedCategories) {
                if (!category.isEnabled()) {
                    continue;
                }
                
                JMenu menu = new JMenu(category.getDisplayName());
                
                for (MenuItem item : category.getItems()) {
                    JMenuItem menuItem = createMenuItem(item);
                    menu.add(menuItem);
                }
                
                menuBar.add(menu);
            }
            
            menuBar.revalidate();
            menuBar.repaint();
        });
    }
    
    /**
     * Creates a menu item from a MenuItem object.
     * @param item The MenuItem object
     * @return The JMenuItem
     */
    private JMenuItem createMenuItem(MenuItem item) {
        JMenuItem menuItem;
        
        if (item.isCheckable()) {
            menuItem = new JCheckBoxMenuItem(item.getDisplayName());
            ((JCheckBoxMenuItem) menuItem).setSelected(item.isSelected());
        } else {
            menuItem = new JMenuItem(item.getDisplayName());
        }
        
        menuItem.setToolTipText(item.getDescription());
        menuItem.setEnabled(item.isEnabled());
        
        if (item.getShortcut() != null && !item.getShortcut().isEmpty()) {
            try {
                KeyStroke keystroke = KeyStroke.getKeyStroke(item.getShortcut());
                menuItem.setAccelerator(keystroke);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Invalid shortcut: " + item.getShortcut(), e);
            }
        }
        
        menuItem.addActionListener(e -> {
            if (item.isCheckable()) {
                item.setSelected(!item.isSelected());
                ((JCheckBoxMenuItem) menuItem).setSelected(item.isSelected());
            }
            
            item.getAction().actionPerformed(e);
            notifyMenuItemSelected(item);
        });
        
        return menuItem;
    }
    
    /**
     * Creates the menu bar.
     * @return The created menu bar
     */
    public JMenuBar createMenuBar() {
        menuBar = new JMenuBar();
        rebuildMenuBar();
        return menuBar;
    }
    
    /**
     * Creates a context menu.
     * @param context The context (e.g., "editor", "project", "file")
     * @return The created context menu
     */
    public JPopupMenu createContextMenu(String context) {
        if (!contextMenuEnabled) {
            return null;
        }
        
        JPopupMenu contextMenu = new JPopupMenu();
        
        // Add relevant items based on context
        for (MenuItem item : items.values()) {
            if (isRelevantForContext(item, context)) {
                JMenuItem menuItem = createMenuItem(item);
                contextMenu.add(menuItem);
            }
        }
        
        if (contextMenu.getComponentCount() > 0) {
            contextMenu.addSeparator();
        }
        
        // Add common context items
        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.addActionListener(e -> refreshContext(context));
        contextMenu.add(refreshItem);
        
        return contextMenu;
    }
    
    /**
     * Checks if an item is relevant for a specific context.
     * @param item The menu item
     * @param context The context
     * @return True if relevant
     */
    private boolean isRelevantForContext(MenuItem item, String context) {
        // Simple relevance check - can be extended
        switch (context.toLowerCase()) {
            case "editor":
                return item.getId().equals("analyze") || 
                       item.getId().equals("refactor") || 
                       item.getId().equals("optimize");
            case "project":
                return item.getId().equals("project_info") || 
                       item.getId().equals("dependencies") || 
                       item.getId().equals("metrics");
            case "file":
                return item.getId().equals("analyze") || 
                       item.getId().equals("context");
            default:
                return true;
        }
    }
    
    /**
     * Adds a menu listener.
     * @param listener The listener to add
     */
    public void addMenuListener(MenuListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes a menu listener.
     * @param listener The listener to remove
     */
    public void removeMenuListener(MenuListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notifies listeners about menu item selection.
     * @param item The selected item
     */
    private void notifyMenuItemSelected(MenuItem item) {
        for (MenuListener listener : listeners) {
            try {
                listener.menuItemSelected(item);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying menu listener", e);
            }
        }
    }
    
    /**
     * Enables or disables context menus.
     * @param enabled Whether to enable context menus
     */
    public void setContextMenuEnabled(boolean enabled) {
        this.contextMenuEnabled = enabled;
    }
    
    /**
     * Gets all menu categories.
     * @return Copy of categories
     */
    public Map<String, MenuCategory> getCategories() {
        return new HashMap<>(categories);
    }
    
    /**
     * Gets all menu items.
     * @return Copy of items
     */
    public Map<String, MenuItem> getItems() {
        return new HashMap<>(items);
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
    
    private void clearChatHistory() {
        // Implementation would clear chat history
        LOG.info("Chat history cleared");
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
    
    private void showDependencies() {
        LOG.info("Showing dependencies");
    }
    
    private void showCodeMetrics() {
        LOG.info("Showing code metrics");
    }
    
    private void openPreferences() {
        LOG.info("Opening preferences");
    }
    
    private void configureTheme() {
        LOG.info("Configuring theme");
    }
    
    private void toggleAIFeatures() {
        LOG.info("Toggling AI features");
    }
    
    private void refreshContext(String context) {
        LOG.info("Refreshing context: " + context);
    }
}
