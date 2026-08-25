/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.bajinho.continuebeans.ui;

import com.bajinho.continuebeans.mcp.McpTool;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Dialog for adding/editing MCP tools.
 * Provides a modern interface for configuring MCP tool connections.
 * 
 * @author Continue Beans Team
 */
public class McpToolDialog extends JDialog {
    
    private JTextField nameField;
    private JTextField descriptionField;
    private JTextField providerField;
    private JTextField endpointField;
    private JTextArea schemaArea;
    private JCheckBox enabledCheckBox;
    private JButton saveButton;
    private JButton cancelButton;
    
    private McpTool tool;
    private boolean saved = false;
    
    // Styling constants
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color SECONDARY_COLOR = new Color(107, 114, 128);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color ERROR_COLOR = new Color(239, 68, 68);
    
    public McpToolDialog(Frame parent) {
        super(parent, "Add MCP Tool", true);
        initializeUI();
        setSize(500, 450);
        setLocationRelativeTo(parent);
    }
    
    public McpToolDialog(Frame parent, McpTool existingTool) {
        super(parent, "Edit MCP Tool", true);
        this.tool = existingTool;
        initializeUI();
        loadExistingTool();
        setSize(500, 450);
        setLocationRelativeTo(parent);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        
        // Main form panel
        JPanel formPanel = createFormPanel();
        add(formPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 255, 255));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("MCP Tool Configuration");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(30, 41, 59));
        panel.add(titleLabel, gbc);
        
        // Tool Name
        gbc.gridy = 1; gbc.gridwidth = 1;
        JLabel nameLabel = new JLabel("Tool Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(nameLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        nameField = new JTextField();
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameField.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(nameField, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel descLabel = new JLabel("Description:");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(descLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        descriptionField = new JTextField();
        descriptionField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descriptionField.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        panel.add(descriptionField, gbc);
        
        // Provider
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        JLabel providerLabel = new JLabel("Provider:");
        providerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(providerLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        providerField = new JTextField();
        providerField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        providerField.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        providerField.setToolTipText("e.g., netbeans, filesystem, api, custom");
        panel.add(providerField, gbc);
        
        // Endpoint
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        JLabel endpointLabel = new JLabel("Endpoint:");
        endpointLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(endpointLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        endpointField = new JTextField();
        endpointField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        endpointField.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        endpointField.setToolTipText("e.g., http://localhost:8080/api/tools");
        panel.add(endpointField, gbc);
        
        // Schema
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        JLabel schemaLabel = new JLabel("Schema (JSON):");
        schemaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        panel.add(schemaLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        schemaArea = new JTextArea(5, 30);
        schemaArea.setFont(new Font("JetBrains Mono", Font.PLAIN, 11));
        schemaArea.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        schemaArea.setToolTipText("JSON schema for tool parameters");
        JScrollPane schemaScroll = new JScrollPane(schemaArea);
        panel.add(schemaScroll, gbc);
        
        // Enabled checkbox
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.weightx = 0; gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        enabledCheckBox = new JCheckBox("Enable this tool");
        enabledCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        enabledCheckBox.setSelected(true);
        panel.add(enabledCheckBox, gbc);
        
        return panel;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(new Color(248, 250, 252));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        saveButton = createModernButton("Save", PRIMARY_COLOR);
        saveButton.addActionListener(this::saveTool);
        
        cancelButton = createModernButton("Cancel", SECONDARY_COLOR);
        cancelButton.addActionListener(this::cancelDialog);
        
        panel.add(saveButton);
        panel.add(cancelButton);
        
        return panel;
    }
    
    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private void loadExistingTool() {
        if (tool != null) {
            nameField.setText(tool.getName());
            descriptionField.setText(tool.getDescription());
            providerField.setText(tool.getProvider());
            endpointField.setText(tool.getEndpoint());
            schemaArea.setText(tool.getSchema());
            enabledCheckBox.setSelected(tool.isEnabled());
        }
    }
    
    private void saveTool(ActionEvent e) {
        if (!validateInput()) {
            return;
        }
        
        String name = nameField.getText().trim();
        String description = descriptionField.getText().trim();
        String provider = providerField.getText().trim();
        String endpoint = endpointField.getText().trim();
        String schema = schemaArea.getText().trim();
        boolean enabled = enabledCheckBox.isSelected();
        
        tool = new McpTool(name, description, provider, enabled, endpoint, schema);
        saved = true;
        dispose();
    }
    
    private void cancelDialog(ActionEvent e) {
        saved = false;
        dispose();
    }
    
    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Tool name is required.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (descriptionField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Description is required.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (providerField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Provider is required.", 
                "Validation Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    public McpTool getTool() {
        return saved ? tool : null;
    }
    
    public boolean wasSaved() {
        return saved;
    }
}