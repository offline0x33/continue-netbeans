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
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
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
    private static final Font UI_FONT = new Font("Inter", Font.PLAIN, 13);
    private static final Font UI_FONT_MEDIUM = new Font("Inter", Font.BOLD, 13);
    private static final Font SMALL_FONT = new Font("Inter", Font.PLAIN, 12);
    private static final Font CODE_FONT = new Font("JetBrains Mono", Font.PLAIN, 12);
    private static final Pattern POSITIVE_DIFF = Pattern.compile("(?:^|\\s)\\+(\\d+)(?=\\s|$)");
    private static final Pattern NEGATIVE_DIFF = Pattern.compile("(?:^|\\s)-(\\d+)(?=\\s|$)");

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
    private String lastResult = "";

    public ChatPanel() {
        llmClient = new LlmClient();
        taskOrchestrator = new TaskOrchestrator();
        isProcessing = false;

        setLayout(new BorderLayout());
        setBackground(BG);
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

        JPanel box = roundedPanel(PANEL, BORDER);
        box.setLayout(new BorderLayout(8, 8));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 12, 8, 12)));

        JLabel hint = new JLabel("Tip: Type @ conversation to bring in context from another chat");
        hint.setForeground(MUTED);
        hint.setFont(UI_FONT);

        promptInput = new JTextField();
        promptInput.setOpaque(false);
        promptInput.setBorder(BorderFactory.createEmptyBorder());
        promptInput.setForeground(PRIMARY);
        promptInput.setCaretColor(PRIMARY);
        promptInput.setFont(UI_FONT);
        promptInput.addActionListener(event -> sendPrompt());

        JPanel controls = new JPanel(new BorderLayout());
        controls.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        left.add(smallControl("+"));
        left.add(smallControl("<> Code"));
        modeSelector = new JComboBox<>(new String[]{"SWE-1.6 Slow", "LM Studio", "OpenAI-compatible"});
        modeSelector.setSelectedIndex(1);
        modeSelector.setFont(UI_FONT);
        modeSelector.setForeground(PRIMARY);
        modeSelector.setBackground(PANEL);
        left.add(modeSelector);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(smallControl("↻ Cascade"));
        right.add(smallControl("◉"));
        sendButton = roundButton("↑");
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
        return wrapper;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 16, 6, 16));
        JLabel left = new JLabel("▰ Local   |   ▰ continue-netbeans");
        left.setForeground(SECONDARY);
        left.setFont(SMALL_FONT);
        JLabel right = new JLabel("Migrate off Cascade");
        right.setForeground(SECONDARY);
        right.setFont(SMALL_FONT);
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
        taskOrchestrator.executeGoal(prompt, "lmstudio", new TaskOrchestrator.Listener() {
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
                        lastResult = task.getLastResult();
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
                BorderFactory.createLineBorder(BORDER),
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
        JPanel card = roundedPanel(PANEL, BORDER);
        card.setLayout(new BorderLayout(8, 8));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)));

        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);
        JLabel icon = new JLabel("▰");
        icon.setForeground(SECONDARY);
        JLabel name = new JLabel(escape(title));
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
        scroll.setBorder(BorderFactory.createLineBorder(BORDER));
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
        like.addActionListener(event -> updateStatus("Feedback recorded", GREEN));
        JButton dislike = iconButton("👎");
        dislike.addActionListener(event -> updateStatus("Feedback recorded", ORANGE));
        JButton copy = iconButton("□");
        copy.setToolTipText("Copy result");
        copy.addActionListener(event -> copyLastResult());
        JButton view = iconButton("▣");
        view.setToolTipText("Toggle details");
        view.addActionListener(event -> updateStatus("Details available in the conversation", BLUE));
        JButton more = iconButton("…");
        more.setToolTipText("More options");
        more.addActionListener(event -> updateStatus("More options are reserved for the current response", SECONDARY));
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
        JPanel warning = roundedPanel(WARNING_BG, WARNING_BORDER);
        warning.setLayout(new BorderLayout(8, 0));
        warning.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WARNING_BORDER),
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
        sendButton.setEnabled(true);
        promptInput.setEnabled(true);
        modeSelector.setEnabled(true);
        promptInput.requestFocus();
    }

    private void refreshConversation() {
        conversationPanel.revalidate();
        conversationPanel.repaint();
    }

    private static JPanel createThoughtLinePanel(String text) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        row.setOpaque(false);
        JLabel brain = new JLabel("◌");
        brain.setForeground(SECONDARY);
        JLabel value = new JLabel(text);
        value.setForeground(SECONDARY);
        value.setFont(UI_FONT);
        row.add(brain);
        row.add(value);
        return row;
    }

    private JPanel createThoughtLine(String text) {
        return createThoughtLinePanel(text);
    }

    private static String escape(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public void clearChat() {
        conversationPanel.removeAll();
        activePlan = null;
        lastResult = "";
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
                for (String line : lines) {
                    if (line.startsWith("+")) additions++;
                    if (line.startsWith("-")) removed++;
                }
                added = additions;
            }
            return new DiffStats(added, removed, newFile);
        }

        private static int find(Pattern pattern, String value) {
            Matcher matcher = pattern.matcher(value);
            return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
        }
    }
}
