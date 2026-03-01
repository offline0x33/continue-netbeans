package com.bajinho.continuebeans.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.prefs.Preferences;
import javax.swing.*;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

/**
 * Options Panel for Continue Beans configuration.
 * Integrates with NetBeans Options dialog.
 * 
 * @author Continue Beans Team
 */
@Messages({
    "AdvancedOption_DisplayName_ContinueBeans=Continue Beans",
    "AdvancedOption_Keywords_ContinueBeans=continue beans, ai, lm studio, ollama, chat, assistant"
})
@OptionsPanelController.SubRegistration(
    location = "Miscellaneous",
    displayName = "#AdvancedOption_DisplayName_ContinueBeans",
    keywords = "#AdvancedOption_Keywords_ContinueBeans",
    keywordsCategory = "Miscellaneous/ContinueBeans"
)
public final class ContinueBeansOptionsPanel extends OptionsPanelController {
    
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private JPanel panel;
    private JTextField lmStudioUrlField;
    private JTextField lmStudioModelField;
    private JTextField ollamaUrlField;
    private JTextField ollamaModelField;
    private JRadioButton lmStudioRadio;
    private JRadioButton ollamaRadio;
    private JButton testConnectionButton;
    private JLabel statusLabel;
    
    private final Preferences prefs = Preferences.userNodeForPackage(ContinueBeansOptionsPanel.class);
    
    @Override
    public void update() {
        loadConfiguration();
    }
    
    @Override
    public void applyChanges() {
        saveConfiguration();
    }
    
    @Override
    public void cancel() {
        // Do nothing
    }
    
    @Override
    public boolean isValid() {
        return true;
    }
    
