package com.bajinho.continuebeans.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * Status bar integration for Continue Beans with dynamic status updates,
 * progress indicators, and contextual information display.
 * 
 * @author Continue Beans Team
 */
@ServiceProvider(service = StatusLineElementProvider.class)
public class StatusIntegration implements StatusLineElementProvider {
    
    private static final Logger LOG = Logger.getLogger(StatusIntegration.class.getName());
    
    private JPanel statusPanel;
    private JLabel statusLabel;
    private JLabel timeLabel;
    private JProgressBar progressBar;
    private JPanel progressPanel;
    private Timer clockTimer;
    private List<StatusItem> statusItems;
    private final Map<String, ProgressTask> progressTasks;
    
    private String currentStatus;
    private Color statusColor;
    private boolean showProgress;
    private boolean showClock;
    
    /**
     * Represents a status item with metadata.
     */
    public static class StatusItem {
        private final String id;
        private final String text;
        private final Color color;
        private final Icon icon;
        private final int priority;
        private final long timestamp;
        private final boolean persistent;
        
        public StatusItem(String id, String text, Color color, Icon icon, 
                         int priority, boolean persistent) {
            this.id = id;
            this.text = text;
            this.color = color;
            this.icon = icon;
            this.priority = priority;
            this.persistent = persistent;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters
        public String getId() { return id; }
        public String getText() { return text; }
        public Color getColor() { return color; }
        public Icon getIcon() { return icon; }
        public int getPriority() { return priority; }
        public long getTimestamp() { return timestamp; }
        public boolean isPersistent() { return persistent; }
    }
    
    /**
     * Represents a progress task.
     */
    public static class ProgressTask {
        private final String id;
        private final String name;
        private final int maximum;
        private int current;
        private final boolean indeterminate;
        private final String status;
        private final long startTime;
        private boolean completed;
        
        public ProgressTask(String id, String name, int maximum, boolean indeterminate, String status) {
            this.id = id;
            this.name = name;
            this.maximum = maximum;
            this.current = 0;
            this.indeterminate = indeterminate;
            this.status = status;
            this.startTime = System.currentTimeMillis();
            this.completed = false;
        }
        
        // Getters and setters
        public String getId() { return id; }
        public String getName() { return name; }
        public int getMaximum() { return maximum; }
        public int getCurrent() { return current; }
        public boolean isIndeterminate() { return indeterminate; }
        public String getStatus() { return status; }
        public long getStartTime() { return startTime; }
        public boolean isCompleted() { return completed; }
        
        public void setCurrent(int current) {
            this.current = Math.max(0, Math.min(maximum, current));
            if (this.current >= maximum) {
                this.completed = true;
            }
        }
        
        public void setCompleted(boolean completed) {
            this.completed = completed;
            if (completed) {
                this.current = maximum;
            }
        }
        
        public double getProgress() {
            return maximum > 0 ? (double) current / maximum : 0.0;
        }
    }
    
    /**
     * Creates a new StatusIntegration.
     */
    public StatusIntegration() {
        this.statusItems = new ArrayList<>();
        this.progressTasks = new ConcurrentHashMap<>();
        this.currentStatus = "Ready";
        this.statusColor = Color.BLACK;
        this.showProgress = true;
        this.showClock = true;
        
        initializeComponents();
        layoutComponents();
        setupClockTimer();
        
        LOG.info("StatusIntegration initialized");
    }
    
    /**
     * Initializes UI components.
     */
    private void initializeComponents() {
        statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(1, 5, 1, 5));
        
