package com.bajinho.continuebeans.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;

/**
 * Smart toolbar with contextual actions and adaptive layout.
 * Provides intelligent tool selection based on current context and user behavior.
 * 
 * @author Continue Beans Team
 */
public class SmartToolbar extends JPanel {
    
    private static final Logger LOG = Logger.getLogger(SmartToolbar.class.getName());
    
    private final Map<String, ToolbarAction> actions;
    private final List<ToolbarSection> sections;
    private JPanel mainPanel;
    private JPanel leftPanel;
    private JPanel centerPanel;
    private JPanel rightPanel;
    
    private String currentContext;
    private boolean adaptiveMode;
    private int maxVisibleActions;
    
    /**
     * Represents a toolbar action with metadata.
     */
    public static class ToolbarAction {
        private final String id;
        private final String displayName;
        private final String description;
        private final Icon icon;
        private final ActionListener action;
        private final String[] contexts;
        private final int priority;
        private final boolean toggleable;
        private boolean selected;
        
        public ToolbarAction(String id, String displayName, String description, 
                           Icon icon, ActionListener action, String[] contexts, 
                           int priority, boolean toggleable) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
            this.action = action;
            this.contexts = contexts != null ? contexts : new String[0];
            this.priority = priority;
            this.toggleable = toggleable;
            this.selected = false;
        }
        
