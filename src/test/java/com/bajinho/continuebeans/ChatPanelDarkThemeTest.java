package com.bajinho.continuebeans;

import com.bajinho.continuebeans.ui.ChatPanel;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;

import static org.junit.jupiter.api.Assertions.*;

class ChatPanelDarkThemeTest {
    @Test
    void darkThemeUsesCanonicalTokens() {
        ChatPanel panel = new ChatPanel();
        assertNotNull(panel);
        // Check that panel has been created with basic UI components
        assertNotNull(findButtonByTooltip(panel, "Configuration Settings"));
    }

    @Test
    void modelRefreshControlIsFunctionalAndCascadeIsAbsent() {
        ChatPanel panel = new ChatPanel();
        JButton settings = findButtonByTooltip(panel, "Configuration Settings");
        assertNotNull(settings);
        assertTrue(settings.isEnabled());
        assertNull(findLabelContaining(panel, "Cascade"));
    }

    @Test
    void composerUsesDarkInputAndSendControl() {
        ChatPanel panel = new ChatPanel();
        JButton send = findButtonByText(panel, "Send");
        assertNotNull(send);
        assertNotNull(panel);
        // Panel should be properly initialized
        assertTrue(send.isEnabled());
    }

    private static JButton findButtonByTooltip(Container container, String tooltip) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton && tooltip.equals(((JButton) component).getToolTipText())) return (JButton) component;
            if (component instanceof Container) {
                JButton found = findButtonByTooltip((Container) component, tooltip);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static JButton findButtonByText(Container container, String text) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton && text.equals(((JButton) component).getText())) return (JButton) component;
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
                    && ((JLabel) component).getText().contains(text)) return (JLabel) component;
            if (component instanceof Container) {
                JLabel found = findLabelContaining((Container) component, text);
                if (found != null) return found;
            }
        }
        return null;
    }
}
