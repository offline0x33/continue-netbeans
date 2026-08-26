package com.bajinho.continuebeans.ui;

import com.bajinho.continuebeans.ChatTransportMode;
import com.bajinho.continuebeans.ContinueSettings;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Compact transport selector shared by the canonical chat window. */
public final class ChatTransportSelector extends JPanel {

    private static final Color BG = new Color(0x12, 0x12, 0x14);
    private static final Color PANEL = new Color(0x1A, 0x1A, 0x1E);
    private static final Color BORDER = new Color(0x27, 0x27, 0x2A);
    private static final Color TEXT = new Color(0xE4, 0xE4, 0xE7);
    private static final Color SECONDARY = new Color(0xA1, 0xA1, 0xAA);

    private final JComboBox<ChatTransportMode> selector;

    public ChatTransportSelector() {
        setOpaque(true);
        setBackground(BG);
        setLayout(new FlowLayout(FlowLayout.RIGHT, 6, 4));

        JLabel label = new JLabel("Response");
        label.setForeground(SECONDARY);
        label.setFont(new Font("Inter", Font.PLAIN, 12));
        add(label);

        selector = new JComboBox<>(ChatTransportMode.values());
        selector.setFont(new Font("Inter", Font.PLAIN, 12));
        selector.setForeground(TEXT);
        selector.setBackground(PANEL);
        selector.setBorder(javax.swing.BorderFactory.createLineBorder(BORDER));
        selector.setSelectedItem(ContinueSettings.getChatTransportMode());
        selector.setToolTipText("Escolha entre resposta única via API ou streaming SSE");
        selector.addActionListener(event -> persistSelection());
        add(selector);
    }

    private void persistSelection() {
        ChatTransportMode selected = (ChatTransportMode) selector.getSelectedItem();
        if (selected != null) {
            ContinueSettings.setChatTransportMode(selected);
        }
    }

    public ChatTransportMode getSelectedMode() {
        return (ChatTransportMode) selector.getSelectedItem();
    }
}
