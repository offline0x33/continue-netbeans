package com.bajinho.continuebeans;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.options.OptionsDisplayer;
import org.openide.util.ImageUtilities;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import javax.swing.JComboBox;
import org.openide.windows.TopComponent;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import org.openide.util.NbBundle.Messages;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import org.fife.ui.rsyntaxtextarea.Theme;

@TopComponent.Description(preferredID = "ContinueTopComponent", persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false)
@ActionID(category = "Window", id = "com.bajinho.continuebeans.ContinueTopComponent")
@ActionReference(path = "Menu/Window", position = 333)
@TopComponent.OpenActionRegistration(displayName = "#CTL_ContinueTopComponentAction", preferredID = "ContinueTopComponent")
@Messages("CTL_ContinueTopComponentAction=Open Continue Beans")
public final class ContinueTopComponent extends TopComponent {

    private final LlmClient client = new LlmClient();
    private RSyntaxTextArea chatArea;
    private JTextArea inputArea;
    private JButton sendButton;
    private JButton configButton;
    private JComboBox<String> modelSelector;
    private JComboBox<String> modeSelector;
    private JButton stopButton;
    private JButton applyButton;
    private JButton insertButton;
    private CompletableFuture<String> currentTask;
    private String lastAiResponseCode;

    public ContinueTopComponent() {
        initComponents();
        setName("Continue Beans");
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("EditorPane.background"));

