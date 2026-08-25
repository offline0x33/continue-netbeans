package com.bajinho.continuebeans.ui;

import com.bajinho.continuebeans.ContinueSettings;
import com.bajinho.continuebeans.LlmClient;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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

/**
 * Dark chat UI implementing the documented Continue Beans visual contract.
 */
public final class DarkChatPanel extends JPanel {

    private static final Color BG = Color.decode("#121214");
    private static final Color PANEL = Color.decode("#1A1A1E");
    private static final Color BORDER = Color.decode("#27272A");
    private static final Color PRIMARY = Color.decode("#E4E4E7");
    private static final Color SECONDARY = Color.decode("#A1A1AA");
    private static final Color MUTED = Color.decode("#71717A");
    private static final Color GREEN = Color.decode("#4ADE80");
    private static final Color RED = Color.decode("#F87171");
    private static final Color BLUE = Color.decode("#60A5FA");
    private static final Color ORANGE = Color.decode("#F97316");
    private static final Color WARNING_BG = Color.decode("#2D1C11");
    private static final Color WARNING_BORDER = Color.decode("#522E15");
    private static final Color SEND_BG = Color.decode("#3F3F46");
    private static final Font UI = new Font("Inter", Font.PLAIN, 13);
    private static final Font UI_MEDIUM = new Font("Inter", Font.BOLD, 13);
    private static final Font CODE = new Font("JetBrains Mono", Font.PLAIN, 12);

    private final LlmClient client = new LlmClient();
    private final JPanel transcript = new JPanel();
    private final JPanel taskBody = new JPanel();
    private final JLabel taskSummary = new JLabel("0 / 0 tasks done");
    private final JLabel status = new JLabel("Ready");
    private final JTextField input = new JTextField();
    private final JComboBox<String> modelSelector = new JComboBox<>();
    private final JButton send = roundButton("↑");

    private boolean taskExpanded = true;
    private boolean codeMode;

    public DarkChatPanel() {
        setLayout(new BorderLayout());
        setBackground(BG);
        add(createTopBar(), BorderLayout.NORTH);
        add(createMainArea(), BorderLayout.CENTER);
        add(createBottomArea(), BorderLayout.SOUTH);

        loadConfiguredModel();
        appendThought("Thought for 0s", "Ready for your next task.");
        addTask("Understand request", true);
        addTask("Generate response", false);
        addTask("Verify response", false);
    }

