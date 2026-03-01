package com.bajinho.continuebeans.ui;

import com.bajinho.continuebeans.ai.LMStudioTextIntegration;
import com.bajinho.continuebeans.ai.OllamaIntegration;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * Configuration Panel for Continue Beans.
 * Allows users to configure server settings and model selection.
 * 
 * @author Continue Beans Team
 */
public class ConfigurationPanel extends JDialog {
    
    private static final Logger LOG = Logger.getLogger(ConfigurationPanel.class.getName());
    
    // UI Components
    private JTextField lmStudioUrlField;
    private JTextField lmStudioModelField;
    private JTextField ollamaUrlField;
    private JTextField ollamaModelField;
    private JRadioButton lmStudioRadio;
    private JRadioButton ollamaRadio;
    private JButton testConnectionButton;
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    
    // Configuration
    private String selectedProvider = "ollama";
    private boolean configurationChanged = false;
    
    public ConfigurationPanel(Frame parent) {
        super(parent, "Continue Beans Configuration", true);
        initializeUI();
        loadCurrentConfiguration();
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(parent);
    }
    
    /**
     * Initialize the configuration UI.
     */
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Provider selection
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel providerLabel = new JLabel("AI Provider:");
        providerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(providerLabel, gbc);
        
        gbc.gridy = 1; gbc.gridwidth = 1;
        lmStudioRadio = new JRadioButton("LM Studio");
        lmStudioRadio.addActionListener(e -> selectProvider("lmstudio"));
        mainPanel.add(lmStudioRadio, gbc);
        
        gbc.gridx = 1;
        ollamaRadio = new JRadioButton("Ollama");
        ollamaRadio.addActionListener(e -> selectProvider("ollama"));
        mainPanel.add(ollamaRadio, gbc);
        
        // Radio button group
        ButtonGroup providerGroup = new ButtonGroup();
        providerGroup.add(lmStudioRadio);
        providerGroup.add(ollamaRadio);
        
        // LM Studio Configuration
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JLabel lmStudioLabel = new JLabel("LM Studio Configuration:");
        lmStudioLabel.setFont(new Font("Arial", Font.BOLD, 12));
        lmStudioLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        mainPanel.add(lmStudioLabel, gbc);
        
