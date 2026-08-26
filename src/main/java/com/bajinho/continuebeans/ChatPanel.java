package com.bajinho.continuebeans;

import com.bajinho.continuebeans.task.AgentTask;
import com.bajinho.continuebeans.task.TaskOrchestrator;
import com.bajinho.continuebeans.task.TaskPlan;
import com.bajinho.continuebeans.task.TaskStatus;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Canonical dark task-driven chat UI. */
public class ChatPanel extends JPanel {

    private static final Color BG = new Color(0x12, 0x12, 0x14);
    private static final Color PANEL = new Color(0x1A, 0x1A, 0x1E);
    private static final Color BORDER = new Color(0x27, 0x27, 0x2A);
    private static final Color PRIMARY = new Color(0xE4, 0xE4, 0xE7);
    private static final Color SECONDARY = new Color(0xA1, 0xA1, 0xAA);
    private static final Color MUTED = new Color(0x71, 0x71, 0x7A);
    private static final Color GREEN = new Color(0x4A, 0xDE, 0x80);
    private static final Color RED = new Color(0xF8, 0x71, 0x71);
    private static final Color BLUE = new Color(0x60, 0xA5, 0xFA);
    private static final Color ORANGE = new Color(0xF9, 0x73, 0x16);
    private static final Color WARNING_BG = new Color(0x2D, 0x1C, 0x11);
    private static final Color WARNING_BORDER = new Color(0x52, 0x2E, 0x15);
    private static final Color SEND_BG = new Color(0x3F, 0x3F, 0x46);
    private static final Color STOP_BG = new Color(0x7F, 0x1D, 0x1D);
    private static final Color HOVER = new Color(0x27, 0x27, 0x2A);
    private static final Color FOCUS = new Color(0x3B, 0x82, 0xF6);
    private static final Font UI_FONT = new Font("Inter", Font.PLAIN, 13);
    private static final Font UI_FONT_MEDIUM = new Font("Inter", Font.BOLD, 13);
    private static final Font SMALL_FONT = new Font("Inter", Font.PLAIN, 12);
    private static final Font CODE_FONT = new Font("JetBrains Mono", Font.PLAIN, 12);
    private static final Pattern POSITIVE_DIFF = Pattern.compile("(?:^|\\s)\\+(\\d+)(?=\\s|$)");
    private static final Pattern NEGATIVE_DIFF = Pattern.compile("(?:^|\\s)-(\\d+)(?=\\s|$)");
    private static final Pattern FILE_PATTERN = Pattern.compile("(?:[A-Za-z0-9_./-]+\\.(?:java|xml|json|yaml|yml|md|properties|feature|css|js|ts))");

    private final LlmClient llmClient;
    private final TaskOrchestrator taskOrchestrator;
    private final JPanel conversationPanel;
    private final JPanel taskPanelHost;
    private final JLabel statusLabel;
    private final JLabel taskProgressLabel;
    private JTextField promptInput;
    private JButton sendButton;
    private JButton stopButton;
    private JComboBox<String> modelSelector;
    private JComboBox<AgentMode> agentModeSelector;
    private boolean isProcessing;
    private final AtomicBoolean cancelRequested = new AtomicBoolean(false);
    private volatile CompletableFuture<?> activeRequest;
    private TaskPlan activePlan;
    private String lastResult = "";
    private String lastTitle = "";

