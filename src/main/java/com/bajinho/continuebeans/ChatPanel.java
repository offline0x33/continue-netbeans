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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Dark task-driven chat UI. The visual state is a projection of the real
 * TaskOrchestrator lifecycle: plan -> run -> verify -> complete/replan.
 */
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
    private static final Font UI_FONT = new Font("Inter", Font.PLAIN, 13);
    private static final Font UI_FONT_MEDIUM = new Font("Inter", Font.BOLD, 13);
    private static final Font CODE_FONT = new Font("JetBrains Mono", Font.PLAIN, 12);

    private final LlmClient llmClient;
    private final TaskOrchestrator taskOrchestrator;
    private final JPanel conversationPanel;
    private final JPanel taskPanelHost;
    private final JLabel statusLabel;
    private final JLabel taskProgressLabel;
    private JTextField promptInput;
    private JButton sendButton;
    private JComboBox<String> modeSelector;
    private boolean isProcessing;
    private TaskPlan activePlan;

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

        JScrollPane scrollPane = new JScrollPane(conversationPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setBackground(BG);
        scrollPane.getViewport().setBackground(BG);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(BG);
        bottom.add(createInputBox(), BorderLayout.CENTER);
        bottom.add(createFooter(), BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        taskPanelHost = new JPanel(new BorderLayout());
        taskPanelHost.setOpaque(false);
        taskProgressLabel = new JLabel("0 / 0 tasks done");
        taskProgressLabel.setForeground(PRIMARY);
        taskProgressLabel.setFont(UI_FONT_MEDIUM);
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(SECONDARY);
        statusLabel.setFont(UI_FONT);

        conversationPanel.add(createThoughtLine("Ready. Describe what you want changed."));
        conversationPanel.add(Box.createVerticalStrut(8));
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
        JButton close = iconButton("×");
        tab.add(icon);
        tab.add(title);
        tab.add(close);
        bar.add(tab, BorderLayout.CENTER);

        JButton split = iconButton("▥");
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        actions.setOpaque(false);
        actions.add(split);
        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    private JPanel createInputBox() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(8, 16, 4, 16));

        JPanel box = roundedPanel(PANEL, BORDER);
        box.setLayout(new BorderLayout(8, 8));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(10, 12, 8, 12)));

        JLabel hint = new JLabel("Tip: Type @ conversation to bring in context from another chat");
        hint.setForeground(MUTED);
        hint.setFont(UI_FONT);

        promptInput = new JTextField();
        promptInput.setOpaque(false);
        promptInput.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
        promptInput.setForeground(PRIMARY);
        promptInput.setCaretColor(PRIMARY);
        promptInput.setFont(UI_FONT);
        promptInput.addActionListener(e -> sendPrompt());

        JPanel controls = new JPanel(new BorderLayout());
        controls.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        JButton attach = smallControl("+");
        JButton code = smallControl("<> Code");
        modeSelector = new JComboBox<>(new String[]{"SWE-1.6 Slow", "LM Studio", "OpenAI-compatible"});
        modeSelector.setSelectedIndex(1);
        modeSelector.setFont(UI_FONT);
        modeSelector.setForeground(PRIMARY);
        modeSelector.setBackground(PANEL);
        left.add(attach);
        left.add(code);
        left.add(modeSelector);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        JButton sync = smallControl("↻ Cascade");
        JButton mic = smallControl("◉");
        sendButton = roundButton("↑");
        sendButton.addActionListener(e -> sendPrompt());
        right.add(sync);
        right.add(mic);
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
        return wrapper;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 16, 6, 16));
        JLabel left = new JLabel("▰ Local   |   ▰ continue-netbeans");
        left.setForeground(SECONDARY);
        left.setFont(new Font("Inter", Font.PLAIN, 12));
        JLabel right = new JLabel("Migrate off Cascade");
        right.setForeground(SECONDARY);
        right.setFont(new Font("Inter", Font.PLAIN, 12));
        footer.add(left, BorderLayout.WEST);
        footer.add(right, BorderLayout.EAST);
        return footer;
    }

    private void sendPrompt() {
        String prompt = promptInput.getText().trim();
        if (prompt.isEmpty() || isProcessing) {
            return;
        }

        isProcessing = true;
        sendButton.setEnabled(false);
        promptInput.setEnabled(false);
        modeSelector.setEnabled(false);
        appendMessage("You", prompt, PRIMARY);
        appendThought("Thought for 0s", "Planning task graph...");

        String provider = "lmstudio";
        taskOrchestrator.executeGoal(prompt, provider, new TaskOrchestrator.Listener() {
            @Override
            public void onPlanCreated(TaskPlan plan) {
                SwingUtilities.invokeLater(() -> {
                    activePlan = plan;
                    rebuildTaskPanel();
                    appendTaskPanel();
                    updateStatus("Planning complete", BLUE);
                });
            }

            @Override
            public void onTaskStarted(AgentTask task) {
                SwingUtilities.invokeLater(() -> {
                    updateStatus("Running: " + task.getTitle(), ORANGE);
                    appendThought("Working on: " + task.getTitle(), "Attempt " + task.getAttempts());
                    rebuildTaskPanel();
                });
            }

            @Override
            public void onTaskVerifying(AgentTask task) {
                SwingUtilities.invokeLater(() -> {
                    updateStatus("Verifying: " + task.getTitle(), ORANGE);
                    appendThought("Verifying", task.getTitle());
                    rebuildTaskPanel();
                });
            }

            @Override
            public void onTaskCompleted(AgentTask task) {
                SwingUtilities.invokeLater(() -> {
                    rebuildTaskPanel();
                    if (task.getLastResult() != null && !task.getLastResult().isBlank()) {
                        appendCodeResult(task.getTitle(), task.getLastResult());
                    }
                });
            }

            @Override
            public void onTaskFailed(AgentTask task) {
                SwingUtilities.invokeLater(() -> {
                    rebuildTaskPanel();
                    appendThought("Task failed", task.getLastError());
                    updateStatus("Task failed", RED);
                });
            }

            @Override
            public void onReplanning(TaskPlan failedPlan) {
                SwingUtilities.invokeLater(() -> {
                    activePlan = failedPlan;
                    rebuildTaskPanel();
                    appendThought("Replanning", "Previous failure preserved as context");
                    updateStatus("Replanning", ORANGE);
                });
            }

            @Override
            public void onCompleted(TaskPlan plan) {
                SwingUtilities.invokeLater(() -> {
                    activePlan = plan;
                    rebuildTaskPanel();
                    appendThought("Objective completed", "All tasks verified");
                    appendActions();
                    updateStatus("Completed", GREEN);
                    resetInputState();
                });
            }

            @Override
            public void onFailed(String message, TaskPlan plan) {
                SwingUtilities.invokeLater(() -> {
                    activePlan = plan;
                    rebuildTaskPanel();
                    appendWarning(message);
                    updateStatus("Failed", RED);
                    resetInputState();
                });
            }
        });
        promptInput.setText("");
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

        JPanel card = roundedPanel(PANEL, BORDER);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(10, 12, 10, 12)));

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
        String normalized = result.length() > 700 ? result.substring(0, 700) + "…" : result;
        JPanel card = roundedPanel(PANEL, BORDER);
        card.setLayout(new BorderLayout(8, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JLabel header = new JLabel("▰  " + escape(title) + "   +result");
        header.setForeground(PRIMARY);
        header.setFont(UI_FONT_MEDIUM);
        JTextArea code = new JTextArea(normalized);
        code.setEditable(false);
        code.setLineWrap(false);
        code.setFont(CODE_FONT);
        code.setForeground(PRIMARY);
        code.setBackground(PANEL);
        code.setBorder(BorderFactory.createEmptyBorder());
        JScrollPane scroll = new JScrollPane(code);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
        scroll.setPreferredSize(new Dimension(10, 110));
        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        card.setAlignmentX(LEFT_ALIGNMENT);
        conversationPanel.add(card);
        conversationPanel.add(Box.createVerticalStrut(8));
        refreshConversation();
    }

    private void appendWarning(String message) {
        JPanel warning = roundedPanel(WARNING_BG, WARNING_BORDER);
        warning.setLayout(new BorderLayout(8, 0));
        warning.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WARNING_BORDER), BorderFactory.createEmptyBorder(10, 12, 10, 12)));
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

    private void appendActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(iconButton("♥"));
        actions.add(iconButton("♧"));
        actions.add(iconButton("□"));
        actions.add(iconButton("▣"));
        actions.add(iconButton("…"));
        actions.setAlignmentX(LEFT_ALIGNMENT);
        conversationPanel.add(actions);
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
        return button;
    }

    private JButton iconButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UI_FONT);
        button.setForeground(SECONDARY);
        button.setBackground(BG);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        return button;
    }

    private JButton roundButton(String text) {
        JButton button = new JButton(text);
        button.setFont(UI_FONT_MEDIUM);
        button.setForeground(PRIMARY);
        button.setBackground(new Color(0x3F, 0x3F, 0x46));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(7, 11, 7, 11));
        return button;
    }

    private JPanel roundedPanel(Color background, Color border) {
        JPanel panel = new JPanel();
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 1),
                BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        return panel;
    }

    private String taskIcon(TaskStatus status) {
        switch (status) {
            case DONE:
                return "✓";
            case RUNNING:
                return "●";
            case VERIFYING:
                return "◌";
            case FAILED:
                return "!";
            case BLOCKED:
                return "×";
            default:
                return "○";
        }
    }

    private Color taskColor(TaskStatus status) {
        switch (status) {
            case DONE:
                return GREEN;
            case FAILED:
            case BLOCKED:
                return RED;
            case RUNNING:
            case VERIFYING:
                return ORANGE;
            default:
                return new Color(0x52, 0x52, 0x5B);
        }
    }

    private void updateStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    private void resetInputState() {
        isProcessing = false;
        sendButton.setEnabled(true);
        promptInput.setEnabled(true);
        modeSelector.setEnabled(true);
        promptInput.requestFocus();
    }

    private void refreshConversation() {
        conversationPanel.revalidate();
        conversationPanel.repaint();
        SwingUtilities.invokeLater(() -> {
            if (getParent() != null) {
                getParent().revalidate();
            }
        });
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
}
