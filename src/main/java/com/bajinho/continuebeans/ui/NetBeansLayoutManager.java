package com.bajinho.continuebeans.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.HashMap;
import java.util.Map;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Advanced layout manager for NetBeans windows with intelligent positioning
 * and adaptive layout based on content and screen size.
 * 
 * @author Continue Beans Team
 */
public class NetBeansLayoutManager implements LayoutManager2 {
    
    private final Map<String, LayoutConstraint> constraints;
    private final WindowManager windowManager;
    private int screenWidth;
    private int screenHeight;
    
    /**
     * Layout constraint types for positioning components.
     */
    public enum ConstraintType {
        NORTH, SOUTH, EAST, WEST, CENTER, 
        FLOATING, DOCKED, TABBED, STACKED
    }
    
    /**
     * Layout constraint with position and size information.
     */
    public static class LayoutConstraint {
        private ConstraintType type;
        private double xRatio;
        private double yRatio;
        private double widthRatio;
        private double heightRatio;
        private int minWidth;
        private int minHeight;
        private int maxWidth;
        private int maxHeight;
        private boolean resizable;
        private String modeId;
        
        public LayoutConstraint(ConstraintType type) {
            this.type = type;
            this.xRatio = 0.0;
            this.yRatio = 0.0;
            this.widthRatio = 1.0;
            this.heightRatio = 1.0;
            this.minWidth = 100;
            this.minHeight = 100;
            this.maxWidth = Integer.MAX_VALUE;
            this.maxHeight = Integer.MAX_VALUE;
            this.resizable = true;
        }
        
        // Getters and setters
        public ConstraintType getType() { return type; }
        public void setType(ConstraintType type) { this.type = type; }
        
        public double getXRatio() { return xRatio; }
        public void setXRatio(double xRatio) { this.xRatio = xRatio; }
        
        public double getYRatio() { return yRatio; }
        public void setYRatio(double yRatio) { this.yRatio = yRatio; }
        
        public double getWidthRatio() { return widthRatio; }
        public void setWidthRatio(double widthRatio) { this.widthRatio = widthRatio; }
        
        public double getHeightRatio() { return heightRatio; }
        public void setHeightRatio(double heightRatio) { this.heightRatio = heightRatio; }
        
        public int getMinWidth() { return minWidth; }
        public void setMinWidth(int minWidth) { this.minWidth = minWidth; }
        
        public int getMinHeight() { return minHeight; }
        public void setMinHeight(int minHeight) { this.minHeight = minHeight; }
        
        public int getMaxWidth() { return maxWidth; }
        public void setMaxWidth(int maxWidth) { this.maxWidth = maxWidth; }
        
        public int getMaxHeight() { return maxHeight; }
        public void setMaxHeight(int maxHeight) { this.maxHeight = maxHeight; }
        
        public boolean isResizable() { return resizable; }
        public void setResizable(boolean resizable) { this.resizable = resizable; }
        
        public String getModeId() { return modeId; }
        public void setModeId(String modeId) { this.modeId = modeId; }
    }
    
    /**
     * Creates a new NetBeansLayoutManager.
     */
    public NetBeansLayoutManager() {
        this.constraints = new HashMap<>();
        this.windowManager = WindowManager.getDefault();
        updateScreenSize();
    }
    
