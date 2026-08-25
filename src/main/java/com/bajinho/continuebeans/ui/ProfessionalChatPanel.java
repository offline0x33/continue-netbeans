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

import com.bajinho.continuebeans.ai.LMStudioTextIntegration;
import com.bajinho.continuebeans.ai.NetBeansFunctionDefinitions;
import com.bajinho.continuebeans.mcp.McpToolManager;
import com.bajinho.continuebeans.mcp.McpTool;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.logging.Logger;

/**
 * Professional Chat Panel with modern UI design and MCP integration.
 * 
 * <p>This component provides an enterprise-grade chat interface for AI interactions
 * with comprehensive MCP (Model Context Protocol) tools management. It follows
 * Apache NetBeans UI standards and provides a modern, professional user experience.</p>
 * 
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Modern tabbed interface with Chat and MCP Tools tabs</li>
 *   <li>Professional styling following NetBeans design guidelines</li>
 *   <li>Real-time connection status monitoring</li>
 *   <li>Comprehensive MCP tools management</li>
 *   <li>Integration with NetBeans function definitions</li>
 *   <li>Persistent configuration storage</li>
 * </ul>
 * 
 * <h3>Architecture:</h3>
 * <p>The panel is divided into two main tabs:</p>
 * <ul>
 *   <li><b>Chat Tab:</b> Provides the main AI interaction interface with message
 *       history, input handling, and real-time streaming responses</li>
 *   <li><b>MCP Tools Tab:</b> Offers comprehensive tools management including
 *       adding, editing, enabling/disabling, and configuring MCP tools</li>
 * </ul>
 * 
 * <h3>Usage:</h3>
 * <pre>{@code
 * ProfessionalChatPanel panel = new ProfessionalChatPanel();
 * parentContainer.add(panel, BorderLayout.CENTER);
 * }</pre>
 * 
 * @author Continue Beans Team
 * @version 1.0
 * @see McpToolManager
 * @see McpTool
 * @see LMStudioTextIntegration
 */
public class ProfessionalChatPanel extends JPanel {
    
    private static final Logger LOG = Logger.getLogger(ProfessionalChatPanel.class.getName());
    
    // UI Components
    private JTextArea chatOutput;
    private JTextField inputField;
    private JButton sendButton;
    private JButton settingsButton;
    private JButton toolsButton;
    private JLabel statusLabel;
    private JTabbedPane tabbedPane;
    private JList<McpTool> toolsList;
    private DefaultListModel<McpTool> toolsListModel;
    
    // AI Integration
    private LMStudioTextIntegration lmStudio;
    private McpToolManager mcpToolManager;
    
    // Styling constants
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color SECONDARY_COLOR = new Color(107, 114, 128);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color ERROR_COLOR = new Color(239, 68, 68);
    private static final Color BACKGROUND_COLOR = new Color(248, 250, 252);
    private static final Color PANEL_COLOR = new Color(255, 255, 255);
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    
    public ProfessionalChatPanel() {
        initializeComponents();
        createModernUI();
        initializeAI();
        initializeMCP();
    }
    
    private void initializeComponents() {
        mcpToolManager = new McpToolManager();
        toolsListModel = new DefaultListModel<>();
        toolsList = new JList<>(toolsListModel);
    }
    
    private void createModernUI() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        
        // Create tabbed interface
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabbedPane.setBackground(PANEL_COLOR);
        
        // Chat tab
        JPanel chatTab = createChatTab();
        tabbedPane.addTab("💬 Chat", chatTab);
        