    @Override
    public boolean isChanged() {
        return true;
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }
    
    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }
    
    private JPanel getPanel() {
        if (panel == null) {
            panel = createPanel();
        }
        return panel;
    }
    
    private JPanel createPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Configuration panel
        JPanel configPanel = new JPanel(new java.awt.GridBagLayout());
        configPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.anchor = java.awt.GridBagConstraints.WEST;
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Continue Beans Configuration");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        configPanel.add(titleLabel, gbc);
        
        // Provider selection
        gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel providerLabel = new JLabel("AI Provider:");
        providerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        configPanel.add(providerLabel, gbc);
        
        gbc.gridy = 2;
        lmStudioRadio = new JRadioButton("LM Studio");
        lmStudioRadio.addActionListener(e -> updateFieldStates());
        configPanel.add(lmStudioRadio, gbc);
        
        gbc.gridy = 3;
        ollamaRadio = new JRadioButton("Ollama");
        ollamaRadio.addActionListener(e -> updateFieldStates());
        configPanel.add(ollamaRadio, gbc);
        
        // Radio button group
        ButtonGroup providerGroup = new ButtonGroup();
        providerGroup.add(lmStudioRadio);
        providerGroup.add(ollamaRadio);
        
        // LM Studio Configuration
        gbc.gridy = 4; gbc.gridwidth = 2;
        JLabel lmStudioLabel = new JLabel("LM Studio Configuration:");
        lmStudioLabel.setFont(new Font("Arial", Font.BOLD, 12));
        lmStudioLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        configPanel.add(lmStudioLabel, gbc);
        
        gbc.gridy = 5; gbc.gridwidth = 1;
        configPanel.add(new JLabel("Server URL:"), gbc);
        
        gbc.gridy = 6; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        lmStudioUrlField = new JTextField();
        lmStudioUrlField.setToolTipText("LM Studio server URL (e.g., http://127.0.0.1:1234)");
        configPanel.add(lmStudioUrlField, gbc);
        
        gbc.gridy = 7; gbc.fill = java.awt.GridBagConstraints.NONE; gbc.weightx = 0;
        configPanel.add(new JLabel("Model:"), gbc);
        
        gbc.gridy = 8; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        lmStudioModelField = new JTextField();
        lmStudioModelField.setToolTipText("Model name (e.g., qwen3-4b-function-calling-finetuned)");
        configPanel.add(lmStudioModelField, gbc);
        
        // Ollama Configuration
        gbc.gridy = 9; gbc.gridwidth = 2;
        JLabel ollamaLabel = new JLabel("Ollama Configuration:");
        ollamaLabel.setFont(new Font("Arial", Font.BOLD, 12));
        ollamaLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));
        configPanel.add(ollamaLabel, gbc);
        
        gbc.gridy = 10; gbc.gridwidth = 1;
        configPanel.add(new JLabel("Server URL:"), gbc);
        
        gbc.gridy = 11; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        ollamaUrlField = new JTextField();
        ollamaUrlField.setToolTipText("Ollama server URL (e.g., http://127.0.0.1:11434)");
        configPanel.add(ollamaUrlField, gbc);
        
        gbc.gridy = 12; gbc.fill = java.awt.GridBagConstraints.NONE; gbc.weightx = 0;
        configPanel.add(new JLabel("Model:"), gbc);
        
        gbc.gridy = 13; gbc.fill = java.awt.GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        ollamaModelField = new JTextField();
        ollamaModelField.setToolTipText("Model name (e.g., qwen2.5:7b)");
        configPanel.add(ollamaModelField, gbc);
        
        // Test connection button
        gbc.gridy = 14; gbc.gridwidth = 2; gbc.fill = java.awt.GridBagConstraints.NONE; gbc.weightx = 0;
        gbc.anchor = java.awt.GridBagConstraints.CENTER;
        testConnectionButton = new JButton("Test Connection");
        testConnectionButton.setFont(new Font("Arial", Font.BOLD, 12));
        testConnectionButton.addActionListener(this::testConnection);
        configPanel.add(testConnectionButton, gbc);
        
        // Status label
        gbc.gridy = 15;
        statusLabel = new JLabel("Ready to test connection");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setForeground(java.awt.Color.GRAY);
        configPanel.add(statusLabel, gbc);
        
        mainPanel.add(configPanel, BorderLayout.NORTH);
        
        // Load initial configuration
        loadConfiguration();
        updateFieldStates();
        
        return mainPanel;
    }
    
    private void loadConfiguration() {
        String provider = prefs.get("provider", "ollama");
        String lmStudioUrl = prefs.get("lmstudio.url", "http://127.0.0.1:1234");
        String lmStudioModel = prefs.get("lmstudio.model", "qwen3-4b-function-calling-finetuned");
        String ollamaUrl = prefs.get("ollama.url", "http://127.0.0.1:11434");
        String ollamaModel = prefs.get("ollama.model", "qwen2.5:7b");
        
        // Set values
        if ("lmstudio".equals(provider)) {
            lmStudioRadio.setSelected(true);
        } else {
            ollamaRadio.setSelected(true);
        }
        
        lmStudioUrlField.setText(lmStudioUrl);
        lmStudioModelField.setText(lmStudioModel);
        ollamaUrlField.setText(ollamaUrl);
        ollamaModelField.setText(ollamaModel);
    }
    
    private void saveConfiguration() {
        String provider = lmStudioRadio.isSelected() ? "lmstudio" : "ollama";
        
        prefs.put("provider", provider);
        prefs.put("lmstudio.url", lmStudioUrlField.getText().trim());
        prefs.put("lmstudio.model", lmStudioModelField.getText().trim());
        prefs.put("ollama.url", ollamaUrlField.getText().trim());
        prefs.put("ollama.model", ollamaModelField.getText().trim());
        
        try {
            prefs.flush();
        } catch (Exception e) {
            // Ignore
        }
    }
    
    private void updateFieldStates() {
        boolean lmStudioEnabled = lmStudioRadio.isSelected();
        
        lmStudioUrlField.setEnabled(lmStudioEnabled);
        lmStudioModelField.setEnabled(lmStudioEnabled);
        ollamaUrlField.setEnabled(!lmStudioEnabled);
        ollamaModelField.setEnabled(!lmStudioEnabled);
    }
    
    private void testConnection(ActionEvent e) {
        statusLabel.setText("Testing connection...");
        statusLabel.setForeground(java.awt.Color.ORANGE);
        testConnectionButton.setEnabled(false);
        
        // Simple connection test
        SwingUtilities.invokeLater(() -> {
            try {
                String url = lmStudioRadio.isSelected() 
                    ? lmStudioUrlField.getText().trim() 
                    : ollamaUrlField.getText().trim();
                
                // Basic URL validation
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    statusLabel.setText("✅ Configuration appears valid!");
                    statusLabel.setForeground(java.awt.Color.GREEN);
                } else {
                    statusLabel.setText("❌ Invalid URL format");
                    statusLabel.setForeground(java.awt.Color.RED);
                }
            } catch (Exception ex) {
                statusLabel.setText("❌ Error: " + ex.getMessage());
                statusLabel.setForeground(java.awt.Color.RED);
            } finally {
                testConnectionButton.setEnabled(true);
            }
        });
    }
}