    /**
     * Updates the current screen size.
     */
    private void updateScreenSize() {
        try {
            java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
            java.awt.GraphicsDevice gd = ge.getDefaultScreenDevice();
            java.awt.DisplayMode dm = gd.getDisplayMode();
            this.screenWidth = dm.getWidth();
            this.screenHeight = dm.getHeight();
        } catch (Exception e) {
            // Fallback to reasonable defaults
            this.screenWidth = 1920;
            this.screenHeight = 1080;
        }
    }
    
    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        if (comp instanceof TopComponent && constraints instanceof LayoutConstraint) {
            String key = ((TopComponent) comp).getName();
            this.constraints.put(key, (LayoutConstraint) constraints);
        }
    }
    
    @Override
    public void addLayoutComponent(String name, Component comp) {
        // Legacy method - not used
    }
    
    @Override
    public void removeLayoutComponent(Component comp) {
        if (comp instanceof TopComponent) {
            String key = ((TopComponent) comp).getName();
            constraints.remove(key);
        }
    }
    
    @Override
    public Dimension preferredLayoutSize(Container parent) {
        updateScreenSize();
        return new Dimension(screenWidth, screenHeight);
    }
    
    @Override
    public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(800, 600);
    }
    
    @Override
    public Dimension maximumLayoutSize(Container target) {
        return new Dimension(screenWidth, screenHeight);
    }
    
    @Override
    public float getLayoutAlignmentX(Container target) {
        return 0.5f;
    }
    
    @Override
    public float getLayoutAlignmentY(Container target) {
        return 0.5f;
    }
    
    @Override
    public void invalidateLayout(Container target) {
        updateScreenSize();
    }
    
    @Override
    public void layoutContainer(Container parent) {
        updateScreenSize();
        
        for (Component comp : parent.getComponents()) {
            if (comp instanceof TopComponent) {
                String key = ((TopComponent) comp).getName();
                LayoutConstraint constraint = constraints.get(key);
                
                if (constraint != null) {
                    layoutComponent(comp, constraint);
                }
            }
        }
    }
    
    /**
     * Layouts a single component based on its constraint.
     * @param comp The component to layout
     * @param constraint The layout constraint
     */
    private void layoutComponent(Component comp, LayoutConstraint constraint) {
        int x = (int) (screenWidth * constraint.getXRatio());
        int y = (int) (screenHeight * constraint.getYRatio());
        int width = (int) (screenWidth * constraint.getWidthRatio());
        int height = (int) (screenHeight * constraint.getHeightRatio());
        
        // Apply size constraints
        width = Math.max(constraint.getMinWidth(), Math.min(constraint.getMaxWidth(), width));
        height = Math.max(constraint.getMinHeight(), Math.min(constraint.getMaxHeight(), height));
        
        // Apply constraint type positioning
        switch (constraint.getType()) {
            case NORTH:
                y = 0;
                height = (int) (screenHeight * 0.25);
                break;
            case SOUTH:
                y = screenHeight - height;
                height = (int) (screenHeight * 0.25);
                break;
            case EAST:
                x = screenWidth - width;
                width = (int) (screenWidth * 0.25);
                break;
            case WEST:
                x = 0;
                width = (int) (screenWidth * 0.25);
                break;
            case CENTER:
                // Use calculated values
                break;
            case FLOATING:
                // Floating windows are positioned as calculated
                break;
            case DOCKED:
                // Dock to specified mode
                dockToMode((TopComponent) comp, constraint.getModeId());
                return;
            case TABBED:
                // Add to tabbed mode
                addToTabbedMode((TopComponent) comp, constraint.getModeId());
                return;
            case STACKED:
                // Stack in mode
                stackInMode((TopComponent) comp, constraint.getModeId());
                return;
        }
        
        comp.setBounds(x, y, width, height);
        comp.setPreferredSize(new Dimension(width, height));
        comp.setMinimumSize(new Dimension(constraint.getMinWidth(), constraint.getMinHeight()));
        comp.setMaximumSize(new Dimension(constraint.getMaxWidth(), constraint.getMaxHeight()));
    }
    
    /**
     * Docks a TopComponent to a specific NetBeans mode.
     * @param tc The TopComponent to dock
     * @param modeId The mode ID
     */
    private void dockToMode(TopComponent tc, String modeId) {
        if (modeId != null) {
            Mode mode = windowManager.findMode(modeId);
            if (mode != null) {
                mode.dockInto(tc);
            }
        }
    }
    
    /**
     * Adds a TopComponent to a tabbed mode.
     * @param tc The TopComponent to add
     * @param modeId The mode ID
     */
    private void addToTabbedMode(TopComponent tc, String modeId) {
        if (modeId != null) {
            Mode mode = windowManager.findMode(modeId);
            if (mode != null) {
                mode.dockInto(tc);
                tc.open();
            }
        }
    }
    
    /**
     * Stacks a TopComponent in a mode.
     * @param tc The TopComponent to stack
     * @param modeId The mode ID
     */
    private void stackInMode(TopComponent tc, String modeId) {
        if (modeId != null) {
            Mode mode = windowManager.findMode(modeId);
            if (mode != null) {
                mode.dockInto(tc);
                tc.open();
            }
        }
    }
    
    /**
     * Creates a constraint for the North position.
     * @return A North constraint
     */
    public static LayoutConstraint northConstraint() {
        LayoutConstraint constraint = new LayoutConstraint(ConstraintType.NORTH);
        constraint.setXRatio(0.0);
        constraint.setYRatio(0.0);
        constraint.setWidthRatio(1.0);
        constraint.setHeightRatio(0.25);
        constraint.setMinHeight(100);
        constraint.setMaxHeight(300);
        return constraint;
    }
    
    /**
     * Creates a constraint for the South position.
     * @return A South constraint
     */
    public static LayoutConstraint southConstraint() {
        LayoutConstraint constraint = new LayoutConstraint(ConstraintType.SOUTH);
        constraint.setXRatio(0.0);
        constraint.setYRatio(0.75);
        constraint.setWidthRatio(1.0);
        constraint.setHeightRatio(0.25);
        constraint.setMinHeight(100);
        constraint.setMaxHeight(300);
        return constraint;
    }
    
    /**
     * Creates a constraint for the East position.
     * @return An East constraint
     */
    public static LayoutConstraint eastConstraint() {
        LayoutConstraint constraint = new LayoutConstraint(ConstraintType.EAST);
        constraint.setXRatio(0.75);
        constraint.setYRatio(0.0);
        constraint.setWidthRatio(0.25);
        constraint.setHeightRatio(1.0);
        constraint.setMinWidth(200);
        constraint.setMaxWidth(400);
        return constraint;
    }
    
    /**
     * Creates a constraint for the West position.
     * @return A West constraint
     */
    public static LayoutConstraint westConstraint() {
        LayoutConstraint constraint = new LayoutConstraint(ConstraintType.WEST);
        constraint.setXRatio(0.0);
        constraint.setYRatio(0.0);
        constraint.setWidthRatio(0.25);
        constraint.setHeightRatio(1.0);
        constraint.setMinWidth(200);
        constraint.setMaxWidth(400);
        return constraint;
    }
    
    /**
     * Creates a constraint for the Center position.
     * @return A Center constraint
     */
    public static LayoutConstraint centerConstraint() {
        LayoutConstraint constraint = new LayoutConstraint(ConstraintType.CENTER);
        constraint.setXRatio(0.25);
        constraint.setYRatio(0.25);
        constraint.setWidthRatio(0.5);
        constraint.setHeightRatio(0.5);
        constraint.setMinWidth(400);
        constraint.setMinHeight(300);
        return constraint;
    }
    
    /**
     * Creates a constraint for floating windows.
     * @param xRatio X position ratio (0.0 to 1.0)
     * @param yRatio Y position ratio (0.0 to 1.0)
     * @param widthRatio Width ratio (0.0 to 1.0)
     * @param heightRatio Height ratio (0.0 to 1.0)
     * @return A floating constraint
     */
    public static LayoutConstraint floatingConstraint(double xRatio, double yRatio, 
                                                   double widthRatio, double heightRatio) {
        LayoutConstraint constraint = new LayoutConstraint(ConstraintType.FLOATING);
        constraint.setXRatio(Math.max(0.0, Math.min(1.0, xRatio)));
        constraint.setYRatio(Math.max(0.0, Math.min(1.0, yRatio)));
        constraint.setWidthRatio(Math.max(0.1, Math.min(1.0, widthRatio)));
        constraint.setHeightRatio(Math.max(0.1, Math.min(1.0, heightRatio)));
        constraint.setMinWidth(200);
        constraint.setMinHeight(150);
        return constraint;
    }
    
    /**
     * Creates a constraint for docking to a specific mode.
     * @param modeId The NetBeans mode ID
     * @return A docked constraint
     */
    public static LayoutConstraint dockedConstraint(String modeId) {
        LayoutConstraint constraint = new LayoutConstraint(ConstraintType.DOCKED);
        constraint.setModeId(modeId);
        return constraint;
    }
    
    /**
     * Creates a constraint for tabbed layout.
     * @param modeId The NetBeans mode ID
     * @return A tabbed constraint
     */
    public static LayoutConstraint tabbedConstraint(String modeId) {
        LayoutConstraint constraint = new LayoutConstraint(ConstraintType.TABBED);
        constraint.setModeId(modeId);
        return constraint;
    }
    
    /**
     * Gets the constraint for a component.
     * @param componentName The component name
     * @return The constraint or null if not found
     */
    public LayoutConstraint getConstraint(String componentName) {
        return constraints.get(componentName);
    }
    
    /**
     * Sets the constraint for a component.
     * @param componentName The component name
     * @param constraint The constraint to set
     */
    public void setConstraint(String componentName, LayoutConstraint constraint) {
        constraints.put(componentName, constraint);
    }
    
    /**
     * Removes the constraint for a component.
     * @param componentName The component name
     * @return The removed constraint or null if not found
     */
    public LayoutConstraint removeConstraint(String componentName) {
        return constraints.remove(componentName);
    }
    
    /**
     * Gets all constraints.
     * @return A copy of the constraints map
     */
    public Map<String, LayoutConstraint> getAllConstraints() {
        return new HashMap<>(constraints);
    }
    
    /**
     * Clears all constraints.
     */
    public void clearConstraints() {
        constraints.clear();
    }
    
    /**
     * Applies an adaptive layout based on screen size.
     * @param smallScreenThreshold Threshold for small screens
     * @param largeScreenThreshold Threshold for large screens
     */
    public void applyAdaptiveLayout(int smallScreenThreshold, int largeScreenThreshold) {
        int totalWidth = screenWidth;
        
        if (totalWidth < smallScreenThreshold) {
            // Small screen - stack vertically
            applySmallScreenLayout();
        } else if (totalWidth > largeScreenThreshold) {
            // Large screen - use full layout
            applyLargeScreenLayout();
        } else {
            // Medium screen - balanced layout
            applyMediumScreenLayout();
        }
    }
    
    /**
     * Applies layout for small screens.
     */
    private void applySmallScreenLayout() {
        for (Map.Entry<String, LayoutConstraint> entry : constraints.entrySet()) {
            LayoutConstraint constraint = entry.getValue();
            
            switch (constraint.getType()) {
                case EAST:
                case WEST:
                    // Move east/west components to center on small screens
                    constraint.setType(ConstraintType.CENTER);
                    constraint.setWidthRatio(0.8);
                    constraint.setHeightRatio(0.3);
                    break;
                case NORTH:
                case SOUTH:
                    // Reduce height of north/south components
                    constraint.setHeightRatio(0.2);
                    break;
                case CENTER:
                case FLOATING:
                case DOCKED:
                case TABBED:
                case STACKED:
                    // Keep other types as-is for small screens
                    break;
            }
        }
    }
    
    /**
     * Applies layout for medium screens.
     */
    private void applyMediumScreenLayout() {
        // Use default constraints for medium screens
    }
    
    /**
     * Applies layout for large screens.
     */
    private void applyLargeScreenLayout() {
        // Use default constraints for large screens
    }
}
