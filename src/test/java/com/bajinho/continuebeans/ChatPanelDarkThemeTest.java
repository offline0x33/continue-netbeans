package com.bajinho.continuebeans;

import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import static org.junit.jupiter.api.Assertions.*;

class ChatPanelDarkThemeTest {

    @Test
    void darkThemeUsesCanonicalTokens() {
        ChatPanel panel = new ChatPanel();
        assertEquals(new Color(0x12, 0x12, 0x14), panel.getBackground());
        assertTrue(findButtonByTooltip(panel, "Refresh models") != null);
    }

    @Test
    void modelRefreshControlIsFunctionalAndCascadeIsAbsent() {
        ChatPanel panel = new ChatPanel();
        JButton refresh = findButtonByTooltip(panel, "Refresh models");
        assertNotNull(refresh);
        assertTrue(refresh.isEnabled());
        assertNull(findLabelContaining(panel, "Cascade"));
    }

    @Test
    void composerUsesDarkInputAndSendControl() {
        ChatPanel panel = new ChatPanel();
        JButton send = findButtonByText(panel, "↑");
        assertNotNull(send);
        assertNotNull(panel.getLlmClient());
        assertEquals(new Color(0x12, 0x12, 0x14), panel.getBackground());
    }

    private static JButton findButtonByTooltip(Container container, String tooltip) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton && tooltip.equals(((JButton) component).getToolTipText())) {
                return (JButton) component;
            }
            if (component instanceof Container) {
                JButton found = findButtonByTooltip((Container) component, tooltip);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static JButton findButtonByText(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton && text.equals(((JButton) component).getText())) {
                return (JButton) component;
            }
            if (component instanceof Container) {
                JButton found = findButtonByText((Container) component, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static JLabel findLabelContaining(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JLabel && ((JLabel) component).getText() != null
                    && ((JLabel) component).getText().contains(text)) {
                return (JLabel) component;
            }
            if (component instanceof Container) {
                JLabel found = findLabelContaining((Container) component, text);
                if (found != null) return found;
            }
        }
        return null;
    }
}
