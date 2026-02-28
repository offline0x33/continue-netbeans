package com.bajinho.continuebeans;

import javax.swing.GroupLayout;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

final class ContinuePanel extends JPanel {

    private final JTextField apiUrlField;
    private final JTextField modelField;
    private final JTextField temperatureField;
    private final JButton fetchModelsButton;
    private final ContinueOptionsPanelController controller;

    ContinuePanel(ContinueOptionsPanelController controller) {
        this.controller = controller;
        JLabel apiUrlLabel = new JLabel("API URL:");
        apiUrlField = new JTextField();
        JLabel modelLabel = new JLabel("Model:");
        modelField = new JTextField();
        fetchModelsButton = new JButton("Buscar Modelos");
        JLabel temperatureLabel = new JLabel("Temperature:");
        temperatureField = new JTextField();

        fetchModelsButton.addActionListener(e -> fetchModels());

        DocumentListener dl = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                ContinuePanel.this.controller.changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                ContinuePanel.this.controller.changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                ContinuePanel.this.controller.changed();
            }
        };
        apiUrlField.getDocument().addDocumentListener(dl);
        modelField.getDocument().addDocumentListener(dl);
        temperatureField.getDocument().addDocumentListener(dl);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(apiUrlLabel)
                                        .addComponent(modelLabel)
                                        .addComponent(temperatureLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(apiUrlField, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(modelField)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(fetchModelsButton))
                                        .addComponent(temperatureField))
                                .addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(apiUrlLabel)
                                        .addComponent(apiUrlField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(modelLabel)
                                        .addComponent(modelField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
                                                GroupLayout.PREFERRED_SIZE)
                                        .addComponent(fetchModelsButton))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(temperatureLabel)
                                        .addComponent(temperatureField, GroupLayout.PREFERRED_SIZE,
                                                GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
    }

    private void fetchModels() {
        LlmClient client = new LlmClient();
        fetchModelsButton.setEnabled(false);
        client.getModelosDisponiveisAsync().thenAccept(modelos -> {
            SwingUtilities.invokeLater(() -> {
                if (modelos.isEmpty()) {
                    apiUrlField.setToolTipText("Nenhum modelo encontrado. A URL está correta?");
                } else {
                    // Por simplicidade, pega o primeiro ou exibe diálogo
                    modelField.setText(modelos.get(0));
                }
                fetchModelsButton.setEnabled(true);
            });
        });
    }

    void load() {
        apiUrlField.setText(ContinueSettings.getApiUrl());
        modelField.setText(ContinueSettings.getModel());
        temperatureField.setText(String.valueOf(ContinueSettings.getTemperature()));
    }

    void store() {
        ContinueSettings.setApiUrl(apiUrlField.getText());
        ContinueSettings.setModel(modelField.getText());
        try {
            ContinueSettings.setTemperature(Double.parseDouble(temperatureField.getText()));
        } catch (NumberFormatException e) {
            // Keep previous value if invalid
        }
    }

    boolean valid() {
        try {
            Double.parseDouble(temperatureField.getText());
            return !apiUrlField.getText().trim().isEmpty() && !modelField.getText().trim().isEmpty();
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