        statusLabel = new JLabel(currentStatus);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11));
        statusLabel.setForeground(statusColor);
        
        timeLabel = new JLabel();
        timeLabel.setFont(timeLabel.getFont().deriveFont(Font.PLAIN, 11));
        timeLabel.setForeground(Color.GRAY);
        
        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(100, 10));
        progressBar.setMaximumSize(new Dimension(200, 10));
        progressBar.setStringPainted(false);
        progressBar.setBorder(BorderFactory.createEmptyBorder());
        
        progressPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        progressPanel.add(progressBar);
        
        // Theme integration
        updateThemeColors();
    }
    
    /**
     * Layouts components.
     */
    private void layoutComponents() {
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.add(statusLabel);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        if (showClock) {
            rightPanel.add(timeLabel);
        }
        
        statusPanel.add(leftPanel, BorderLayout.WEST);
        statusPanel.add(progressPanel, BorderLayout.CENTER);
        statusPanel.add(rightPanel, BorderLayout.EAST);
    }
    
    /**
     * Sets up the clock timer.
     */
    private void setupClockTimer() {
        clockTimer = new Timer(1000, e -> updateClock());
        clockTimer.start();
    }
    
    /**
     * Updates the clock display.
     */
    private void updateClock() {
        SwingUtilities.invokeLater(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            timeLabel.setText(sdf.format(new Date()));
        });
    }
    
    /**
     * Updates theme colors from ThemeManager.
     */
    private void updateThemeColors() {
        try {
            ThemeManager themeManager = ThemeManager.getInstance();
            Color foreground = themeManager.getChatForeground();
            Color background = themeManager.getChatBackground();
            
            statusLabel.setForeground(foreground);
            timeLabel.setForeground(foreground.darker());
            statusPanel.setBackground(background);
            
            // Update progress bar colors
            progressBar.setForeground(themeManager.getColor("accent"));
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to update theme colors", e);
        }
    }
    
    @Override
    public Component getStatusLineElement() {
        return statusPanel;
    }
    
    /**
     * Sets the current status message.
     * @param status The status message
     */
    public void setStatus(String status) {
        setStatus(status, null, null, 0, false);
    }
    
    /**
     * Sets the current status with color.
     * @param status The status message
     * @param color The status color
     */
    public void setStatus(String status, Color color) {
        setStatus(status, color, null, 0, false);
    }
    
    /**
     * Sets the current status with full options.
     * @param status The status message
     * @param color The status color
     * @param icon The status icon
     * @param priority The priority
     * @param persistent Whether to persist the status
     */
    public void setStatus(String status, Color color, Icon icon, int priority, boolean persistent) {
        SwingUtilities.invokeLater(() -> {
            this.currentStatus = status;
            this.statusColor = color != null ? color : Color.BLACK;
            
            statusLabel.setText(status);
            statusLabel.setForeground(this.statusColor);
            
            if (icon != null) {
                statusLabel.setIcon(icon);
            }
            
            // Add to status items if persistent
            if (persistent) {
                StatusItem item = new StatusItem("current", status, this.statusColor, icon, priority, persistent);
                addStatusItem(item);
            }
            
            LOG.info("Status updated: " + status);
        });
    }
    
    /**
     * Adds a status item.
     * @param item The status item to add
     */
    public void addStatusItem(StatusItem item) {
        statusItems.add(item);
        statusItems.sort((i1, i2) -> Integer.compare(i2.getPriority(), i1.getPriority()));
        
        // Remove non-persistent items older than 30 seconds
        long cutoff = System.currentTimeMillis() - 30000;
        statusItems.removeIf(i -> !i.isPersistent() && i.getTimestamp() < cutoff);
        
        // Keep only top 10 items
        if (statusItems.size() > 10) {
            statusItems = new ArrayList<>(statusItems.subList(0, 10));
        }
    }
    
    /**
     * Shows an informational status.
     * @param message The message
     */
    public void showInfo(String message) {
        setStatus(message, Color.BLUE, null, 50, false);
    }
    
    /**
     * Shows a warning status.
     * @param message The message
     */
    public void showWarning(String message) {
        setStatus(message, Color.ORANGE, null, 75, false);
    }
    
    /**
     * Shows an error status.
     * @param message The message
     */
    public void showError(String message) {
        setStatus(message, Color.RED, null, 100, false);
    }
    
    /**
     * Shows a success status.
     * @param message The message
     */
    public void showSuccess(String message) {
        setStatus(message, new Color(0, 128, 0), null, 60, false);
    }
    
    /**
     * Starts a progress task.
     * @param taskId The task ID
     * @param name The task name
     * @param maximum The maximum progress value
     * @param indeterminate Whether the progress is indeterminate
     * @param status The task status
     * @return The created progress task
     */
    public ProgressTask startProgress(String taskId, String name, int maximum, 
                                   boolean indeterminate, String status) {
        ProgressTask task = new ProgressTask(taskId, name, maximum, indeterminate, status);
        progressTasks.put(taskId, task);
        
        SwingUtilities.invokeLater(() -> {
            updateProgressBar();
            if (showProgress) {
                progressPanel.setVisible(true);
            }
        });
        
        LOG.info("Progress task started: " + name);
        return task;
    }
    
    /**
     * Updates a progress task.
     * @param taskId The task ID
     * @param current The current progress value
     */
    public void updateProgress(String taskId, int current) {
        ProgressTask task = progressTasks.get(taskId);
        if (task != null) {
            task.setCurrent(current);
            SwingUtilities.invokeLater(() -> updateProgressBar());
        }
    }
    
    /**
     * Updates a progress task with status.
     * @param taskId The task ID
     * @param current The current progress value
     * @param status The new status
     */
    public void updateProgress(String taskId, int current, String status) {
        ProgressTask task = progressTasks.get(taskId);
        if (task != null) {
            task.setCurrent(current);
            SwingUtilities.invokeLater(() -> {
                updateProgressBar();
                setStatus(status);
            });
        }
    }
    
    /**
     * Completes a progress task.
     * @param taskId The task ID
     */
    public void completeProgress(String taskId) {
        ProgressTask task = progressTasks.get(taskId);
        if (task != null) {
            task.setCompleted(true);
            SwingUtilities.invokeLater(() -> {
                updateProgressBar();
                showSuccess(task.getName() + " completed");
                
                // Remove completed task after delay
                CompletableFuture.delayedExecutor(2, java.util.concurrent.TimeUnit.SECONDS)
                    .execute(() -> {
                        progressTasks.remove(taskId);
                        SwingUtilities.invokeLater(() -> updateProgressBar());
                    });
            });
        }
    }
    
    /**
     * Updates the progress bar display.
     */
    private void updateProgressBar() {
        if (progressTasks.isEmpty()) {
            progressPanel.setVisible(false);
            return;
        }
        
        // Show the highest priority active task
        ProgressTask activeTask = null;
        for (ProgressTask task : progressTasks.values()) {
            if (!task.isCompleted()) {
                activeTask = task;
                break;
            }
        }
        
        if (activeTask != null) {
            progressBar.setVisible(true);
            progressBar.setIndeterminate(activeTask.isIndeterminate());
            
            if (!activeTask.isIndeterminate()) {
                progressBar.setMaximum(activeTask.getMaximum());
                progressBar.setValue(activeTask.getCurrent());
            }
            
            progressBar.setToolTipText(activeTask.getName() + ": " + activeTask.getStatus());
            progressPanel.setVisible(showProgress);
        } else {
            progressPanel.setVisible(false);
        }
    }
    
    /**
     * Shows or hides the progress bar.
     * @param show Whether to show progress
     */
    public void setShowProgress(boolean show) {
        this.showProgress = show;
        SwingUtilities.invokeLater(() -> updateProgressBar());
    }
    
    /**
     * Shows or hides the clock.
     * @param show Whether to show clock
     */
    public void setShowClock(boolean show) {
        this.showClock = show;
        SwingUtilities.invokeLater(() -> {
            timeLabel.setVisible(show);
            statusPanel.revalidate();
        });
    }
    
    /**
     * Gets the current status.
     * @return The current status
     */
    public String getCurrentStatus() {
        return currentStatus;
    }
    
    /**
     * Gets all status items.
     * @return Copy of status items
     */
    public List<StatusItem> getStatusItems() {
        return new ArrayList<>(statusItems);
    }
    
    /**
     * Gets all progress tasks.
     * @return Copy of progress tasks
     */
    public Map<String, ProgressTask> getProgressTasks() {
        return new ConcurrentHashMap<>(progressTasks);
    }
    
    /**
     * Clears all status items.
     */
    public void clearStatusItems() {
        statusItems.clear();
    }
    
    /**
     * Clears completed progress tasks.
     */
    public void clearCompletedTasks() {
        progressTasks.entrySet().removeIf(entry -> entry.getValue().isCompleted());
        SwingUtilities.invokeLater(() -> updateProgressBar());
    }
    
    /**
     * Refreshes theme colors.
     */
    public void refreshTheme() {
        SwingUtilities.invokeLater(() -> updateThemeColors());
    }
    
    /**
     * Shows temporary status for a specific duration.
     * @param message The message
     * @param color The color
     * @param duration The duration in milliseconds
     */
    public void showTemporaryStatus(String message, Color color, int duration) {
        setStatus(message, color);
        
        CompletableFuture.delayedExecutor(duration / 1000, java.util.concurrent.TimeUnit.SECONDS)
            .execute(() -> {
                SwingUtilities.invokeLater(() -> setStatus("Ready"));
            });
    }
    
    /**
     * Shows status with automatic color based on message type.
     * @param message The message
     * @param type The message type (info, warning, error, success)
     */
    public void showStatus(String message, String type) {
        switch (type.toLowerCase()) {
            case "info":
                showInfo(message);
                break;
            case "warning":
                showWarning(message);
                break;
            case "error":
                showError(message);
                break;
            case "success":
                showSuccess(message);
                break;
            default:
                setStatus(message);
                break;
        }
    }
}
