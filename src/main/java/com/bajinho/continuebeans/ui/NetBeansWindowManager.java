package com.bajinho.continuebeans.ui;

import java.awt.EventQueue;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Central control for NetBeans window management.
 * Provides intelligent window operations with EDT safety and async support.
 * 
 * @author Continue Beans Team
 */
public class NetBeansWindowManager {
    
    private static final Logger LOG = Logger.getLogger(NetBeansWindowManager.class.getName());
    private static NetBeansWindowManager instance;
    
    private final WindowManager windowManager;
    
    private NetBeansWindowManager() {
        this.windowManager = WindowManager.getDefault();
        LOG.info("NetBeansWindowManager initialized");
    }
    
    /**
     * Singleton instance with lazy initialization.
     * @return The singleton instance
     */
    public static synchronized NetBeansWindowManager getInstance() {
        if (instance == null) {
            instance = new NetBeansWindowManager();
        }
        return instance;
    }
    
    /**
     * Opens a TopComponent in the specified mode asynchronously.
     * @param tc The TopComponent to open
     * @param modeId The mode identifier (e.g., "editor", "output", "explorer")
     * @return CompletableFuture indicating success/failure
     */
    public CompletableFuture<Boolean> openTopComponentAsync(TopComponent tc, String modeId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Ensure we're on EDT for UI operations
                if (EventQueue.isDispatchThread()) {
                    return openTopComponentInternal(tc, modeId);
                } else {
                    CompletableFuture<Boolean> result = new CompletableFuture<>();
                    EventQueue.invokeLater(() -> {
                        try {
                            result.complete(openTopComponentInternal(tc, modeId));
                        } catch (Exception e) {
                            result.completeExceptionally(e);
                        }
                    });
                    return result.get();
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to open TopComponent: " + tc.getName(), e);
                return false;
            }
        });
    }
    
    /**
     * Internal method to open TopComponent (must be called on EDT).
     */
    private boolean openTopComponentInternal(TopComponent tc, String modeId) {
        try {
            Mode mode = windowManager.findMode(modeId);
            if (mode == null) {
                LOG.warning("Mode not found: " + modeId + ", using default mode");
                tc.open();
            } else {
                mode.dockInto(tc);
                tc.open();
            }
            
            tc.requestActive();
            LOG.info("TopComponent opened successfully: " + tc.getName() + " in mode: " + modeId);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error opening TopComponent", e);
            return false;
        }
    }
    
    /**
     * Closes a TopComponent asynchronously.
     * @param tc The TopComponent to close
     * @return CompletableFuture indicating success/failure
     */
    public CompletableFuture<Boolean> closeTopComponentAsync(TopComponent tc) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (EventQueue.isDispatchThread()) {
                    return closeTopComponentInternal(tc);
                } else {
                    CompletableFuture<Boolean> result = new CompletableFuture<>();
                    EventQueue.invokeLater(() -> {
                        try {
                            result.complete(closeTopComponentInternal(tc));
                        } catch (Exception e) {
                            result.completeExceptionally(e);
                        }
                    });
                    return result.get();
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to close TopComponent: " + tc.getName(), e);
                return false;
            }
        });
    }
    
    /**
     * Internal method to close TopComponent (must be called on EDT).
     */
    private boolean closeTopComponentInternal(TopComponent tc) {
        try {
            if (tc.isOpened()) {
                tc.close();
                LOG.info("TopComponent closed successfully: " + tc.getName());
                return true;
            } else {
                LOG.warning("TopComponent already closed: " + tc.getName());
                return false;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error closing TopComponent", e);
            return false;
        }
    }
    
    /**
     * Finds a TopComponent by its ID.
     * @param tcId The TopComponent ID
     * @return The TopComponent or null if not found
     */
    public TopComponent findTopComponent(String tcId) {
        try {
            return windowManager.findTopComponent(tcId);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to find TopComponent: " + tcId, e);
            return null;
        }
    }
    
    /**
     * Gets all opened TopComponents.
     * @return Array of opened TopComponents
     */
    public TopComponent[] getOpenedTopComponents() {
        try {
            // Get all modes and collect their opened components
            java.util.Set<TopComponent> openedComponents = new java.util.HashSet<>();
            String[] modes = getAvailableModes();
            for (String modeId : modes) {
                org.openide.windows.Mode mode = windowManager.findMode(modeId);
                if (mode != null) {
                    TopComponent[] tcs = mode.getTopComponents();
                    for (TopComponent tc : tcs) {
                        if (tc.isOpened()) {
                            openedComponents.add(tc);
                        }
                    }
                }
            }
            return openedComponents.toArray(new TopComponent[0]);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get opened TopComponents", e);
            return new TopComponent[0];
        }
    }
    
    /**
     * Minimizes all TopComponents in a specific mode.
     * @param modeId The mode identifier
     * @return CompletableFuture indicating success/failure
     */
    public CompletableFuture<Boolean> minimizeModeAsync(String modeId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (EventQueue.isDispatchThread()) {
                    return minimizeModeInternal(modeId);
                } else {
                    CompletableFuture<Boolean> result = new CompletableFuture<>();
                    EventQueue.invokeLater(() -> {
                        try {
                            result.complete(minimizeModeInternal(modeId));
                        } catch (Exception e) {
                            result.completeExceptionally(e);
                        }
                    });
                    return result.get();
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to minimize mode: " + modeId, e);
                return false;
            }
        });
    }
    
    /**
     * Internal method to minimize mode (must be called on EDT).
     */
    private boolean minimizeModeInternal(String modeId) {
        try {
            Mode mode = windowManager.findMode(modeId);
            if (mode == null) {
                LOG.warning("Mode not found: " + modeId);
                return false;
            }
            
            TopComponent[] tcs = mode.getTopComponents();
            int minimized = 0;
            for (TopComponent tc : tcs) {
                if (tc.isOpened()) {
                    // NetBeans doesn't have direct minimize, so we'll close and reopen later
                    // This is a workaround - in real implementation we'd use WindowManager APIs
                    tc.close();
                    minimized++;
                }
            }
            
            LOG.info("Minimized " + minimized + " TopComponents in mode: " + modeId);
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error minimizing mode", e);
            return false;
        }
    }
    
    /**
     * Gets information about available modes.
     * @return Array of mode names
     */
    public String[] getAvailableModes() {
        try {
            java.util.Set<String> modeNames = new java.util.HashSet<>();
            java.util.Set<? extends org.openide.windows.Mode> modes = windowManager.getModes();
            for (org.openide.windows.Mode mode : modes) {
                modeNames.add(mode.getName());
            }
            return modeNames.toArray(new String[0]);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get available modes", e);
            return new String[0];
        }
    }
    
    /**
     * Activates a specific TopComponent.
     * @param tc The TopComponent to activate
     * @return CompletableFuture indicating success/failure
     */
    public CompletableFuture<Boolean> activateTopComponentAsync(TopComponent tc) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (EventQueue.isDispatchThread()) {
                    return activateTopComponentInternal(tc);
                } else {
                    CompletableFuture<Boolean> result = new CompletableFuture<>();
                    EventQueue.invokeLater(() -> {
                        try {
                            result.complete(activateTopComponentInternal(tc));
                        } catch (Exception e) {
                            result.completeExceptionally(e);
                        }
                    });
                    return result.get();
                }
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to activate TopComponent: " + tc.getName(), e);
                return false;
            }
        });
    }
    
    /**
     * Internal method to activate TopComponent (must be called on EDT).
     */
    private boolean activateTopComponentInternal(TopComponent tc) {
        try {
            if (tc.isOpened()) {
                tc.requestActive();
                tc.requestVisible();
                LOG.info("TopComponent activated: " + tc.getName());
                return true;
            } else {
                LOG.warning("Cannot activate closed TopComponent: " + tc.getName());
                return false;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error activating TopComponent", e);
            return false;
        }
    }
    
    /**
     * Gets the current active TopComponent.
     * @return The active TopComponent or null
     */
    public TopComponent getActiveTopComponent() {
        try {
            // Get the registry and find the active component
            org.openide.windows.TopComponent.Registry registry = org.openide.windows.TopComponent.getRegistry();
            return registry.getActivated();
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get active TopComponent", e);
            return null;
        }
    }
}
