package com.bajinho.continuebeans;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;

/**
 * Chat UI Panel for Continue Beans plugin.
 * Provides input field, mode selection, streaming output, and error display.
 */
public class ChatPanel extends JPanel {

    private JTextArea chatOutput;
    private JTextField promptInput;
    private JButton sendButton;
    private JComboBox<String> modeSelector;
    private final JLabel statusLabel;
    private final LlmClient llmClient;
    private boolean isProcessing = false;

    public ChatPanel() {
        this.llmClient = new LlmClient();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Chat Output Area
        chatOutput = new JTextArea();
        chatOutput.setEditable(false);
        chatOutput.setLineWrap(true);
        chatOutput.setWrapStyleWord(true);
        chatOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        // Use NetBeans theme background color
        chatOutput.setBackground(UIManager.getColor("EditorPane.background"));
        if (chatOutput.getBackground() == null) {
            chatOutput.setBackground(UIManager.getColor("TextArea.background"));
        }
        if (chatOutput.getBackground() == null) {
            chatOutput.setBackground(new Color(240, 240, 240)); // fallback
        }

        JScrollPane outputScroll = new JScrollPane(chatOutput);
        outputScroll.setPreferredSize(new Dimension(600, 400));

        // Input Panel
        JPanel inputPanel = createInputPanel();

        // Status Label
        statusLabel = new JLabel("Pronto");
        statusLabel.setForeground(new Color(0, 128, 0));

        // Main Layout
        add(outputScroll, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));

        // Mode Selector
        modeSelector = new JComboBox<>(new String[]{"Code", "Planning"});
        modeSelector.setSelectedItem("Code");

        // Prompt Input
        promptInput = new JTextField(30);
        promptInput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        promptInput.addActionListener(e -> sendPrompt()); // Send on Enter

        // Send Button
        sendButton = new JButton("Enviar");
        sendButton.addActionListener(e -> sendPrompt());

        // Left: Mode selector
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(new JLabel("Modo:"));
        leftPanel.add(modeSelector);

        // Right: Input and button
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.add(promptInput, BorderLayout.CENTER);
        rightPanel.add(sendButton, BorderLayout.EAST);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);

        return panel;
    }

    /**
     * Send prompt to LLM and stream response.
     */
    private void sendPrompt() {
        String prompt = promptInput.getText().trim();
        if (prompt.isEmpty() || isProcessing) {
            return;
        }

        isProcessing = true;
        sendButton.setEnabled(false);
        promptInput.setEnabled(false);
        modeSelector.setEnabled(false);

        updateStatus("Processando...", new Color(255, 165, 0));
        appendToChatOutput("👤 Você: " + prompt + "\n");
        appendToChatOutput("🤖 Assistente: ");

        String mode = (String) modeSelector.getSelectedItem();
        String selectedModel = "llama3"; // Default model

        // Create provider instance (in real implementation, would use singleton)
        LmStudioProvider provider = new LmStudioProvider(java.net.http.HttpClient.newHttpClient(), new com.google.gson.Gson());

        // Stream response
        provider.stream(null, prompt, selectedModel, mode,
                chunk -> SwingUtilities.invokeLater(() -> appendToChatOutput(chunk)),
                error -> SwingUtilities.invokeLater(() -> {
                    appendToChatOutput("\n❌ Erro: " + error.getMessage() + "\n\n");
                    updateStatus("Erro: " + error.getMessage(), new Color(255, 0, 0));
                    resetInputState();
                }),
                () -> SwingUtilities.invokeLater(this::onComplete));

        promptInput.setText("");
    }

    /**
     * Append text to chat output.
     */
    private void appendToChatOutput(String text) {
        Document doc = chatOutput.getDocument();
        try {
            doc.insertString(doc.getLength(), text, null);
        } catch (BadLocationException e) {
            ContinueLogger.error("Failed to append to chat output", e);
        }

        // Auto-scroll to bottom
        chatOutput.setCaretPosition(doc.getLength());
    }

    /**
     * Called when streaming completes.
     */
    private void onComplete() {
        appendToChatOutput("\n\n");
        updateStatus("Pronto", new Color(0, 128, 0));
        resetInputState();
    }

    /**
     * Reset input controls to enabled state.
     */
    private void resetInputState() {
        isProcessing = false;
        sendButton.setEnabled(true);
        promptInput.setEnabled(true);
        modeSelector.setEnabled(true);
        promptInput.requestFocus();
    }

    /**
     * Update status label with color.
     */
    private void updateStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    /**
     * Clear chat history.
     */
    public void clearChat() {
        chatOutput.setText("");
        updateStatus("Pronto", new Color(0, 128, 0));
    }

    /**
     * Get the underlying LlmClient.
     */
    public LlmClient getLlmClient() {
        return llmClient;
    }

    /**
     * Check if chat is currently processing.
     */
    public boolean isProcessing() {
        return isProcessing;
    }
}