        gbc.gridy = 3; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Server URL:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        lmStudioUrlField = new JTextField("http://127.0.0.1:1234");
        lmStudioUrlField.setToolTipText("LM Studio server URL (e.g., http://127.0.0.1:1234)");
        mainPanel.add(lmStudioUrlField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        mainPanel.add(new JLabel("Model:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        lmStudioModelField = new JTextField("qwen3-4b-function-calling-finetuned");
        lmStudioModelField.setToolTipText("Model name (e.g., qwen3-4b-function-calling-finetuned)");
        mainPanel.add(lmStudioModelField, gbc);
        
        // Ollama Configuration
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        JLabel ollamaLabel = new JLabel("Ollama Configuration:");
        ollamaLabel.setFont(new Font("Arial", Font.BOLD, 12));
        ollamaLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        mainPanel.add(ollamaLabel, gbc);
        
        gbc.gridy = 6; gbc.gridwidth = 1;
        mainPanel.add(new JLabel("Server URL:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        ollamaUrlField = new JTextField("http://127.0.0.1:11434");
        ollamaUrlField.setToolTipText("Ollama server URL (e.g., http://127.0.0.1:11434)");
        mainPanel.add(ollamaUrlField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 7; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        mainPanel.add(new JLabel("Model:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        ollamaModelField = new JTextField("qwen2.5:7b");
        ollamaModelField.setToolTipText("Model name (e.g., qwen2.5:7b)");
        mainPanel.add(ollamaModelField, gbc);
        
        // Test connection button
        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        testConnectionButton = new JButton("Test Connection");
        testConnectionButton.setFont(new Font("Arial", Font.BOLD, 12));
        testConnectionButton.setBackground(new Color(70, 130, 180));
        testConnectionButton.setForeground(Color.WHITE);
        testConnectionButton.setFocusPainted(false);
        testConnectionButton.addActionListener(this::testConnection);
        mainPanel.add(testConnectionButton, gbc);
        
        // Status label
        gbc.gridy = 9;
        statusLabel = new JLabel("Ready to test connection");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setForeground(Color.GRAY);
        mainPanel.add(statusLabel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        saveButton = new JButton("Save");
        saveButton.setFont(new Font("Arial", Font.BOLD, 12));
        saveButton.setBackground(new Color(60, 179, 113));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(this::saveConfiguration);
        
        cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 12));
        cancelButton.setBackground(new Color(220, 53, 69));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(this::cancelConfiguration);
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Enable/disable fields based on selected provider
        updateFieldStates();
    }
    
    /**
     * Select provider and update UI.
     */
    private void selectProvider(String provider) {
        selectedProvider = provider;
        updateFieldStates();
        configurationChanged = true;
    }
    
    /**
     * Update field states based on selected provider.
     */
    private void updateFieldStates() {
        boolean lmStudioEnabled = "lmstudio".equals(selectedProvider);
        boolean ollamaEnabled = "ollama".equals(selectedProvider);
        
        lmStudioUrlField.setEnabled(lmStudioEnabled);
        lmStudioModelField.setEnabled(lmStudioEnabled);
        ollamaUrlField.setEnabled(ollamaEnabled);
        ollamaModelField.setEnabled(ollamaEnabled);
        
        if (lmStudioEnabled) {
            lmStudioRadio.setSelected(true);
        } else {
            ollamaRadio.setSelected(true);
        }
    }
    
    /**
     * Load current configuration.
     */
    private void loadCurrentConfiguration() {
        // Load from system properties or defaults
        String provider = System.getProperty("continue.beans.provider", "ollama");
        String lmStudioUrl = System.getProperty("continue.beans.lmstudio.url", "http://127.0.0.1:1234");
        String lmStudioModel = System.getProperty("continue.beans.lmstudio.model", "qwen3-4b-function-calling-finetuned");
        String ollamaUrl = System.getProperty("continue.beans.ollama.url", "http://127.0.0.1:11434");
        String ollamaModel = System.getProperty("continue.beans.ollama.model", "qwen2.5:7b");
        
        // Set values
        selectProvider(provider);
        lmStudioUrlField.setText(lmStudioUrl);
        lmStudioModelField.setText(lmStudioModel);
        ollamaUrlField.setText(ollamaUrl);
        ollamaModelField.setText(ollamaModel);
        
        configurationChanged = false;
    }
    
    /**
     * Test connection to selected provider.
     */
    private void testConnection(ActionEvent e) {
        statusLabel.setText("Testing connection...");
        statusLabel.setForeground(Color.ORANGE);
        testConnectionButton.setEnabled(false);
        
        CompletableFuture<Boolean> testFuture;
        
        if ("lmstudio".equals(selectedProvider)) {
            String url = lmStudioUrlField.getText().trim();
            String model = lmStudioModelField.getText().trim();
            LMStudioTextIntegration integration = new LMStudioTextIntegration(url, model);
            testFuture = integration.testConnection();
        } else {
            String url = ollamaUrlField.getText().trim();
            String model = ollamaModelField.getText().trim();
            OllamaIntegration integration = new OllamaIntegration(url, model);
            testFuture = integration.testConnection();
        }
        
        testFuture.thenAccept(success -> {
            SwingUtilities.invokeLater(() -> {
                if (success) {
                    statusLabel.setText("✅ Connection successful!");
                    statusLabel.setForeground(Color.GREEN);
                } else {
                    statusLabel.setText("❌ Connection failed. Check server and model.");
                    statusLabel.setForeground(Color.RED);
                }
                testConnectionButton.setEnabled(true);
            });
        }).exceptionally(throwable -> {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("❌ Error: " + throwable.getMessage());
                statusLabel.setForeground(Color.RED);
                testConnectionButton.setEnabled(true);
            });
            return null;
        });
    }
    
    /**
     * Save configuration.
     */
    private void saveConfiguration(ActionEvent e) {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }
        
        // Save to system properties
        System.setProperty("continue.beans.provider", selectedProvider);
        System.setProperty("continue.beans.lmstudio.url", lmStudioUrlField.getText().trim());
        System.setProperty("continue.beans.lmstudio.model", lmStudioModelField.getText().trim());
        System.setProperty("continue.beans.ollama.url", ollamaUrlField.getText().trim());
        System.setProperty("continue.beans.ollama.model", ollamaModelField.getText().trim());
        
        configurationChanged = false;
        dispose();
        
        // Show success message
        JOptionPane.showMessageDialog(this, 
            "Configuration saved successfully!\n\n" +
            "Provider: " + selectedProvider + "\n" +
            "URL: " + getUrlForProvider() + "\n" +
            "Model: " + getModelForProvider(),
            "Configuration Saved", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Cancel configuration.
     */
    private void cancelConfiguration(ActionEvent e) {
        if (configurationChanged) {
            int result = JOptionPane.showConfirmDialog(this, 
                "You have unsaved changes. Are you sure you want to cancel?", 
                "Unsaved Changes", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);
            
            if (result != JOptionPane.YES_OPTION) {
                return;
            }
        }
        dispose();
    }
    
    /**
     * Validate input fields.
     */
    private boolean validateInputs() {
        String url = getUrlForProvider();
        String model = getModelForProvider();
        
        if (url == null || url.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid server URL.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (model == null || model.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter a valid model name.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    /**
     * Get URL for selected provider.
     */
    private String getUrlForProvider() {
        if ("lmstudio".equals(selectedProvider)) {
            return lmStudioUrlField.getText().trim();
        } else {
            return ollamaUrlField.getText().trim();
        }
    }
    
    /**
     * Get model for selected provider.
     */
    private String getModelForProvider() {
        if ("lmstudio".equals(selectedProvider)) {
            return lmStudioModelField.getText().trim();
        } else {
            return ollamaModelField.getText().trim();
        }
    }
    
    /**
     * Get selected provider.
     */
    public String getSelectedProvider() {
        return selectedProvider;
    }
    
    /**
     * Get URL for selected provider.
     */
    public String getUrl() {
        return getUrlForProvider();
    }
    
    /**
     * Get model for selected provider.
     */
    public String getModel() {
        return getModelForProvider();
    }
}
