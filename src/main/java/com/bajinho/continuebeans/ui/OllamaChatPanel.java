package com.bajinho.continuebeans.ui;

import com.bajinho.continuebeans.ai.OllamaIntegration;
import com.bajinho.continuebeans.ai.NetBeansFunctionDefinitions;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * ChatPanel with Ollama integration and NetBeans control.
 * Alternative implementation using Ollama instead of LM Studio.
 * 
 * @author Continue Beans Team
 */
public class OllamaChatPanel extends JPanel {
    
    private static final Logger LOG = Logger.getLogger(OllamaChatPanel.class.getName());
    
    private OllamaIntegration ollama;
    private JTextArea chatOutput;
    private JTextField inputField;
    private JButton sendButton;
    private JLabel statusLabel;
    
    public OllamaChatPanel() {
        initializeOllama();
        createUI();
    }
    
    /**
     * Initialize Ollama connection.
     */
    private void initializeOllama() {
        try {
            // Default Ollama URL and model
            String baseUrl = "http://127.0.0.1:11434";
            String modelName = "qwen2.5:7b";
            
            // Create Ollama integration
            ollama = new OllamaIntegration(baseUrl, modelName);
            
            // Test connection
            ollama.testConnection().thenAccept(connected -> {
                SwingUtilities.invokeLater(() -> {
                    if (connected) {
                        statusLabel.setText("✅ Ollama Connected");
                        statusLabel.setForeground(Color.GREEN);
                        LOG.info("Ollama connection successful");
                    } else {
                        statusLabel.setText("❌ Ollama Not Connected - Check if Ollama is running on port 11434");
                        statusLabel.setForeground(Color.RED);
                        LOG.warning("Ollama connection failed - make sure Ollama is running on http://127.0.0.1:11434");
                    }
                });
            });
            
            LOG.info("Ollama integration ready");
            
        } catch (Exception e) {
            LOG.severe("Failed to initialize Ollama: " + e.getMessage());
            statusLabel.setText("❌ Connection Error");
            statusLabel.setForeground(Color.RED);
        }
    }
    
    /**
     * Create the chat UI.
     */
    private void createUI() {
        setLayout(new BorderLayout());
        
        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("🔌 Connecting to Ollama...");
        statusLabel.setForeground(Color.ORANGE);
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.NORTH);
        
        // Chat output
        chatOutput = new JTextArea();
        chatOutput.setEditable(false);
        chatOutput.setFont(new Font("Monospaced", Font.PLAIN, 12));
        chatOutput.setBackground(new Color(240, 240, 240));
        JScrollPane scrollPane = new JScrollPane(chatOutput);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        sendButton = new JButton("Send");
        sendButton.setFont(new Font("Arial", Font.BOLD, 12));
        sendButton.setBackground(new Color(70, 130, 180));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);
        
        // Event handlers
        sendButton.addActionListener(this::sendMessage);
        inputField.addActionListener(this::sendMessage);
        
        // Welcome message
        appendToChat("🤖 Continue Beans with Ollama\n");
        appendToChat("✨ AI-powered NetBeans development assistant\n");
        appendToChat("🔧 Using Ollama for reliable function calling\n");
        appendToChat("📝 Type your message and press Enter or click Send\n\n");
    }
    
    /**
     * Send message to Ollama.
     */
    private void sendMessage(ActionEvent e) {
        String message = inputField.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        
        // Add user message to chat
        appendToChat("👤 You: " + message + "\n");
        inputField.setText("");
        sendButton.setEnabled(false);
        inputField.setEnabled(false);
        
        // Process with Ollama
        processMessage(message);
    }
    
    /**
     * Process message with Ollama.
     */
    private void processMessage(String message) {
        if (ollama == null) {
            appendToChat("❌ Ollama not connected. Please start Ollama first.\n\n");
            resetInputControls();
            return;
        }
        
        ollama.processRequest(message)
            .thenAccept(response -> {
                SwingUtilities.invokeLater(() -> {
                    appendToChat("🤖 AI: " + response + "\n\n");
                    resetInputControls();
                });
            })
            .exceptionally(throwable -> {
                SwingUtilities.invokeLater(() -> {
                    appendToChat("❌ Error: " + throwable.getMessage() + "\n\n");
                    resetInputControls();
                });
                return null;
            });
    }
    
    /**
     * Append text to chat output.
     */
    private void appendToChat(String text) {
        chatOutput.append(text);
        chatOutput.setCaretPosition(chatOutput.getDocument().getLength());
    }
    
    /**
     * Reset input controls.
     */
    private void resetInputControls() {
        sendButton.setEnabled(true);
        inputField.setEnabled(true);
        inputField.requestFocus();
    }
    
    /**
     * Show available functions.
     */
    public void showAvailableFunctions() {
        if (ollama != null) {
            var functions = NetBeansFunctionDefinitions.getAllFunctions();
            appendToChat("🔧 Available NetBeans Functions:\n");
            for (var function : functions) {
                appendToChat("• " + function.getName() + ": " + function.getDescription() + "\n");
            }
            appendToChat("\n");
        }
    }
    
    /**
     * Example usage methods for easy testing
     */
    public void testHelloWorld() {
        inputField.setText("crie hello world em python");
        sendMessage(null);
    }
    
    public void testCreateClass() {
        inputField.setText("crie uma classe UserService");
        sendMessage(null);
    }
    
    public void testReadFile() {
        inputField.setText("leia o arquivo README.md");
        sendMessage(null);
    }
    
    public void testProjectInfo() {
        inputField.setText("mostre informações do projeto");
        sendMessage(null);
    }
}