        // Getters
        public String getId() { return id; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public Icon getIcon() { return icon; }
        public ActionListener getAction() { return action; }
        public String[] getContexts() { return contexts; }
        public int getPriority() { return priority; }
        public boolean isToggleable() { return toggleable; }
        public boolean isSelected() { return selected; }
        
        public void setSelected(boolean selected) {
            this.selected = selected;
        }
        
        /**
         * Checks if this action is relevant for the given context.
         * @param context The context to check
         * @return True if relevant
         */
        public boolean isRelevantForContext(String context) {
            if (contexts.length == 0) {
                return true; // Action is relevant for all contexts
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
     * Represents a section in the toolbar.
     */
    public static class ToolbarSection {
        private final String name;
        private final List<ToolbarAction> actions;
        private final boolean collapsible;
        private final boolean visible;
        private final int position;
        
        public ToolbarSection(String name, int position, boolean collapsible) {
            this.name = name;
            this.position = position;
            this.collapsible = collapsible;
            this.actions = new ArrayList<>();
            this.visible = true;
        }
        
        public void addAction(ToolbarAction action) {
            actions.add(action);
            actions.sort((a1, a2) -> Integer.compare(a2.getPriority(), a1.getPriority()));
        }
        
        // Getters
        public String getName() { return name; }
        public List<ToolbarAction> getActions() { return actions; }
        public boolean isCollapsible() { return collapsible; }
        public boolean isVisible() { return visible; }
        public int getPosition() { return position; }
    }
    
    /**
     * Creates a new SmartToolbar.
     */
    public SmartToolbar() {
        this.actions = new HashMap<>();
        this.sections = new ArrayList<>();
        this.currentContext = "default";
        this.adaptiveMode = true;
        this.maxVisibleActions = 10;
        
        initializeComponents();
        layoutComponents();
        setupDefaultActions();
    }
    
    /**
     * Initializes UI components.
     */
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        
        mainPanel = new JPanel(new BorderLayout());
        leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
        rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        
        mainPanel.add(leftPanel, BorderLayout.WEST);
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(rightPanel, BorderLayout.EAST);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Layouts components.
     */
    private void layoutComponents() {
        // Layout is handled by updateToolbar()
    }
    
    /**
     * Sets up default actions.
     */
    private void setupDefaultActions() {
        // File operations section
        ToolbarSection fileSection = new ToolbarSection("File", 1, false);
        fileSection.addAction(new ToolbarAction(
            "new", "New", "Create new file", 
            loadIcon("org/openide/resources/newFile.png"),
            e -> LOG.info("New file action"),
            new String[]{"editor", "project"}, 100, false
        ));
        fileSection.addAction(new ToolbarAction(
            "open", "Open", "Open file", 
            loadIcon("org/openide/resources/openFile.png"),
            e -> LOG.info("Open file action"),
            new String[]{"editor", "project"}, 90, false
        ));
        fileSection.addAction(new ToolbarAction(
            "save", "Save", "Save file", 
            loadIcon("org/openide/resources/saveFile.png"),
            e -> LOG.info("Save file action"),
            new String[]{"editor"}, 95, false
        ));
        addSection(fileSection);
        
        // Edit operations section
        ToolbarSection editSection = new ToolbarSection("Edit", 2, true);
        editSection.addAction(new ToolbarAction(
            "undo", "Undo", "Undo last action", 
            loadIcon("org/openide/resources/undo.png"),
            e -> LOG.info("Undo action"),
            new String[]{"editor"}, 80, false
        ));
        editSection.addAction(new ToolbarAction(
            "redo", "Redo", "Redo last action", 
            loadIcon("org/openide/resources/redo.png"),
            e -> LOG.info("Redo action"),
            new String[]{"editor"}, 79, false
        ));
        addSection(editSection);
        
        // Continue Beans section
        ToolbarSection continueSection = new ToolbarSection("Continue Beans", 3, false);
        continueSection.addAction(new ToolbarAction(
            "chat", "Chat", "Open AI Chat", 
            loadIcon("com/bajinho/continuebeans/resources/chat.png"),
            e -> openContinueChat(),
            new String[]{"all"}, 100, true
        ));
        continueSection.addAction(new ToolbarAction(
            "context", "Context", "Show project context", 
            loadIcon("com/bajinho/continuebeans/resources/context.png"),
            e -> showProjectContext(),
            new String[]{"project", "editor"}, 90, false
        ));
        continueSection.addAction(new ToolbarAction(
            "codebase", "Codebase", "Analyze codebase", 
            loadIcon("com/bajinho/continuebeans/resources/codebase.png"),
            e -> analyzeCodebase(),
            new String[]{"project"}, 85, false
        ));
        addSection(continueSection);
        
        updateToolbar();
    }
    
    /**
     * Loads an icon from the NetBeans icon repository.
     * @param path The icon path
     * @return The loaded icon or null
     */
    private Icon loadIcon(String path) {
        try {
            return ImageUtilities.loadImageIcon(path, false);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to load icon: " + path, e);
            // Return a simple placeholder icon
            return new ImageIcon(createPlaceholderIcon());
        }
    }
    
    /**
     * Creates a placeholder icon.
     * @return Placeholder icon image
     */
    private java.awt.Image createPlaceholderIcon() {
        int size = 16;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(
            size, size, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2d = image.createGraphics();
        g2d.setColor(java.awt.Color.GRAY);
        g2d.fillRect(0, 0, size, size);
        g2d.setColor(java.awt.Color.WHITE);
        g2d.drawRect(2, 2, size-4, size-4);
        g2d.dispose();
        return image;
    }
    
    /**
     * Adds a toolbar section.
     * @param section The section to add
     */
    public void addSection(ToolbarSection section) {
        sections.add(section);
        sections.sort((s1, s2) -> Integer.compare(s1.getPosition(), s2.getPosition()));
        updateToolbar();
    }
    
    /**
     * Removes a toolbar section.
     * @param sectionName The section name to remove
     */
    public void removeSection(String sectionName) {
        sections.removeIf(section -> section.getName().equals(sectionName));
        updateToolbar();
    }
    
    /**
     * Adds an action to a specific section.
     * @param sectionName The section name
     * @param action The action to add
     */
    public void addAction(String sectionName, ToolbarAction action) {
        actions.put(action.getId(), action);
        for (ToolbarSection section : sections) {
            if (section.getName().equals(sectionName)) {
                section.addAction(action);
                updateToolbar();
                return;
            }
        }
        // Create new section if not found
        ToolbarSection newSection = new ToolbarSection(sectionName, sections.size() + 1, true);
        newSection.addAction(action);
        addSection(newSection);
    }
    
    /**
     * Removes an action.
     * @param actionId The action ID to remove
     */
    public void removeAction(String actionId) {
        actions.remove(actionId);
        for (ToolbarSection section : sections) {
            section.getActions().removeIf(action -> action.getId().equals(actionId));
        }
        updateToolbar();
    }
    
    /**
     * Updates the toolbar display based on current context.
     */
    public void updateToolbar() {
        SwingUtilities.invokeLater(() -> {
            // Clear all panels
            leftPanel.removeAll();
            centerPanel.removeAll();
            rightPanel.removeAll();
            
            // Add actions based on context and priority
            List<ToolbarAction> visibleActions = getVisibleActions();
            
            if (adaptiveMode && visibleActions.size() > maxVisibleActions) {
                visibleActions = visibleActions.subList(0, maxVisibleActions);
            }
            
            // Distribute actions across panels
            int leftCount = visibleActions.size() / 3;
            int centerCount = visibleActions.size() / 3;
            
            for (int i = 0; i < visibleActions.size(); i++) {
                ToolbarAction action = visibleActions.get(i);
                Component button = createActionButton(action);
                
                if (i < leftCount) {
                    leftPanel.add(button);
                } else if (i < leftCount + centerCount) {
                    centerPanel.add(button);
                } else {
                    rightPanel.add(button);
                }
            }
            
            revalidate();
            repaint();
        });
    }
    
    /**
     * Gets visible actions based on current context.
     * @return List of visible actions
     */
    private List<ToolbarAction> getVisibleActions() {
        List<ToolbarAction> visible = new ArrayList<>();
        
        for (ToolbarSection section : sections) {
            if (!section.isVisible()) {
                continue;
            }
            
            for (ToolbarAction action : section.getActions()) {
                if (action.isRelevantForContext(currentContext)) {
                    visible.add(action);
                }
            }
        }
        
        // Sort by priority
        visible.sort((a1, a2) -> Integer.compare(a2.getPriority(), a1.getPriority()));
        
        return visible;
    }
    
    /**
     * Creates a button for a toolbar action.
     * @param action The action
     * @return The button component
     */
    private Component createActionButton(ToolbarAction action) {
        if (action.isToggleable()) {
            JToggleButton toggleButton = new JToggleButton(action.getIcon());
            toggleButton.setToolTipText(action.getDisplayName() + " - " + action.getDescription());
            toggleButton.setSelected(action.isSelected());
            toggleButton.addActionListener(e -> {
                action.setSelected(!action.isSelected());
                action.getAction().actionPerformed(e);
                updateToolbar(); // Refresh to show selection state
            });
            return toggleButton;
        } else {
            JButton button = new JButton(action.getIcon());
            button.setToolTipText(action.getDisplayName() + " - " + action.getDescription());
            button.addActionListener(action.getAction());
            return button;
        }
    }
    
    /**
     * Sets the current context.
     * @param context The new context
     */
    public void setCurrentContext(String context) {
        this.currentContext = context;
        updateToolbar();
        LOG.info("Toolbar context changed to: " + context);
    }
    
    /**
     * Gets the current context.
     * @return The current context
     */
    public String getCurrentContext() {
        return currentContext;
    }
    
    /**
     * Enables or disables adaptive mode.
     * @param adaptive Whether to use adaptive mode
     */
    public void setAdaptiveMode(boolean adaptive) {
        this.adaptiveMode = adaptive;
        updateToolbar();
    }
    
    /**
     * Sets the maximum number of visible actions.
     * @param maxVisible The maximum number
     */
    public void setMaxVisibleActions(int maxVisible) {
        this.maxVisibleActions = Math.max(1, maxVisible);
        updateToolbar();
    }
    
    /**
     * Shows or hides a section.
     * @param sectionName The section name
     * @param visible Whether to show the section
     */
    public void setSectionVisible(String sectionName, boolean visible) {
        for (ToolbarSection section : sections) {
            if (section.getName().equals(sectionName)) {
                // Note: This would require making ToolbarSection mutable
                updateToolbar();
                return;
            }
        }
    }
    
    /**
     * Opens the Continue Chat window.
     */
    private void openContinueChat() {
        try {
            // Find and open the ContinueTopComponent
            TopComponent chatTC = org.openide.windows.WindowManager.getDefault()
                .findTopComponent("ContinueTopComponent");
            if (chatTC != null) {
                chatTC.open();
                chatTC.requestActive();
            } else {
                LOG.warning("ContinueTopComponent not found");
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to open Continue Chat", e);
        }
    }
    
    /**
     * Shows project context information.
     */
    private void showProjectContext() {
        CompletableFuture.runAsync(() -> {
            try {
                // Create a dynamic window with project context
                DynamicTopComponent contextWindow = new DynamicTopComponent(
                    "project-context", 
                    DynamicTopComponent.commandContentProvider("pwd", "ls", "-la")
                );
                contextWindow.setWindowTitle("Project Context");
                contextWindow.setWindowSize(600, 400);
                
                // Open the window
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
    
    /**
     * Analyzes the codebase.
     */
    private void analyzeCodebase() {
        CompletableFuture.runAsync(() -> {
            try {
                // Create a dynamic window with codebase analysis
                DynamicTopComponent analysisWindow = new DynamicTopComponent(
                    "codebase-analysis",
                    DynamicTopComponent.staticContentProvider(
                        "Analyzing codebase...\n\n" +
                        "This feature will provide:\n" +
                        "- Project structure analysis\n" +
                        "- Code quality metrics\n" +
                        "- Dependency analysis\n" +
                        "- Security vulnerabilities\n" +
                        "- Performance bottlenecks"
                    )
                );
                analysisWindow.setWindowTitle("Codebase Analysis");
                analysisWindow.setWindowSize(800, 600);
                
                // Open the window
                NetBeansWindowManager.getInstance().openTopComponentAsync(analysisWindow, "editor")
                    .thenAccept(success -> {
                        if (success) {
                            LOG.info("Codebase analysis window opened");
                        }
                    });
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to analyze codebase", e);
            }
        });
    }
    
    /**
     * Gets all actions.
     * @return Copy of all actions
     */
    public Map<String, ToolbarAction> getAllActions() {
        return new HashMap<>(actions);
    }
    
    /**
     * Gets all sections.
     * @return Copy of all sections
     */
    public List<ToolbarSection> getAllSections() {
        return new ArrayList<>(sections);
    }
    
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(super.getPreferredSize().width, 32); // Standard toolbar height
    }
    
    @Override
    public Dimension getMaximumSize() {
        return new Dimension(Integer.MAX_VALUE, 32);
    }
    
    @Override
    public Dimension getMinimumSize() {
        return new Dimension(100, 32);
    }
}