        // Tools tab
        JPanel toolsTab = createToolsTab();
        tabbedPane.addTab("🔧 MCP Tools", toolsTab);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Status bar
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);
    }
    
    private JPanel createChatTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_COLOR);
        
        // Header with toolbar
        JPanel header = createChatHeader();
        panel.add(header, BorderLayout.NORTH);
        
        // Chat output area with modern styling
        chatOutput = new JTextArea();
        chatOutput.setEditable(false);
        chatOutput.setFont(new Font("JetBrains Mono", Font.PLAIN, 13));
        chatOutput.setBackground(new Color(248, 250, 252));
        chatOutput.setForeground(new Color(30, 41, 59));
        chatOutput.setLineWrap(true);
        chatOutput.setWrapStyleWord(true);
        chatOutput.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JScrollPane scrollPane = new JScrollPane(chatOutput);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Input panel with modern styling
        JPanel inputPanel = createInputPanel();
        panel.add(inputPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createChatHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // Title
        JLabel titleLabel = new JLabel("Continue Beans AI Assistant");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(30, 41, 59));
        header.add(titleLabel, BorderLayout.WEST);
        
        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        toolbar.setBackground(PANEL_COLOR);
        
        // Tools button
        toolsButton = createModernButton("🔧 Tools", SECONDARY_COLOR);
        toolsButton.addActionListener(e -> tabbedPane.setSelectedIndex(1));
        toolbar.add(toolsButton);
        
        // Settings button
        settingsButton = createModernButton("⚙️ Settings", SECONDARY_COLOR);
        settingsButton.addActionListener(this::openSettings);
        toolbar.add(settingsButton);
        
        header.add(toolbar, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Input field with modern styling
        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBackground(new Color(255, 255, 255));
        inputField.setForeground(new Color(30, 41, 59));
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        inputField.setToolTipText("Type your message here...");
        
        // Send button with modern styling
        sendButton = createModernButton("Send", PRIMARY_COLOR);
        sendButton.setPreferredSize(new Dimension(100, 40));
        sendButton.addActionListener(this::onSendMessage);
        
        panel.add(inputField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        
        // Add Enter key listener
        inputField.addActionListener(this::onSendMessage);
        
        return panel;
    }
    
    private JPanel createToolsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_COLOR);
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        
        JLabel titleLabel = new JLabel("MCP Tools Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(30, 41, 59));
        header.add(titleLabel, BorderLayout.WEST);
        
        // Add tool button
        JButton addToolButton = createModernButton("+ Add Tool", PRIMARY_COLOR);
        addToolButton.addActionListener(this::addMcpTool);
        header.add(addToolButton, BorderLayout.EAST);
        
        panel.add(header, BorderLayout.NORTH);
        
        // Tools list
        JPanel toolsListPanel = new JPanel(new BorderLayout());
        toolsListPanel.setBackground(PANEL_COLOR);
        toolsListPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        
        toolsList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        toolsList.setBackground(new Color(255, 255, 255));
        toolsList.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        toolsList.setCellRenderer(new McpToolListCellRenderer());
        
        JScrollPane scrollPane = new JScrollPane(toolsList);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        toolsListPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Tool details panel
        JPanel toolDetailsPanel = createToolDetailsPanel();
        toolsListPanel.add(toolDetailsPanel, BorderLayout.SOUTH);
        
        panel.add(toolsListPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createToolDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JLabel detailsLabel = new JLabel("Select a tool to view details");
        detailsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        detailsLabel.setForeground(SECONDARY_COLOR);
        panel.add(detailsLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(241, 245, 249));
        statusBar.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        statusLabel = new JLabel("🔄 Initializing...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(SECONDARY_COLOR);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        
        statusBar.add(statusLabel, BorderLayout.WEST);
        
        // Connection indicator
        JLabel connectionLabel = new JLabel("●");
        connectionLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        connectionLabel.setForeground(ERROR_COLOR);
        connectionLabel.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        statusBar.add(connectionLabel, BorderLayout.EAST);
        
        return statusBar;
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
    
    private void initializeAI() {
        try {
            String baseUrl = "http://127.0.0.1:1234";
            String modelName = "qwen3-4b-function-calling-finetuned";
            
            lmStudio = new LMStudioTextIntegration(baseUrl, modelName);
            
            lmStudio.testConnection().thenAccept(connected -> {
                SwingUtilities.invokeLater(() -> {
                    if (connected) {
                        statusLabel.setText("✅ Connected to LM Studio");
                        statusLabel.setForeground(SUCCESS_COLOR);
                    } else {
                        statusLabel.setText("❌ LM Studio not connected");
                        statusLabel.setForeground(ERROR_COLOR);
                    }
                });
            });
            
        } catch (Exception e) {
            LOG.severe("Failed to initialize AI: " + e.getMessage());
            statusLabel.setText("❌ AI initialization failed");
            statusLabel.setForeground(ERROR_COLOR);
        }
    }
    
    private void initializeMCP() {
        // Load existing MCP tools
        List<McpTool> existingTools = mcpToolManager.loadTools();
        for (McpTool tool : existingTools) {
            toolsListModel.addElement(tool);
        }
        
        // Add NetBeans function definitions as MCP tools
        addNetBeansFunctionsAsMcpTools();
    }
    
    private void addNetBeansFunctionsAsMcpTools() {
        var netBeansFunctions = NetBeansFunctionDefinitions.getAllFunctions();
        for (var function : netBeansFunctions) {
            McpTool tool = new McpTool(
                function.getName(),
                function.getDescription(),
                "netbeans",
                true
            );
            if (!toolsListModel.contains(tool)) {
                toolsListModel.addElement(tool);
            }
        }
    }
    
    private void onSendMessage(ActionEvent e) {
        String message = inputField.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        
        inputField.setText("");
        appendToChat("👤 You: " + message + "\n");
        
        sendButton.setEnabled(false);
        inputField.setEnabled(false);
        statusLabel.setText("🤔 Processing...");
        
        processMessage(message);
    }
    
    private void processMessage(String message) {
        if (lmStudio == null) {
            appendToChat("❌ AI not connected. Please check your settings.\n\n");
            resetInputControls();
            return;
        }
        
        lmStudio.processRequest(message)
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
    
    private void resetInputControls() {
        sendButton.setEnabled(true);
        inputField.setEnabled(true);
        inputField.requestFocus();
        statusLabel.setText("✅ Ready");
        statusLabel.setForeground(SUCCESS_COLOR);
    }
    
    private void appendToChat(String text) {
        chatOutput.append(text);
        chatOutput.setCaretPosition(chatOutput.getDocument().getLength());
    }
    
    private void openSettings(ActionEvent e) {
        JOptionPane.showMessageDialog(this, 
            "Settings panel coming soon!\n\nFor now, use:\nTools → Options → Miscellaneous → Continue Beans", 
            "Settings", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void addMcpTool(ActionEvent e) {
        McpToolDialog dialog = new McpToolDialog((Frame) SwingUtilities.getWindowAncestor(this));
        dialog.setVisible(true);
        
        if (dialog.getTool() != null) {
            McpTool tool = dialog.getTool();
            toolsListModel.addElement(tool);
            mcpToolManager.saveTool(tool);
            appendToChat("🔧 MCP Tool added: " + tool.getName() + "\n\n");
        }
    }
    
    // Custom cell renderer for MCP tools list
    private static class McpToolListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof McpTool) {
                McpTool tool = (McpTool) value;
                setText(tool.getName() + " - " + tool.getDescription());
                setToolTipText(tool.getProvider() + " | " + (tool.isEnabled() ? "Enabled" : "Disabled"));
            }
            
            return this;
        }
    }
}