    private JPanel createTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.decode("#09090B"));
        bar.setPreferredSize(new Dimension(10, 40));

        JPanel tab = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 6));
        tab.setBackground(BG);
        tab.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
        JLabel icon = label("●", BLUE, UI_MEDIUM);
        JLabel title = label("NetBeans Plugin Integration", PRIMARY, UI_MEDIUM);
        JButton close = iconButton("×");
        tab.add(icon);
        tab.add(title);
        tab.add(close);
        bar.add(tab, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 5));
        actions.setOpaque(false);
        actions.add(iconButton("▥"));
        bar.add(actions, BorderLayout.EAST);
        return bar;
    }

    private JPanel createMainArea() {
        transcript.setLayout(new BoxLayout(transcript, BoxLayout.Y_AXIS));
        transcript.setBackground(BG);
        transcript.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JScrollPane scroll = new JScrollPane(transcript);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        return scroll;
    }

    private JPanel createBottomArea() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBackground(BG);
        bottom.add(createInputBox(), BorderLayout.CENTER);
        bottom.add(createFooter(), BorderLayout.SOUTH);
        return bottom;
    }

    private JPanel createInputBox() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        wrapper.setBorder(BorderFactory.createEmptyBorder(8, 16, 4, 16));

        JPanel box = roundedPanel(PANEL, BORDER);
        box.setLayout(new BorderLayout(6, 6));
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(10, 12, 8, 12)));

        JLabel hint = label("Tip: Type @ conversation to bring in context from another chat", MUTED, UI);
        input.setOpaque(false);
        input.setForeground(PRIMARY);
        input.setCaretColor(PRIMARY);
        input.setFont(UI);
        input.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        input.addActionListener(e -> sendMessage());

        JPanel controls = new JPanel(new BorderLayout());
        controls.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        left.setOpaque(false);

        JButton attach = smallButton("+");
        attach.addActionListener(e -> appendThought("Tip", "File/context attachment can be added here."));
        JButton code = smallButton("<> Code");
        code.addActionListener(e -> {
            codeMode = !codeMode;
            code.setForeground(codeMode ? GREEN : PRIMARY);
        });
        modelSelector.setFont(UI);
        modelSelector.setForeground(PRIMARY);
        modelSelector.setBackground(PANEL);
        modelSelector.setEditable(true);
        modelSelector.setPreferredSize(new Dimension(150, 28));
        left.add(attach);
        left.add(code);
        left.add(modelSelector);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        right.setOpaque(false);
        right.add(smallButton("↻ Cascade"));
        JButton mic = smallButton("◉");
        mic.setToolTipText("Voice input");
        right.add(mic);
        send.addActionListener(e -> sendMessage());
        right.add(send);

        controls.add(left, BorderLayout.WEST);
        controls.add(right, BorderLayout.EAST);

        JPanel center = new JPanel(new BorderLayout(3, 3));
        center.setOpaque(false);
        center.add(hint, BorderLayout.NORTH);
        center.add(input, BorderLayout.CENTER);
        center.add(controls, BorderLayout.SOUTH);

        box.add(center, BorderLayout.CENTER);
        wrapper.add(box, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(BG);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 16, 6, 16));
        footer.add(label("▰ Local   |   ▰ continue-netbeans", SECONDARY, new Font("Inter", Font.PLAIN, 12)), BorderLayout.WEST);
        footer.add(label("Migrate off Cascade", SECONDARY, new Font("Inter", Font.PLAIN, 12)), BorderLayout.EAST);
        return footer;
    }

    private void loadConfiguredModel() {
        String current = ContinueSettings.getModel();
        modelSelector.addItem("SWE-1.6 Slow");
        if (current != null && !current.isBlank() && !current.equals("SWE-1.6 Slow")) {
            modelSelector.addItem(current);
            modelSelector.setSelectedItem(current);
        } else {
            modelSelector.setSelectedIndex(0);
        }
    }

    private void sendMessage() {
        String prompt = input.getText().trim();
        if (prompt.isEmpty()) {
            return;
        }
        input.setText("");
        send.setEnabled(false);

        appendUserMessage(prompt);
        appendThought("Thought for 0s", codeMode ? "Preparing code response..." : "Preparing response...");
        setTaskState(1, true);
        status.setText("Working");
        status.setForeground(ORANGE);

        String model = String.valueOf(modelSelector.getSelectedItem());
        ContinueSettings.setModel(model);
        StringBuilder response = new StringBuilder();

        client.perguntarIAStreaming(null, prompt, model, codeMode ? "Code" : "Ask",
                chunk -> SwingUtilities.invokeLater(() -> {
                    response.append(chunk);
                    appendStreamingChunk(chunk);
                }),
                error -> SwingUtilities.invokeLater(() -> {
                    send.setEnabled(true);
                    status.setText("Error");
                    status.setForeground(RED);
                    appendThought("Request failed", error.getMessage());
                }),
                () -> SwingUtilities.invokeLater(() -> {
                    send.setEnabled(true);
                    status.setText("Ready");
                    status.setForeground(SECONDARY);
                    setTaskState(2, true);
                    appendMessageToolbar();
                }));
    }

    private void appendUserMessage(String message) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        JLabel label = label("You", PRIMARY, UI_MEDIUM);
        JTextArea text = text(message, PRIMARY, UI);
        row.add(label, BorderLayout.NORTH);
        row.add(text, BorderLayout.CENTER);
        transcript.add(row);
        transcript.add(Box.createVerticalStrut(10));
        scrollEnd();
    }

    private void appendStreamingChunk(String chunk) {
        if (transcript.getComponentCount() == 0 || !(transcript.getComponent(transcript.getComponentCount() - 1) instanceof JPanel)) {
            appendCodeCard("Assistant", "");
        }
        JPanel card = (JPanel) transcript.getComponent(transcript.getComponentCount() - 2);
        if (card.getComponentCount() > 1 && card.getComponent(1) instanceof JScrollPane) {
            JScrollPane scroll = (JScrollPane) card.getComponent(1);
            if (scroll.getViewport().getView() instanceof JTextArea) {
                JTextArea area = (JTextArea) scroll.getViewport().getView();
                area.append(chunk);
                area.setCaretPosition(area.getDocument().getLength());
            }
        }
        scrollEnd();
    }

    private void appendCodeCard(String title, String content) {
        JPanel card = roundedPanel(PANEL, BORDER);
        card.setLayout(new BorderLayout(8, 8));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(10, 14, 10, 14)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        left.setOpaque(false);
        left.add(label("Y", SECONDARY, UI_MEDIUM));
        left.add(label(title, PRIMARY, UI_MEDIUM));
        JLabel badge = label("new", GREEN, new Font("Inter", Font.PLAIN, 11));
        badge.setOpaque(true);
        badge.setBackground(Color.decode("#163B2A"));
        badge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        left.add(badge);
        header.add(left, BorderLayout.WEST);
        header.add(label("+106   -31", SECONDARY, CODE), BorderLayout.EAST);

        JTextArea code = text(content, SECONDARY, CODE);
        code.setEditable(false);
        code.setLineWrap(false);
        JScrollPane scroll = new JScrollPane(code);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(PANEL);
        scroll.getViewport().setBackground(PANEL);
        card.add(header, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);

        transcript.add(card);
        transcript.add(Box.createVerticalStrut(10));
    }

    private void appendThought(String title, String detail) {
        JPanel line = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        line.setOpaque(false);
        line.add(label("◉", SECONDARY, UI));
        line.add(label(title, SECONDARY, UI));
        if (detail != null && !detail.isBlank()) {
            line.add(label(detail, BLUE, UI));
        }
        transcript.add(line);
        transcript.add(Box.createVerticalStrut(5));
        scrollEnd();
    }

    private void appendTaskPanel() {
        JPanel panel = roundedPanel(PANEL, BORDER);
        panel.setLayout(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        JButton header = new JButton("⌄");
        header.setBorderPainted(false);
        header.setContentAreaFilled(false);
        header.setForeground(PRIMARY);
        header.addActionListener(e -> {
            taskExpanded = !taskExpanded;
            taskBody.setVisible(taskExpanded);
            revalidate();
            repaint();
        });
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        JPanel title = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        title.setOpaque(false);
        title.add(header);
        title.add(taskSummary);
        taskSummary.setFont(UI_MEDIUM);
        taskSummary.setForeground(PRIMARY);
        head.add(title, BorderLayout.WEST);

        taskBody.setOpaque(false);
        taskBody.setLayout(new GridLayout(0, 1, 0, 4));
        panel.add(head, BorderLayout.NORTH);
        panel.add(taskBody, BorderLayout.CENTER);
        transcript.add(panel);
        transcript.add(Box.createVerticalStrut(10));
    }

    private void addTask(String name, boolean completed) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 7, 0));
        row.setOpaque(false);
        row.add(label(completed ? "✓" : "○", completed ? GREEN : Color.decode("#52525B"), UI_MEDIUM));
        row.add(label(name, completed ? PRIMARY : SECONDARY, UI));
        taskBody.add(row);
        updateTaskSummary();
    }

    private void setTaskState(int taskIndex, boolean completed) {
        if (taskBody.getComponentCount() <= taskIndex) {
            return;
        }
        JPanel row = (JPanel) taskBody.getComponent(taskIndex);
        JLabel icon = (JLabel) row.getComponent(0);
        JLabel name = (JLabel) row.getComponent(1);
        icon.setText(completed ? "✓" : "○");
        icon.setForeground(completed ? GREEN : Color.decode("#52525B"));
        name.setForeground(completed ? PRIMARY : SECONDARY);
        updateTaskSummary();
    }

    private void updateTaskSummary() {
        int done = 0;
        for (java.awt.Component component : taskBody.getComponents()) {
            if (component instanceof JPanel) {
                JLabel icon = (JLabel) ((JPanel) component).getComponent(0);
                if ("✓".equals(icon.getText())) {
                    done++;
                }
            }
        }
        taskSummary.setText(done + " / " + taskBody.getComponentCount() + " tasks done");
    }

    private void appendWarning() {
        JPanel warning = roundedPanel(WARNING_BG, WARNING_BORDER);
        warning.setLayout(new BorderLayout(8, 0));
        warning.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(WARNING_BORDER), BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        warning.add(label("▲", ORANGE, UI_MEDIUM), BorderLayout.WEST);
        warning.add(label("Your included daily usage quota is exhausted. Purchase extra usage to continue using premium models.", PRIMARY, UI), BorderLayout.CENTER);
        transcript.add(warning);
        transcript.add(Box.createVerticalStrut(10));
        scrollEnd();
    }

    private void appendMessageToolbar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        bar.setOpaque(false);
        bar.add(iconButton("👍"));
        bar.add(iconButton("👎"));
        bar.add(iconButton("⧉"));
        bar.add(iconButton("▤"));
        bar.add(iconButton("…"));
        transcript.add(bar);
        transcript.add(Box.createVerticalStrut(8));
        scrollEnd();
    }

    private void appendCodeResult(String content) {
        appendCodeCard("release.yml", content);
    }

    private void scrollEnd() {
        SwingUtilities.invokeLater(() -> {
            if (getComponentCount() > 1 && getComponent(1) instanceof JScrollPane) {
                JScrollPane scroll = (JScrollPane) getComponent(1);
                scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
            }
        });
    }

    private static JPanel roundedPanel(Color background, Color border) {
        JPanel panel = new JPanel();
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createLineBorder(border));
        return panel;
    }

    private static JLabel label(String text, Color color, Font font) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(font);
        return label;
    }

    private static JTextArea text(String text, Color color, Font font) {
        JTextArea area = new JTextArea(text);
        area.setOpaque(false);
        area.setForeground(color);
        area.setFont(font);
        area.setBorder(BorderFactory.createEmptyBorder());
        return area;
    }

    private static JButton iconButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(SECONDARY);
        button.setFont(UI);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private static JButton smallButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(PRIMARY);
        button.setBackground(PANEL);
        button.setFont(UI);
        button.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        button.setFocusPainted(false);
        return button;
    }

    private static JButton roundButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(PRIMARY);
        button.setBackground(SEND_BG);
        button.setFont(UI_MEDIUM);
        button.setPreferredSize(new Dimension(36, 30));
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        return button;
    }
}
