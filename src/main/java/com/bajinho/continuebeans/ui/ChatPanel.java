package com.bajinho.continuebeans.ui;

import com.bajinho.continuebeans.ai.LMStudioTextIntegration;
import com.bajinho.continuebeans.ai.NetBeansFunctionDefinitions;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

/**
 * ChatPanel with REAL LM Studio integration and NetBeans control.
 * This is the REAL implementation that connects LM Studio with NetBeans.
 *
 * @author Continue Beans Team
 */
public class ChatPanel extends JPanel {
    private static final Logger LOG = Logger.getLogger(ChatPanel.class.getName());
    private LMStudioTextIntegration lmStudio;
    private JTextArea chatOutput;
    private JTextField inputField;
    private JButton sendButton;
    private JButton settingsButton;
    private JLabel statusLabel;

    public ChatPanel() {
        createUI();
        initializeLMStudio();
    }

    private void initializeLMStudio() {
        try {
            String baseUrl = "http://127.0.0.1:1234";
            String modelName = "qwen3-4b-function-calling-finetuned";
            lmStudio = new LMStudioTextIntegration(baseUrl, modelName);
            lmStudio.testConnection().thenAccept(connected ->
                SwingUtilities.invokeLater(() -> {
                    if (connected) {
                        statusLabel.setText("✅ LM Studio Connected");
                        statusLabel.setForeground(Color.GREEN);
                        LOG.info("LM Studio connection successful");
                    } else {
                        statusLabel.setText("❌ LM Studio Not Connected - Check if server is running on port 1234");
                        statusLabel.setForeground(Color.RED);
                        LOG.warning("LM Studio connection failed - make sure LM Studio server is running on http://127.0.0.1:1234");
                    }
                }));
            LOG.info("Text-based integration ready");
        } catch (Exception e) {
            LOG.severe("Failed to initialize LM Studio: " + e.getMessage());
            if (statusLabel != null) {
                statusLabel.setText("❌ Connection Error");
                statusLabel.setForeground(Color.RED);
            }
        }
    }