    public ChatPanel() {
        llmClient = new LlmClient();
        taskOrchestrator = new TaskOrchestrator();
        isProcessing = false;

        setLayout(new BorderLayout());
        setBackground(BG);
        setBorder(BorderFactory.createEmptyBorder());
        add(createTopBar(), BorderLayout.NORTH);

        conversationPanel = new JPanel();
        conversationPanel.setLayout(new BoxLayout(conversationPanel, BoxLayout.Y_AXIS));
        conversationPanel.setBackground(BG);
        conversationPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JScrollPane scroll = new JScrollPane(conversationPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);

        taskPanelHost = new JPanel(new BorderLayout());
        taskPanelHost.setOpaque(false);
        taskProgressLabel = new JLabel("0 / 0 tasks done");
        taskProgressLabel.setForeground(PRIMARY);
        taskProgressLabel.setFont(UI_FONT_MEDIUM);
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(SECONDARY);
        statusLabel.setFont(UI_FONT);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(BG);
        bottom.add(createInputBox(), BorderLayout.CENTER);
        bottom.add(createFooter(), BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        conversationPanel.add(createThoughtLine("Ready. Describe what you want changed."));
        conversationPanel.add(Box.createVerticalStrut(8));
        if (hasConfiguredApiUrl()) {
            refreshModels();
        }
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(0x09, 0x09, 0x0B));
        bar.setPreferredSize(new Dimension(10, 40));

        JPanel tab = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 7));
        tab.setBackground(BG);
        JLabel icon = new JLabel("●");
        icon.setForeground(BLUE);
        icon.setFont(UI_FONT_MEDIUM);
        JLabel title = new JLabel("NetBeans Plugin Integration");
        title.setForeground(PRIMARY);
        title.setFont(UI_FONT_MEDIUM);
        tab.add(icon);
        tab.add(title);
        tab.add(iconButton("×"));
        bar.add(tab, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        actions.setOpaque(false);
        actions.add(iconButton("▥"));
        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    private JPanel createInputBox() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(8, 16, 4, 16));

        JPanel box = roundedPanel(PANEL, BORDER, 12);
        box.setLayout(new BorderLayout(8, 8));
        box.setBorder(BorderFactory.createCompoundBorder(
                roundedBorder(BORDER, 12),
                BorderFactory.createEmptyBorder(10, 12, 8, 12)));

        JLabel hint = new JLabel("Tip: Type @ conversation to bring in context from another chat");
        hint.setForeground(MUTED);
        hint.setFont(UI_FONT);

        promptInput = new JTextField();
        promptInput.setOpaque(false);
        promptInput.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));
        promptInput.setForeground(PRIMARY);
        promptInput.setCaretColor(PRIMARY);
        promptInput.setFont(UI_FONT);
        promptInput.addActionListener(event -> sendPrompt());
        promptInput.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                box.setBorder(BorderFactory.createCompoundBorder(roundedBorder(FOCUS, 12), BorderFactory.createEmptyBorder(10, 12, 8, 12)));
                box.repaint();
            }
            @Override public void focusLost(FocusEvent e) {
                box.setBorder(BorderFactory.createCompoundBorder(roundedBorder(BORDER, 12), BorderFactory.createEmptyBorder(10, 12, 8, 12)));
                box.repaint();
            }
        });
        promptInput.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateSendAvailability(); }
            public void removeUpdate(DocumentEvent e) { updateSendAvailability(); }
            public void changedUpdate(DocumentEvent e) { updateSendAvailability(); }
        });

        JPanel controls = new JPanel(new BorderLayout());
        controls.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        left.add(smallControl("+"));

        agentModeSelector = new JComboBox<>(AgentMode.values());
        agentModeSelector.setFont(UI_FONT);
        agentModeSelector.setForeground(PRIMARY);
        agentModeSelector.setBackground(PANEL);
        agentModeSelector.setSelectedItem(ContinueSettings.getAgentMode());
        agentModeSelector.setToolTipText("Agent mode: Code, Planning, Docs or Agent");
        agentModeSelector.addActionListener(event -> persistSelectedAgentMode());
        left.add(agentModeSelector);

        modelSelector = new JComboBox<>();
        modelSelector.setFont(UI_FONT);
        modelSelector.setForeground(PRIMARY);
        modelSelector.setBackground(PANEL);
        modelSelector.setEditable(false);
        modelSelector.setToolTipText("Model");
        modelSelector.addActionListener(event -> persistSelectedModel());
        String configuredModel = ContinueSettings.getModel();
        if (configuredModel != null && !configuredModel.isBlank()) {
            modelSelector.addItem(configuredModel);
            modelSelector.setSelectedItem(configuredModel);
        } else {
            modelSelector.addItem("Loading models…");
        }
        left.add(modelSelector);

        JButton refresh = smallControl("↻");
        refresh.setToolTipText("Refresh models");
        refresh.addActionListener(event -> refreshModels());
        left.add(refresh);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(statusLabel);

        stopButton = roundButton("■");
        stopButton.setBackground(STOP_BG);
        stopButton.setToolTipText("Stop generation");
        stopButton.setVisible(false);
        stopButton.addActionListener(event -> stopGeneration());
        right.add(stopButton);

        sendButton = roundButton("↑");
        sendButton.setToolTipText("Send");
        sendButton.addActionListener(event -> sendPrompt());
        right.add(sendButton);

        controls.add(left, BorderLayout.WEST);
        controls.add(right, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(2, 4));
        center.setOpaque(false);
        center.add(hint, BorderLayout.NORTH);
        center.add(promptInput, BorderLayout.CENTER);
        center.add(controls, BorderLayout.SOUTH);
        box.add(center, BorderLayout.CENTER);
        wrapper.add(box, BorderLayout.CENTER);
        updateSendAvailability();
        return wrapper;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 16, 6, 16));
        JLabel left = new JLabel("▰ Local   |   ▰ continue-netbeans");
        left.setForeground(SECONDARY);
        left.setFont(SMALL_FONT);
        JLabel right = new JLabel("Ready");
        right.setForeground(SECONDARY);
        right.setFont(SMALL_FONT);
        footer.add(left, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    private boolean hasConfiguredApiUrl() {
        String apiUrl = ContinueSettings.getApiUrl();
        if (apiUrl == null || apiUrl.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(apiUrl.trim());
            return uri.getScheme() != null && !uri.getScheme().isBlank();
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private void refreshModels() {
        if (!hasConfiguredApiUrl()) {
            updateStatus("Configure AI API", ORANGE);
            return;
        }

        updateStatus("Loading models…", SECONDARY);
        CompletableFuture<List<String>> future = llmClient.getModelosDisponiveisAsync();
        future.thenAccept(models -> SwingUtilities.invokeLater(() -> {
            String selected = ContinueSettings.getModel();
            modelSelector.removeAllItems();
            if (models != null) {
                for (String model : models) {
                    if (model != null && !model.isBlank()) {
                        modelSelector.addItem(model);
                    }
                }
            }
            if (modelSelector.getItemCount() == 0) {
                if (selected != null && !selected.isBlank()) {
                    modelSelector.addItem(selected);
                } else {
                    modelSelector.addItem("No models available");
                }
            }
            if (selected != null && !selected.isBlank() && containsItem(selected)) {
                modelSelector.setSelectedItem(selected);
            } else if (modelSelector.getItemCount() > 0) {
                Object first = modelSelector.getItemAt(0);
                if (first instanceof String && !((String) first).equals("No models available")) {
                    ContinueSettings.setModel((String) first);
                    modelSelector.setSelectedItem(first);
                }
            }
            updateStatus("Models ready", SECONDARY);
        })).exceptionally(error -> {
            SwingUtilities.invokeLater(() -> {
                String selected = ContinueSettings.getModel();
                if ((modelSelector.getItemCount() == 0 || "Loading models…".equals(modelSelector.getItemAt(0)))
                        && selected != null && !selected.isBlank()) {
                    modelSelector.removeAllItems();
                    modelSelector.addItem(selected);
                    modelSelector.setSelectedItem(selected);
                }
                updateStatus("Model discovery failed", ORANGE);
            });
            return null;
        });
    }

    private boolean containsItem(String value) {
        for (int index = 0; index < modelSelector.getItemCount(); index++) {
            if (value.equals(modelSelector.getItemAt(index))) {
                return true;
            }
        }
        return false;
    }

    private void persistSelectedModel() {
        if (modelSelector == null || modelSelector.getSelectedItem() == null) {
            return;
        }
        String selected = String.valueOf(modelSelector.getSelectedItem());
        if (!selected.isBlank() && !selected.endsWith("…") && !selected.equals("No models available")) {
            ContinueSettings.setModel(selected);
            updateStatus("Model: " + selected, SECONDARY);
        }
    }

    private void persistSelectedAgentMode() {
        if (agentModeSelector == null || agentModeSelector.getSelectedItem() == null) {
            return;
        }
        AgentMode mode = (AgentMode) agentModeSelector.getSelectedItem();
        ContinueSettings.setAgentMode(mode);
        updateStatus("Mode: " + mode.getLabel(), SECONDARY);
    }

    private String getSelectedModel() {
        Object selected = modelSelector == null ? null : modelSelector.getSelectedItem();
        if (selected == null) {
            return ContinueSettings.getModel();
        }
        String model = String.valueOf(selected).trim();
        return model.isBlank() || model.endsWith("…") || model.equals("No models available")
                ? ContinueSettings.getModel() : model;
    }

    private AgentMode getSelectedAgentMode() {
        Object selected = agentModeSelector == null ? null : agentModeSelector.getSelectedItem();
        if (selected instanceof AgentMode) {
            return (AgentMode) selected;
        }
        return ContinueSettings.getAgentMode();
    }

    private void updateSendAvailability() {
        if (sendButton != null) {
            sendButton.setEnabled(!isProcessing && promptInput != null && !promptInput.getText().trim().isEmpty());
        }
        if (stopButton != null) {
            stopButton.setVisible(isProcessing);
            stopButton.setEnabled(isProcessing);
        }
    }

    private void sendPrompt() {
        String prompt = promptInput.getText().trim();
        if (prompt.isEmpty() || isProcessing) {
            return;
        }
        String selectedModel = getSelectedModel();
        if (selectedModel == null || selectedModel.isBlank()) {
            updateStatus("Select a model first", ORANGE);
            return;
        }
        AgentMode agentMode = getSelectedAgentMode();
        ContinueSettings.setModel(selectedModel);
        ContinueSettings.setAgentMode(agentMode);

        cancelRequested.set(false);
        isProcessing = true;
        sendButton.setEnabled(false);
        stopButton.setVisible(true);
        stopButton.setEnabled(true);
        promptInput.setEnabled(false);
        modelSelector.setEnabled(false);
        agentModeSelector.setEnabled(false);

        appendMessage("You", prompt, PRIMARY);
        appendThought("Mode: " + agentMode.getLabel(), "Planning task graph...");

        CompletableFuture<TaskPlan> request = taskOrchestrator.executeGoal(prompt, "lmstudio",
                new TaskOrchestrator.Listener() {
            @Override public void onPlanCreated(TaskPlan plan) {
                if (cancelRequested.get()) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    activePlan = plan;
                    rebuildTaskPanel();
                    appendTaskPanel();
                    updateStatus("Planning complete", BLUE);
                    appendActivity("Planner", "Task graph created", BLUE);
                });
            }
            @Override public void onTaskStarted(AgentTask task) {
                if (cancelRequested.get()) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    updateStatus("Running: " + task.getTitle(), ORANGE);
                    appendThought("Working on: " + task.getTitle(), "Attempt " + task.getAttempts());
                    appendActivity("Tool", task.getTitle(), ORANGE);
                    rebuildTaskPanel();
                });
            }
            @Override public void onTaskVerifying(AgentTask task) {
                if (cancelRequested.get()) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    updateStatus("Verifying: " + task.getTitle(), ORANGE);
                    appendThought("Verifying", task.getTitle());
                    appendActivity("Verify", task.getTitle(), ORANGE);
                    rebuildTaskPanel();
                });
            }
            @Override public void onTaskCompleted(AgentTask task) {
                if (cancelRequested.get()) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    rebuildTaskPanel();
                    if (task.getLastResult() != null && !task.getLastResult().isBlank()) {
                        lastResult = task.getLastResult();
                        lastTitle = task.getTitle();
                        appendCodeResult(task.getTitle(), task.getLastResult());
                        appendActivity("File", extractFileReference(task.getTitle(), task.getLastResult()), GREEN);
                    }
                });
            }
            @Override public void onTaskFailed(AgentTask task) {
                if (cancelRequested.get()) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    rebuildTaskPanel();
                    appendThought("Task failed", task.getLastError());
                    appendActivity("Error", task.getLastError(), RED);
                    updateStatus("Task failed", RED);
                });
            }
            @Override public void onReplanning(TaskPlan failedPlan) {
                if (cancelRequested.get()) {
                    return;
                }
                SwingUtilities.invokeLater(() -> {
                    activePlan = failedPlan;
                    rebuildTaskPanel();
                    appendThought("Replanning", "Previous failure preserved as context");
                    appendActivity("Planner", "Rebuilding task graph", ORANGE);
                    updateStatus("Replanning", ORANGE);
                });
            }
            @Override public void onCompleted(TaskPlan plan) {
                SwingUtilities.invokeLater(() -> {
                    if (!cancelRequested.get()) {
                        activePlan = plan;
                        rebuildTaskPanel();
                        appendThought("Objective completed", "All tasks verified");
                        appendActions();
                        updateStatus("Completed", GREEN);
                    }
                    resetInputState();
                });
            }
            @Override public void onFailed(String message, TaskPlan plan) {
                SwingUtilities.invokeLater(() -> {
                    if (!cancelRequested.get()) {
                        activePlan = plan;
                        rebuildTaskPanel();
                        appendWarning(message);
                        appendActivity("Error", message, RED);
                        updateStatus("Failed", RED);
                    }
                    resetInputState();
                });
            }
        });

        activeRequest = request;
        request.whenComplete((plan, error) -> SwingUtilities.invokeLater(() -> {
            if (cancelRequested.get()) {
                appendThought("Stopped", "Generation cancelled by user");
                updateStatus("Stopped", ORANGE);
                resetInputState();
            }
        }));

        promptInput.setText("");
        updateSendAvailability();
    }

    private void stopGeneration() {
        if (!isProcessing) {
            return;
        }
        cancelRequested.set(true);
        CompletableFuture<?> request = activeRequest;
        if (request != null) {
            request.cancel(true);
        }
        appendThought("Stopping…", "Cancelling active request");
        updateStatus("Stopping…", ORANGE);
        resetInputState();
    }

    private void appendMessage(String author, String message, Color color) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel label = new JLabel("<html><b>" + escape(author) + ":</b> " + escape(message) + "</html>");
        label.setForeground(color);
        label.setFont(UI_FONT);
        row.add(label, BorderLayout.CENTER);
        conversationPanel.add(row);
        conversationPanel.add(Box.createVerticalStrut(8));
        refreshConversation();
    }

    private void appendThought(String title, String detail) {
        conversationPanel.add(createThoughtLine(title + "   " + detail));
        conversationPanel.add(Box.createVerticalStrut(4));
        refreshConversation();
    }

    private void appendActivity(String kind, String text, Color color) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        JLabel icon = new JLabel(activityIcon(kind));
        icon.setForeground(color);
        icon.setFont(UI_FONT_MEDIUM);
        JLabel value = new JLabel("<html><span style='color:#A1A1AA'>" + escape(kind)
                + "</span> &nbsp; <span style='color:#60A5FA'>" + escape(text) + "</span></html>");
        value.setFont(SMALL_FONT);
        value.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        value.setToolTipText(text);
        row.add(icon, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        conversationPanel.add(row);
        conversationPanel.add(Box.createVerticalStrut(3));
        refreshConversation();
    }

    private String activityIcon(String kind) {
        switch (kind) {
            case "File": return "▱";
            case "Tool": return "⚙";
            case "Verify": return "✓";
            case "Planner": return "◇";
            case "Error": return "!";
            default: return "•";
        }
    }

    private String extractFileReference(String title, String result) {
        Matcher matcher = FILE_PATTERN.matcher(result == null ? "" : result);
        if (matcher.find()) {
            return matcher.group();
        }
        matcher = FILE_PATTERN.matcher(title == null ? "" : title);
        return matcher.find() ? matcher.group() : (title == null ? "result" : title);
    }

    private void appendTaskPanel() {
        conversationPanel.add(taskPanelHost);
        conversationPanel.add(Box.createVerticalStrut(6));
        refreshConversation();
    }

    private JPanel createThoughtLine(String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        row.setOpaque(false);
        JLabel brain = new JLabel("◌");
        brain.setForeground(SECONDARY);
        JLabel value = new JLabel(text);
        value.setForeground(SECONDARY);
        value.setFont(UI_FONT);
        row.add(brain);
        row.add(value);
        row.setAlignmentX(LEFT_ALIGNMENT);
        return row;
    }

    private void rebuildTaskPanel() {
        taskPanelHost.removeAll();
        if (activePlan == null) {
            return;
        }
        int done = 0;
        for (AgentTask task : activePlan.getTasks()) {
            if (task.getStatus() == TaskStatus.DONE) {
                done++;
            }
        }
        taskProgressLabel.setText(done + " / " + activePlan.getTasks().size() + " tasks done");
        JPanel card = roundedPanel(PANEL, BORDER, 8);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(roundedBorder(BORDER, 8),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel chevron = new JLabel("⌄");
        chevron.setForeground(SECONDARY);
        chevron.setFont(UI_FONT_MEDIUM);
        header.add(chevron, BorderLayout.WEST);
        header.add(taskProgressLabel, BorderLayout.CENTER);
        JPanel items = new JPanel();
        items.setOpaque(false);
        items.setLayout(new BoxLayout(items, BoxLayout.Y_AXIS));
        for (AgentTask task : activePlan.getTasks()) {
            items.add(taskRow(task));
        }
        card.add(header, BorderLayout.NORTH);
        card.add(items, BorderLayout.CENTER);
        taskPanelHost.add(card, BorderLayout.CENTER);
        taskPanelHost.setAlignmentX(LEFT_ALIGNMENT);
        taskPanelHost.revalidate();
        taskPanelHost.repaint();
    }

    private JPanel taskRow(AgentTask task) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        JLabel state = new JLabel(taskIcon(task.getStatus()));
        state.setFont(UI_FONT_MEDIUM);
        state.setForeground(taskColor(task.getStatus()));
        JLabel title = new JLabel(escape(task.getTitle()));
        title.setForeground(task.getStatus() == TaskStatus.PENDING ? SECONDARY : PRIMARY);
        title.setFont(UI_FONT);
        row.add(state, BorderLayout.WEST);
        row.add(title, BorderLayout.CENTER);
        return row;
    }

    private void appendCodeResult(String title, String result) {
        DiffStats diff = DiffStats.from(result);
        String fileName = extractFileReference(title, result);
        JPanel card = roundedPanel(PANEL, BORDER, 8);
        card.setLayout(new BorderLayout(8, 8));
        card.setBorder(BorderFactory.createCompoundBorder(roundedBorder(BORDER, 8),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        JLabel icon = new JLabel("▰");
        icon.setForeground(SECONDARY);
        JLabel name = new JLabel(escape(fileName));
        name.setForeground(PRIMARY);
        name.setFont(UI_FONT_MEDIUM);
        left.add(icon);
        left.add(name);
        if (diff.newFile) {
            JLabel badge = new JLabel("new");
            badge.setForeground(GREEN);
            badge.setOpaque(true);
            badge.setBackground(new Color(0x16, 0x3B, 0x2A));
            badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            badge.setFont(SMALL_FONT);
            left.add(badge);
        }
        JPanel counters = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        counters.setOpaque(false);
        JLabel added = new JLabel("+" + diff.added);
        added.setForeground(GREEN);
        added.setFont(CODE_FONT);
        JLabel removed = new JLabel("-" + diff.removed);
        removed.setForeground(RED);
        removed.setFont(CODE_FONT);
        counters.add(added);
        counters.add(removed);
        header.add(left, BorderLayout.WEST);
        header.add(counters, BorderLayout.EAST);
        JTextArea code = new JTextArea(result);
        code.setEditable(false);
        code.setLineWrap(false);
        code.setFont(CODE_FONT);
        code.setForeground(PRIMARY);
        code.setBackground(PANEL);
        code.setCaretColor(PRIMARY);
        code.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        JScrollPane scroll = new JScrollPane(code);
        scroll.setBorder(roundedBorder(BORDER, 6));
        scroll.setPreferredSize(new Dimension(10, Math.min(260, Math.max(90, result.split("\\R", -1).length * 18))));
        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.setAlignmentX(LEFT_ALIGNMENT);
        conversationPanel.add(card);
        conversationPanel.add(Box.createVerticalStrut(8));
        refreshConversation();
    }

    private void appendActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        actions.setOpaque(false);
        JButton like = iconButton("👍");
        like.setToolTipText("Good response");
        like.addActionListener(event -> updateStatus("Feedback recorded", GREEN));
        JButton dislike = iconButton("👎");
        dislike.setToolTipText("Poor response");
        dislike.addActionListener(event -> updateStatus("Feedback recorded", ORANGE));
        JButton copy = iconButton("□");
        copy.setToolTipText("Copy result");
        copy.addActionListener(event -> copyLastResult());
        JButton view = iconButton("▣");
        view.setToolTipText("Show response details");
        view.addActionListener(event -> showDetails());
        JButton more = iconButton("…");
        more.setToolTipText("More options");
        more.addActionListener(event -> showMoreMenu(more));
        actions.add(like);
        actions.add(dislike);
        actions.add(copy);
        actions.add(view);
        actions.add(more);
        actions.setAlignmentX(LEFT_ALIGNMENT);
        conversationPanel.add(actions);
        conversationPanel.add(Box.createVerticalStrut(8));
        refreshConversation();
    }

    private void showDetails() {
        String file = lastTitle == null || lastTitle.isBlank()
                ? "response" : extractFileReference(lastTitle, lastResult);
        updateStatus("Response details: " + file, BLUE);
    }

    private void showMoreMenu(JButton anchor) {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem copy = new JMenuItem("Copy result");
        copy.addActionListener(event -> copyLastResult());
        JMenuItem clear = new JMenuItem("Clear conversation");
        clear.addActionListener(event -> clearChat());
        menu.add(copy);
        menu.add(clear);
        menu.show(anchor, 0, anchor.getHeight());
    }

    private void copyLastResult() {
        if (lastResult.isBlank()) {
            updateStatus("Nothing to copy", SECONDARY);
            return;
        }
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(lastResult), null);
            updateStatus("Result copied", GREEN);
        } catch (IllegalStateException error) {
            updateStatus("Clipboard unavailable", RED);
        }
    }

    private void appendWarning(String message) {
        JPanel warning = roundedPanel(WARNING_BG, WARNING_BORDER, 8);
        warning.setLayout(new BorderLayout(8, 0));
        warning.setBorder(BorderFactory.createCompoundBorder(roundedBorder(WARNING_BORDER, 8),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));
        JLabel icon = new JLabel("▲");
        icon.setForeground(ORANGE);
        JLabel text = new JLabel("<html>" + escape(message) + "</html>");
        text.setForeground(ORANGE);
        text.setFont(UI_FONT);
        warning.add(icon, BorderLayout.WEST);
        warning.add(text, BorderLayout.CENTER);
        warning.setAlignmentX(LEFT_ALIGNMENT);
        conversationPanel.add(warning);
        conversationPanel.add(Box.createVerticalStrut(8));
        refreshConversation();
    }

    private JButton smallControl(String text) {
        JButton button = new JButton(text);
        button.setFont(UI_FONT);
        button.setForeground(SECONDARY);
        button.setBackground(PANEL);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 7, 5, 7));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        installHover(button, PANEL, HOVER);
        return button;
    }

    private JButton iconButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UI_FONT);
        button.setForeground(SECONDARY);
        button.setBackground(BG);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        installHover(button, BG, HOVER);
        return button;
    }

    private JButton roundButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UI_FONT_MEDIUM);
        button.setForeground(PRIMARY);
        button.setBackground(SEND_BG);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(7, 11, 7, 11));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        installHover(button, SEND_BG, HOVER);
        return button;
    }

    private void installHover(JButton button, Color normal, Color hover) {
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(hover);
                }
            }
            @Override public void mouseExited(MouseEvent e) {
                button.setBackground(normal);
            }
        });
    }

    private JPanel roundedPanel(Color background, Color border, int radius) {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        panel.setOpaque(false);
        panel.setBackground(background);
        panel.setBorder(roundedBorder(border, radius));
        return panel;
    }

    private static javax.swing.border.Border roundedBorder(Color color, int radius) {
        return new javax.swing.border.Border() {
            public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(color);
                g2.setStroke(new BasicStroke(1f));
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
                g2.dispose();
            }
            public Insets getBorderInsets(java.awt.Component c) {
                return new Insets(1, 1, 1, 1);
            }
            public boolean isBorderOpaque() {
                return false;
            }
        };
    }

    private String taskIcon(TaskStatus status) {
        switch (status) {
            case DONE: return "✓";
            case RUNNING: return "●";
            case VERIFYING: return "◌";
            case FAILED: return "!";
            case BLOCKED: return "×";
            default: return "○";
        }
    }

    private Color taskColor(TaskStatus status) {
        switch (status) {
            case DONE: return GREEN;
            case FAILED:
            case BLOCKED: return RED;
            case RUNNING:
            case VERIFYING: return ORANGE;
            default: return new Color(0x52, 0x52, 0x5B);
        }
    }

    private void updateStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    private void resetInputState() {
        isProcessing = false;
        activeRequest = null;
        sendButton.setEnabled(true);
        stopButton.setVisible(false);
        stopButton.setEnabled(false);
        promptInput.setEnabled(true);
        modelSelector.setEnabled(true);
        agentModeSelector.setEnabled(true);
        promptInput.requestFocus();
        updateSendAvailability();
    }

    private void refreshConversation() {
        conversationPanel.revalidate();
        conversationPanel.repaint();
    }

    private static String escape(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public void clearChat() {
        conversationPanel.removeAll();
        activePlan = null;
        lastResult = "";
        lastTitle = "";
        conversationPanel.add(createThoughtLine("Ready. Describe what you want changed."));
        conversationPanel.add(Box.createVerticalStrut(8));
        updateStatus("Ready", SECONDARY);
        refreshConversation();
    }

    public LlmClient getLlmClient() {
        return llmClient;
    }

    public boolean isProcessing() {
        return isProcessing;
    }

    private static final class DiffStats {
        private final int added;
        private final int removed;
        private final boolean newFile;

        private DiffStats(int added, int removed, boolean newFile) {
            this.added = added;
            this.removed = removed;
            this.newFile = newFile;
        }

        private static DiffStats from(String value) {
            int added = find(POSITIVE_DIFF, value);
            int removed = find(NEGATIVE_DIFF, value);
            boolean newFile = value.contains("new file") || value.contains("@@");
            if (added == 0 && removed == 0) {
                String[] lines = value.split("\\R", -1);
                int additions = 0;
                int deletions = 0;
                for (String line : lines) {
                    if (line.startsWith("+")) {
                        additions++;
                    }
                    if (line.startsWith("-")) {
                        deletions++;
                    }
                }
                added = additions;
                removed = deletions;
            }
            return new DiffStats(added, removed, newFile);
        }

        private static int find(Pattern pattern, String value) {
            Matcher matcher = pattern.matcher(value == null ? "" : value);
            return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
        }
    }
}
