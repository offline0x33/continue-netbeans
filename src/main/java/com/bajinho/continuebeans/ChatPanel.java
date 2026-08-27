package com.bajinho.continuebeans;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/** Canonical dark chat panel used by the NetBeans assistant UI. */
public class ChatPanel extends JPanel {
    private static final Color BACKGROUND = new Color(0x12, 0x12, 0x14);
    private static final Color SURFACE = new Color(0x1A, 0x1A, 0x1E);
    private static final Color TEXT = new Color(0xE8, 0xE8, 0xEC);

    private final LlmClient llmClient = new LlmClient();
    private final JTextArea conversation = new JTextArea();
    private final JTextField composer = new JTextField();
    private boolean processing;

    public ChatPanel() {
        super(new BorderLayout());
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel readyState = new JLabel("Ready. Describe what you want changed.");
        readyState.setForeground(TEXT);
        header.add(readyState, BorderLayout.WEST);

        JComboBox<String> modeSelector = new JComboBox<>(new String[] {
            ContinueSettings.getAgentMode().getLabel()
        });
        modeSelector.setSelectedIndex(0);
        header.add(modeSelector, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        conversation.setEditable(false);
        conversation.setLineWrap(true);
        conversation.setWrapStyleWord(true);
        conversation.setBackground(BACKGROUND);
        conversation.setForeground(TEXT);
        conversation.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(new JScrollPane(conversation), BorderLayout.CENTER);

        JPanel composerPanel = new JPanel(new BorderLayout(8, 0));
        composerPanel.setOpaque(false);
        composer.setBackground(SURFACE);
        composer.setForeground(TEXT);
        composer.setCaretColor(TEXT);
        composer.setPreferredSize(new Dimension(0, 36));
        composerPanel.add(composer, BorderLayout.CENTER);

        JButton send = new JButton("↑");
        send.setToolTipText("Send message");
        composerPanel.add(send, BorderLayout.EAST);
        add(composerPanel, BorderLayout.SOUTH);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        footer.setOpaque(false);
        footer.add(new JLabel("Tip: Type @ conversation"));
        footer.add(new JLabel("Local · continue-netbeans"));
        footer.add(new JLabel("Assistant"));
        JButton refresh = new JButton("↻");
        refresh.setToolTipText("Refresh models");
        footer.add(refresh);
        add(footer, BorderLayout.PAGE_END);

        send.addActionListener(e -> sendMessage());
        composer.addActionListener(e -> sendMessage());
        refresh.addActionListener(e -> llmClient.getModelosDisponiveisAsync());
    }

    private void sendMessage() {
        if (processing || composer.getText().trim().isEmpty()) {
            return;
        }
        String message = composer.getText().trim();
        composer.setText("");
        processing = true;
        llmClient.perguntarIAStreaming("", message, ContinueSettings.getModel(),
                ContinueSettings.getAgentMode().getLabel(),
                chunk -> javax.swing.SwingUtilities.invokeLater(() -> conversation.append("\n" + chunk)),
                error -> javax.swing.SwingUtilities.invokeLater(() -> {
                    conversation.append("\nErro: " + error.getMessage());
                    processing = false;
                }),
                () -> javax.swing.SwingUtilities.invokeLater(() -> processing = false));
    }

    public LlmClient getLlmClient() {
        return llmClient;
    }

    public boolean isProcessing() {
        return processing;
    }

    public void clearChat() {
        conversation.setText("Ready. Describe what you want changed.");
    }
}
