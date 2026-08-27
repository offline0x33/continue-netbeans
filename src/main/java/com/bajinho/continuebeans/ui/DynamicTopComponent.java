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

    public DynamicTopComponent() {
        this("dynamic-" + System.currentTimeMillis(), null);
    }

    public DynamicTopComponent(String windowId, DynamicContentProvider contentProvider) {
        this.windowId = windowId;
        this.contentProvider = contentProvider;
        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        setDisplayName("Dynamic Window");
        setToolTipText("Dynamic configurable window");
        refreshContent();
    }

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
        setPreferredSize(new Dimension(400, 300));
    }

    private void layoutComponents() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(mainPanel, BorderLayout.CENTER);
    }

    private void setupEventHandlers() {
        closeButton.addActionListener(e -> close());
        refreshButton.addActionListener(e -> refreshContent());
    }

    /**
     * Refreshes content. Providers remain asynchronous when invoked from the EDT;
     * when invoked from another thread the provider is completed before returning,
     * preventing callers from observing stale content during synchronous UI tests.
     */
    public void refreshContent() {
        if (contentProvider == null) {
            setContentOnEdt("No content provider configured.\n\nWindow ID: " + windowId);
            return;
        }

        CompletableFuture<String> future = contentProvider.loadContentAsync();
        if (SwingUtilities.isEventDispatchThread()) {
            future.thenAccept(this::setContentOnEdt)
                    .exceptionally(throwable -> {
                        setContentOnEdt("Error loading content: " + rootMessage(throwable));
                        return null;
                    });
            return;
        }

        try {
            setContentOnEdt(future.join());
        } catch (java.util.concurrent.CompletionException e) {
            setContentOnEdt("Error loading content: " + rootMessage(e));
        }
    }

    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage() != null ? current.getMessage() : current.toString();
    }

    private void setContentOnEdt(String content) {
        Runnable update = () -> {
            contentArea.setText(content);
            contentArea.setCaretPosition(0);
        };
        if (SwingUtilities.isEventDispatchThread()) {
            update.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(update);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new IllegalStateException("Failed to update dynamic content", e.getCause());
        }
    }

    public void setWindowTitle(String title) {
        titleLabel.setText(title);
        setDisplayName(title);
    }

    public void setContentProvider(DynamicContentProvider provider) {
        this.contentProvider = provider;
        refreshContent();
    }

    public String getWindowId() {
        return windowId;
    }

    public void appendContent(String text) {
        SwingUtilities.invokeLater(() -> {
            contentArea.append(text);
            contentArea.setCaretPosition(contentArea.getDocument().getLength());
        });
    }

    public void clearContent() {
        SwingUtilities.invokeLater(() -> contentArea.setText(""));
    }

    public void setContent(String content) {
        SwingUtilities.invokeLater(() -> {
            contentArea.setText(content);
            contentArea.setCaretPosition(0);
        });
    }

    public String getContent() {
        return contentArea.getText();
    }

    public void setControlButtonsVisible(boolean visible) {
        SwingUtilities.invokeLater(() -> {
            closeButton.setVisible(visible);
            refreshButton.setVisible(visible);
        });
    }

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
        refreshContent();
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
    }

    @FunctionalInterface
    public interface DynamicContentProvider {
        CompletableFuture<String> loadContentAsync();
    }

    public static DynamicContentProvider staticContentProvider(String content) {
        return () -> CompletableFuture.completedFuture(content);
    }

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
