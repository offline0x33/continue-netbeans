package com.bajinho.continuebeans;

import com.bajinho.continuebeans.task.AgentTask;
import com.bajinho.continuebeans.task.TaskOrchestrator;
import com.bajinho.continuebeans.task.TaskPlan;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;

/**
 * Chat UI Panel for Continue Beans plugin.
 * User goals are executed through the task orchestrator so the agent plans,
 * executes, verifies and only then reports completion.
 */
public class ChatPanel extends JPanel {

    private JTextArea chatOutput;
    private JTextField promptInput;
    private JButton sendButton;
    private JComboBox<String> modeSelector;
    private final JLabel statusLabel;
    private final LlmClient llmClient;
    private final TaskOrchestrator taskOrchestrator;
    private boolean isProcessing = false;

    public ChatPanel() {
        this.llmClient = new LlmClient();
        this.taskOrchestrator = new TaskOrchestrator();
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        chatOutput = new JTextArea();
        chatOutput.setEditable(false);
        chatOutput.setLineWrap(true);
        chatOutput.setWrapStyleWord(true);
        chatOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        chatOutput.setBackground(UIManager.getColor("EditorPane.background"));
        if (chatOutput.getBackground() == null) {
            chatOutput.setBackground(UIManager.getColor("TextArea.background"));
        }
        if (chatOutput.getBackground() == null) {
            chatOutput.setBackground(new Color(240, 240, 240));
        }

        JScrollPane outputScroll = new JScrollPane(chatOutput);
        outputScroll.setPreferredSize(new Dimension(600, 400));

        JPanel inputPanel = createInputPanel();
        statusLabel = new JLabel("Pronto");
        statusLabel.setForeground(new Color(0, 128, 0));

        add(outputScroll, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
        add(statusLabel, BorderLayout.NORTH);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        modeSelector = new JComboBox<>(new String[]{"Code", "Planning"});
        modeSelector.setSelectedItem("Code");

        promptInput = new JTextField(30);
        promptInput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        promptInput.addActionListener(e -> sendPrompt());

        sendButton = new JButton("Enviar");
        sendButton.addActionListener(e -> sendPrompt());

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(new JLabel("Modo:"));
        leftPanel.add(modeSelector);

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.add(promptInput, BorderLayout.CENTER);
        rightPanel.add(sendButton, BorderLayout.EAST);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.CENTER);
        return panel;
    }

    /** Execute the user's goal as a task plan. */
    private void sendPrompt() {
        String prompt = promptInput.getText().trim();
        if (prompt.isEmpty() || isProcessing) {
            return;
        }

        isProcessing = true;
        sendButton.setEnabled(false);
        promptInput.setEnabled(false);
        modeSelector.setEnabled(false);
        updateStatus("Planejando...", new Color(255, 165, 0));

        appendToChatOutput("👤 Você: " + prompt + "\n");
        appendToChatOutput("🤖 Agente: iniciando plano...\n");

        String provider = "lmstudio";
        taskOrchestrator.executeGoal(prompt, provider, new TaskOrchestrator.Listener() {
            @Override
            public void onPlanCreated(TaskPlan plan) {
                SwingUtilities.invokeLater(() -> {
                    appendToChatOutput("\n📋 Plano: " + plan.getGoal() + "\n");
                    int index = 1;
                    for (AgentTask task : plan.getTasks()) {
                        appendToChatOutput("  " + index++ + ". " + task.getTitle() + "\n");
                    }
                    updateStatus("Executando tarefas...", new Color(255, 165, 0));
                });
            }

            @Override
            public void onTaskStarted(AgentTask task) {
                SwingUtilities.invokeLater(() -> {
                    appendToChatOutput("▶ Tarefa: " + task.getTitle() + " (tentativa "
                            + task.getAttempts() + ")\n");
                    updateStatus("Executando: " + task.getTitle(), new Color(255, 165, 0));
                });
            }

            @Override
            public void onTaskVerifying(AgentTask task) {
                SwingUtilities.invokeLater(() -> {
                    appendToChatOutput("🔎 Verificando: " + task.getTitle() + "\n");
                    updateStatus("Verificando: " + task.getTitle(), new Color(255, 165, 0));
                });
            }

            @Override
            public void onTaskCompleted(AgentTask task) {
                SwingUtilities.invokeLater(() -> {
                    appendToChatOutput("✅ Concluída: " + task.getTitle() + "\n");
                });
            }

            @Override
            public void onTaskFailed(AgentTask task) {
                SwingUtilities.invokeLater(() -> {
                    appendToChatOutput("⚠ Falhou: " + task.getTitle()
                            + " — " + task.getLastError() + "\n");
                });
            }

            @Override
            public void onReplanning(TaskPlan failedPlan) {
                SwingUtilities.invokeLater(() -> {
                    appendToChatOutput("🔄 Replanejando após falha de tarefa...\n");
                    updateStatus("Replanejando...", new Color(255, 165, 0));
                });
            }

            @Override
            public void onCompleted(TaskPlan plan) {
                SwingUtilities.invokeLater(() -> {
                    appendToChatOutput("🏁 OBJETIVO CONCLUÍDO. Todas as tarefas foram verificadas.\n\n");
                    updateStatus("Concluído", new Color(0, 128, 0));
                    resetInputState();
                });
            }

            @Override
            public void onFailed(String message, TaskPlan plan) {
                SwingUtilities.invokeLater(() -> {
                    appendToChatOutput("❌ Execução interrompida: " + message + "\n\n");
                    updateStatus("Falha: " + message, new Color(255, 0, 0));
                    resetInputState();
                });
            }
        });

        promptInput.setText("");
    }

    private void appendToChatOutput(String text) {
        Document doc = chatOutput.getDocument();
        try {
            doc.insertString(doc.getLength(), text, null);
        } catch (BadLocationException e) {
            ContinueLogger.error("Failed to append to chat output", e);
        }
        chatOutput.setCaretPosition(doc.getLength());
    }

    private void resetInputState() {
        isProcessing = false;
        sendButton.setEnabled(true);
        promptInput.setEnabled(true);
        modeSelector.setEnabled(true);
        promptInput.requestFocus();
    }

    private void updateStatus(String text, Color color) {
        statusLabel.setText(text);
        statusLabel.setForeground(color);
    }

    public void clearChat() {
        chatOutput.setText("");
        updateStatus("Pronto", new Color(0, 128, 0));
    }

    public LlmClient getLlmClient() {
        return llmClient;
    }

    public boolean isProcessing() {
        return isProcessing;
    }
}
