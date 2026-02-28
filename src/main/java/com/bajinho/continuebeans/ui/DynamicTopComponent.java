package com.bajinho.continuebeans.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.concurrent.CompletableFuture;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;

/**
 * Dynamic TopComponent that can be configured and controlled programmatically.
 * Supports dynamic content loading, layout management, and window state control.
 * 
 * @author Continue Beans Team
 */
@TopComponent.Description(
    preferredID = "DynamicTopComponent",
    persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED
)
@TopComponent.Registration(
    mode = "output",
    openAtStartup = false
)
@ActionID(
    category = "Window",
    id = "com.bajinho.continuebeans.ui.DynamicTopComponent"
)
@ActionReference(
    path = "Menu/Window",
    position = 350
)
@Messages({
    "CTL_DynamicTopComponent=Dynamic Window",
    "HINT_DynamicTopComponent=Dynamic configurable window"
})
public class DynamicTopComponent extends TopComponent {
    
    private JPanel mainPanel;
    private JTextArea contentArea;
    private JLabel titleLabel;
    private JButton closeButton;
    private JButton refreshButton;
    private String windowId;
    private DynamicContentProvider contentProvider;
    
    /**
     * Creates a new DynamicTopComponent.
     */
    public DynamicTopComponent() {
        this("dynamic-" + System.currentTimeMillis(), null);
    }
    
    /**
     * Creates a new DynamicTopComponent with specific ID.
     * @param windowId Unique identifier for this window
     * @param contentProvider Provider for dynamic content
     */
    public DynamicTopComponent(String windowId, DynamicContentProvider contentProvider) {
        this.windowId = windowId;
        this.contentProvider = contentProvider;
        
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        
        setDisplayName("Dynamic Window");
        setToolTipText("Dynamic configurable window");
        
        // Load initial content
        refreshContent();
    }
    
    /**
     * Initializes all UI components.
     */
    private void initializeComponents() {
        mainPanel = new JPanel(new BorderLayout());
        
        titleLabel = new JLabel("Dynamic Window");
        titleLabel.setFont(titleLabel.getFont().deriveFont(14f));
        
        contentArea = new JTextArea();
        contentArea.setEditable(false);
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        
        closeButton = new JButton("Close");
        refreshButton = new JButton("Refresh");
        
        // Set preferred size
        setPreferredSize(new Dimension(400, 300));
    }
    
    /**
     * Layouts all components in the panel.
     */
    private void layoutComponents() {
        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Main layout
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);
        
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Sets up event handlers for buttons.
     */
    private void setupEventHandlers() {
        closeButton.addActionListener(e -> close());
        
        refreshButton.addActionListener(e -> refreshContent());
    }
    
    /**
     * Refreshes the content from the content provider.
     */
    public void refreshContent() {
        if (contentProvider != null) {
            contentProvider.loadContentAsync()
                .thenAccept(content -> {
                    SwingUtilities.invokeLater(() -> {
                        contentArea.setText(content);
                        contentArea.setCaretPosition(0);
                    });
                })
                .exceptionally(throwable -> {
                    SwingUtilities.invokeLater(() -> {
                        contentArea.setText("Error loading content: " + throwable.getMessage());
                    });
                    return null;
                });
        } else {
            contentArea.setText("No content provider configured.\n\nWindow ID: " + windowId);
        }
    }
    
    /**
     * Sets the title of this window.
     * @param title The new title
     */
    public void setWindowTitle(String title) {
        titleLabel.setText(title);
        setDisplayName(title);
    }
    
    /**
     * Sets the content provider for this window.
     * @param provider The new content provider
     */
    public void setContentProvider(DynamicContentProvider provider) {
        this.contentProvider = provider;
        refreshContent();
    }
    
    /**
     * Gets the window ID.
     * @return The window ID
     */
    public String getWindowId() {
        return windowId;
    }
    
    /**
     * Appends text to the content area.
     * @param text The text to append
     */
    public void appendContent(String text) {
        SwingUtilities.invokeLater(() -> {
            contentArea.append(text);
            contentArea.setCaretPosition(contentArea.getDocument().getLength());
        });
    }
    
    /**
     * Clears the content area.
     */
    public void clearContent() {
        SwingUtilities.invokeLater(() -> {
            contentArea.setText("");
        });
    }
    
    /**
     * Sets the content text directly.
     * @param content The new content
     */
    public void setContent(String content) {
        SwingUtilities.invokeLater(() -> {
            contentArea.setText(content);
            contentArea.setCaretPosition(0);
        });
    }
    
    /**
     * Gets the current content text.
     * @return The current content
     */
    public String getContent() {
        return contentArea.getText();
    }
    
    /**
     * Shows or hides the control buttons.
     * @param visible Whether to show the buttons
     */
    public void setControlButtonsVisible(boolean visible) {
        SwingUtilities.invokeLater(() -> {
            closeButton.setVisible(visible);
            refreshButton.setVisible(visible);
        });
    }
    
    /**
     * Sets the window size.
     * @param width The width
     * @param height The height
     */
    public void setWindowSize(int width, int height) {
        SwingUtilities.invokeLater(() -> {
            setPreferredSize(new Dimension(width, height));
            revalidate();
        });
    }
    
    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }
    
    @Override
    protected String preferredID() {
        return "DynamicTopComponent_" + windowId;
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
        // Refresh content when window opens
        refreshContent();
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
        // Cleanup if needed
    }
    
    /**
     * Interface for providing dynamic content to the window.
     */
    @FunctionalInterface
    public interface DynamicContentProvider {
        
        /**
         * Loads content asynchronously.
         * @return CompletableFuture containing the content
         */
        CompletableFuture<String> loadContentAsync();
    }
    
    /**
     * Creates a simple content provider from static text.
     * @param content The static content
     * @return A content provider that returns the static content
     */
    public static DynamicContentProvider staticContentProvider(String content) {
        return () -> CompletableFuture.completedFuture(content);
    }
    
    /**
     * Creates a content provider that loads from a URL.
     * @param url The URL to load from
     * @return A content provider that loads from the URL
     */
    public static DynamicContentProvider urlContentProvider(String url) {
        return () -> CompletableFuture.supplyAsync(() -> {
            try {
                java.net.URLConnection connection = new java.net.URL(url).openConnection();
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(connection.getInputStream()))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                    return content.toString();
                }
            } catch (Exception e) {
                return "Error loading from URL: " + e.getMessage();
            }
        });
    }
    
    /**
     * Creates a content provider that executes a command and returns the output.
     * @param command The command to execute
     * @return A content provider that executes the command
     */
    public static DynamicContentProvider commandContentProvider(String... command) {
        return () -> CompletableFuture.supplyAsync(() -> {
            try {
                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                
                StringBuilder output = new StringBuilder();
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                }
                
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    output.append("\nCommand exited with code: ").append(exitCode);
                }
                
                return output.toString();
            } catch (Exception e) {
                return "Error executing command: " + e.getMessage();
            }
        });
    }
}