        // --- HEADER ---
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UIManager.getColor("Panel.background"));
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY.brighter()));
        headerPanel.setPreferredSize(new Dimension(100, 35));

        JLabel titleLabel = new JLabel("  CONTINUE");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 10));
        titleLabel.setForeground(UIManager.getColor("Label.foreground"));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // --- SELECTORS PANEL ---
        JPanel selectorsPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 5));
        selectorsPanel.setOpaque(false);

        modelSelector = new JComboBox<>(new String[] { ContinueSettings.getModel() });
        modelSelector.setEditable(true);
        modelSelector.setPreferredSize(new Dimension(150, 22));
        modelSelector.setFont(new Font("SansSerif", Font.PLAIN, 10));
        modelSelector.addActionListener(e -> {
            String selected = (String) modelSelector.getSelectedItem();
            if (selected != null && !selected.isEmpty() && !selected.equals(ContinueSettings.getModel())) {
                client.loadModel(selected);
            }
        });

        modeSelector = new JComboBox<>(new String[] { "Ask", "Code", "Planning" });
        modeSelector.setPreferredSize(new Dimension(80, 22));
        modeSelector.setFont(new Font("SansSerif", Font.PLAIN, 10));

        JButton refreshBtn = new JButton("↻");
        refreshBtn.setPreferredSize(new Dimension(24, 22));
        refreshBtn.setToolTipText("Recarregar modelos");
        refreshBtn.addActionListener(e -> refreshModels());

        selectorsPanel.add(new JLabel("Mode:"));
        selectorsPanel.add(modeSelector);
        selectorsPanel.add(Box.createHorizontalStrut(5));
        selectorsPanel.add(new JLabel("Model:"));
        selectorsPanel.add(modelSelector);
        selectorsPanel.add(refreshBtn);

        headerPanel.add(selectorsPanel, BorderLayout.CENTER);

        // Icone de Engrenagem
        ImageIcon gearIconOriginal = ImageUtilities.loadImageIcon("com/bajinho/continuebeans/gear_icon.png", false);
        if (gearIconOriginal != null) {
            Image scaledGear = gearIconOriginal.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            configButton = new JButton(new ImageIcon(scaledGear));
        } else {
            configButton = new JButton("⚙");
        }
        configButton.setToolTipText("Configurações");
        configButton.setBorderPainted(false);
        configButton.setContentAreaFilled(false);
        configButton.setFocusPainted(false);
        configButton.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        configButton.addActionListener(
                e -> OptionsDisplayer.getDefault().open("Miscellaneous/com.bajinho.continuebeans.Options"));

        // Botão Stop
        ImageIcon stopIconOriginal = ImageUtilities.loadImageIcon("com/bajinho/continuebeans/stop_icon.png", false);
        if (stopIconOriginal != null) {
            Image scaledStop = stopIconOriginal.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
            stopButton = new JButton(new ImageIcon(scaledStop));
        } else {
            stopButton = new JButton("⬛");
        }
        stopButton.setToolTipText("Parar geração");
        stopButton.setBorderPainted(false);
        stopButton.setContentAreaFilled(false);
        stopButton.setEnabled(false);
        stopButton.addActionListener(e -> {
            if (currentTask != null && !currentTask.isDone()) {
                currentTask.cancel(true);
                appendMessage("SISTEMA", "Geração cancelada pelo usuário.");
                setGenerating(false);
            }
        });

        // Botão Clear
        JButton clearButton = new JButton("🗑");
        clearButton.setToolTipText("Limpar chat (Ctrl+L)");
        clearButton.setBorderPainted(false);
        clearButton.setContentAreaFilled(false);
        clearButton.addActionListener(e -> clearChat());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new javax.swing.BoxLayout(buttonPanel, javax.swing.BoxLayout.X_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(0, 0, 0, 5));
        buttonPanel.add(stopButton);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(clearButton);
        buttonPanel.add(Box.createHorizontalStrut(5));
        buttonPanel.add(configButton);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // --- CODE ACTIONS PANEL (Hidden by default) ---
        JPanel actionPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        actionPanel.setBackground(UIManager.getColor("Panel.background"));
        applyButton = new JButton("Substituir Seleção");
        insertButton = new JButton("Inserir no Cursor");
        applyButton.setVisible(false);
        insertButton.setVisible(false);

        applyButton.addActionListener(e -> applyCodeToEditor(true));
        insertButton.addActionListener(e -> applyCodeToEditor(false));

        actionPanel.add(new JLabel("Ações: "));
        actionPanel.add(applyButton);
        actionPanel.add(insertButton);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.add(actionPanel, BorderLayout.NORTH);

        // --- CHAT AREA ---
        chatArea = new RSyntaxTextArea();
        chatArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_MARKDOWN);
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setMargin(new java.awt.Insets(10, 10, 10, 10));

        // Tentar aplicar o tema do editor do NetBeans
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/idea.xml"));
            theme.apply(chatArea);
        } catch (Exception e) {
            // Usa default se falhar
        }

        RTextScrollPane scrollPane = new RTextScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        centerWrapper.add(scrollPane, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        // --- INPUT PANEL ---
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setOpaque(false);
        southPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel inputContainer = new JPanel(new BorderLayout());
        inputContainer.setBackground(UIManager.getColor("TextField.background"));
        inputContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1, true),
                new EmptyBorder(5, 5, 5, 5)));

        inputArea = new JTextArea(3, 20);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
        inputArea.setBorder(null);
        inputArea.setBackground(UIManager.getColor("TextField.background"));

        // Listener para @contexto simples
        inputArea.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER && e.isControlDown()) {
                    sendMessage();
                    e.consume();
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_L && e.isControlDown()) {
                    clearChat();
                    e.consume();
                } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_R && e.isControlDown()) {
                    refreshModels();
                    e.consume();
                }
            }
        });

        sendButton = new JButton("Enviar →");
        sendButton.setFont(new Font("SansSerif", Font.BOLD, 11));
        sendButton.setBackground(new Color(0, 120, 215)); // Azul padrão
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.addActionListener((ActionEvent e) -> sendMessage());

        inputContainer.add(new JScrollPane(inputArea), BorderLayout.CENTER);

        JPanel btnWrapper = new JPanel(new BorderLayout());
        btnWrapper.setOpaque(false);
        btnWrapper.setBorder(new EmptyBorder(0, 5, 0, 0));
        btnWrapper.add(sendButton, BorderLayout.SOUTH);

        inputContainer.add(btnWrapper, BorderLayout.EAST);
        southPanel.add(inputContainer, BorderLayout.CENTER);

        add(southPanel, BorderLayout.SOUTH);

        appendMessage("SISTEMA", "Bem-vindo ao Continue-Beans! Selecione código no editor e envie sua pergunta.");
        refreshModels();
    }

    private void refreshModels() {
        client.getModelosDisponiveisAsync().thenAccept(modelos -> {
            SwingUtilities.invokeLater(() -> {
                if (modelos != null && !modelos.isEmpty()) {
                    String current = (String) modelSelector.getSelectedItem();
                    modelSelector.removeAllItems();
                    for (String m : modelos) {
                        modelSelector.addItem(m);
                    }
                    if (current != null && modelos.contains(current)) {
                        modelSelector.setSelectedItem(current);
                    } else if (!modelos.isEmpty()) {
                        modelSelector.setSelectedIndex(0);
                    }
                    appendMessage("SISTEMA",
                            "Lista de modelos atualizada: " + modelos.size() + " modelos encontrados.");
                } else {
                    appendMessage("SISTEMA",
                            "Modelos não encontrados automaticamente. Verifique o servidor ou digite o nome do modelo manualmente.");
                }
            });
        }).exceptionally(ex -> {
            SwingUtilities.invokeLater(() -> {
                appendMessage("ERRO", "Falha ao buscar modelos: " + ex.getMessage()
                        + ". Certifique-se que o LM Studio está rodando e com o servidor local ativo.");
            });
            return null;
        });
    }

    private void sendMessage() {
        String msg = inputArea.getText().trim();
        if (msg.isEmpty())
            return;

        inputArea.setText("");
        String contextCode = EditorUtils.getSelectedCode();
        String projectDir = EditorUtils.getCurrentProjectDirectory();

        // Process context commands like @file:
        String enrichedMsg = ContextManager.processContext(msg, projectDir);

        appendMessage("VOCÊ", msg);

        String selectedModel = (String) modelSelector.getSelectedItem();
        String selectedMode = (String) modeSelector.getSelectedItem();

        String finalUrl = client.resolveUrl(ContinueSettings.getApiUrl());
        ContinueLogger.info("Enviando para: " + finalUrl + " | Modelo: " + selectedModel);

        setGenerating(true);
        appendThinking();

        StringBuilder fullResponse = new StringBuilder();
        final boolean[] firstChunk = { true };

        client.perguntarIAStreaming(contextCode, enrichedMsg, selectedModel, selectedMode,
                chunk -> {
                    SwingUtilities.invokeLater(() -> {
                        if (firstChunk[0]) {
                            removeThinking();
                            appendMessageHeader("IA");
                            firstChunk[0] = false;
                        }
                        chatArea.append(chunk);
                        fullResponse.append(chunk);
                        chatArea.setCaretPosition(chatArea.getDocument().getLength());
                    });
                },
                ex -> {
                    SwingUtilities.invokeLater(() -> {
                        removeThinking();
                        appendMessage("ERRO", "Falha na comunicação: " + ex.getMessage());
                        setGenerating(false);
                    });
                },
                () -> {
                    SwingUtilities.invokeLater(() -> {
                        chatArea.append("\n\n");
                        extractAndStoreCode(fullResponse.toString());
                        setGenerating(false);
                    });
                });
    }

    private void appendMessageHeader(String user) {
        chatArea.append("*** " + user + " ***\n");
    }

    private void removeThinking() {
        String current = chatArea.getText();
        String thinkingMark = "*** IA ***\nPensando...\n\n";
        if (current.endsWith(thinkingMark)) {
            chatArea.setText(current.substring(0, current.length() - thinkingMark.length()));
        }
    }

    private void extractAndStoreCode(String text) {
        // Regex simples para pegar conteúdo entre ```
        java.util.regex.Pattern p = java.util.regex.Pattern.compile("```(?:\\w+)?\\n([\\s\\S]*?)\\n```");
        java.util.regex.Matcher m = p.matcher(text);
        if (m.find()) {
            lastAiResponseCode = m.group(1);
            applyButton.setVisible(true);
            insertButton.setVisible(true);
        } else {
            lastAiResponseCode = null;
            applyButton.setVisible(false);
            insertButton.setVisible(false);
        }
    }

    private void applyCodeToEditor(boolean replace) {
        if (lastAiResponseCode == null)
            return;

        NotifyDescriptor d = new NotifyDescriptor.Confirmation(
                "Deseja aplicar as mudanças ao código no editor?",
                "Segurança de Código",
                NotifyDescriptor.YES_NO_OPTION);

        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
            if (replace) {
                EditorUtils.replaceSelection(lastAiResponseCode);
            } else {
                EditorUtils.insertCodeAtCursor(lastAiResponseCode);
            }
        }
    }

    private void setGenerating(boolean generating) {
        sendButton.setEnabled(!generating);
        stopButton.setEnabled(generating);
        if (!generating)
            currentTask = null;
    }

    private void appendMessage(String user, String message) {
        String current = chatArea.getText();
        String formatted = "*** " + user + " ***\n" + message + "\n\n";

        if (current.isEmpty()) {
            chatArea.setText(formatted);
        } else {
            chatArea.append(formatted);
        }
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void appendThinking() {
        chatArea.append("*** IA ***\nPensando...\n\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void clearChat() {
        chatArea.setText("");
        appendMessage("SISTEMA", "Chat limpo.");
        lastAiResponseCode = null;
        applyButton.setVisible(false);
        insertButton.setVisible(false);
    }

}