    private void createUI() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(600, 400));

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("🔄 Connecting to LM Studio...");
        statusLabel.setForeground(Color.ORANGE);
        statusPanel.add(statusLabel);

        settingsButton = new JButton("⚙️");
        settingsButton.setFont(new Font("Arial", Font.BOLD, 12));
        settingsButton.setToolTipText("Configuration Settings");
        settingsButton.setBackground(new Color(108, 117, 125));
        settingsButton.setForeground(Color.WHITE);
        settingsButton.setFocusPainted(false);
        settingsButton.addActionListener(e -> openConfiguration());
        statusPanel.add(settingsButton);
        add(statusPanel, BorderLayout.NORTH);

        chatOutput = new JTextArea();
        chatOutput.setEditable(false);
        chatOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        chatOutput.setLineWrap(true);
        chatOutput.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(chatOutput);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(80, 30));
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(inputPanel, BorderLayout.SOUTH);

        sendButton.addActionListener(this::onSendMessage);
        inputField.addActionListener(this::onSendMessage);
        appendToChat("🤖 Continue Beans - LM Studio Integration\n");
        appendToChat("💬 Ask me to create files, analyze code, or manage your NetBeans project!\n\n");
    }

    private void onSendMessage(ActionEvent e) {
        String message = inputField.getText().trim();
        if (message.isEmpty()) return;
        inputField.setText("");
        appendToChat("👤 You: " + message + "\n");
        sendButton.setEnabled(false);
        inputField.setEnabled(false);
        statusLabel.setText("🤔 Thinking...");
        processMessage(message);
    }

    private void processMessage(String message) {
        if (lmStudio == null) {
            appendToChat("❌ LM Studio not connected. Please start LM Studio first.\n\n");
            resetInputControls();
            return;
        }
        lmStudio.processRequest(message)
            .thenAccept(response -> SwingUtilities.invokeLater(() -> {
                appendToChat("🤖 AI: " + response + "\n\n");
                if (response.contains("✅ **Função NetBeans executada")) {
                    appendToChat("🔧 NetBeans operation completed successfully!\n");
                    showNetBeansNotification();
                }
                resetInputControls();
            }))
            .exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    appendToChat("❌ Error: " + throwable.getMessage() + "\n\n");
                    resetInputControls();
                });
                return null;
            });
    }

    private void resetInputControls() {
        sendButton.setEnabled(true);
        inputField.setEnabled(true);
        inputField.requestFocus();
        statusLabel.setText("✅ Ready");
        statusLabel.setForeground(Color.GREEN);
    }

    private void appendToChat(String text) {
        chatOutput.append(text);
        chatOutput.setCaretPosition(chatOutput.getDocument().getLength());
    }

    private void showNetBeansNotification() {
        SwingUtilities.invokeLater(() -> {
            try {
                org.openide.awt.StatusDisplayer.getDefault().setStatusText("🔧 NetBeans operation completed by AI!");
                JOptionPane.showMessageDialog(this,
                    "✅ AI successfully executed NetBeans operation!\n\nCheck your project files for changes.",
                    "NetBeans Operation Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                LOG.info("NetBeans operation completed (notification fallback)");
            }
        });
    }

    public void testConnection() {
        if (lmStudio != null) {
            appendToChat("🔄 Testing LM Studio connection...\n");
            lmStudio.testConnection().thenAccept(connected -> SwingUtilities.invokeLater(() -> {
                if (connected) appendToChat("✅ LM Studio connection successful!\n\n");
                else appendToChat("❌ LM Studio connection failed. Make sure LM Studio is running.\n\n");
            }));
        }
    }

    private void openConfiguration() {
        try {
            ConfigurationPanel configPanel = new ConfigurationPanel((Frame) SwingUtilities.getWindowAncestor(this));
            configPanel.setVisible(true);
            if (configPanel.getSelectedProvider() != null) {
                String provider = configPanel.getSelectedProvider();
                String url = configPanel.getUrl();
                String model = configPanel.getModel();
                statusLabel.setText("🔄 Reconnecting with new configuration...");
                statusLabel.setForeground(Color.ORANGE);
                if ("ollama".equals(provider)) {
                    lmStudio = null;
                } else {
                    lmStudio = new LMStudioTextIntegration(url, model);
                    lmStudio.testConnection().thenAccept(connected -> SwingUtilities.invokeLater(() -> {
                        if (connected) {
                            statusLabel.setText("✅ LM Studio Connected");
                            statusLabel.setForeground(Color.GREEN);
                            appendToChat("🔧 Configuration updated and connected successfully!\n\n");
                        } else {
                            statusLabel.setText("❌ Connection Failed");
                            statusLabel.setForeground(Color.RED);
                            appendToChat("❌ Failed to connect with new configuration.\n\n");
                        }
                    }));
                }
            }
        } catch (Exception e) {
            LOG.severe("Error opening configuration: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error opening configuration: " + e.getMessage(),
                "Configuration Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showAvailableFunctions() {
        if (lmStudio != null) {
            var functions = NetBeansFunctionDefinitions.getAllFunctions();
            appendToChat("🔧 Available NetBeans Functions:\n");
            for (var function : functions) {
                appendToChat("• " + function.getName() + ": " + function.getDescription() + "\n");
            }
            appendToChat("\n");
        }
    }

    public void createExampleClass() { inputField.setText("Crie uma classe UserService com métodos CRUD"); onSendMessage(null); }
    public void readMainFile() { inputField.setText("Leia o arquivo src/main/java/Main.java"); onSendMessage(null); }
    public void getProjectInfo() { inputField.setText("Mostre informações do projeto"); onSendMessage(null); }
    public void listActiveWindows() { inputField.setText("Liste as janelas ativas do NetBeans"); onSendMessage(null); }
}
