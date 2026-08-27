package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

/** Regression contract for the canonical Dark ChatPanel entry point. */
class ChatPanelUiContractTest {

    @Test
    void usesDarkThemeAsCanonicalPanel() {
        ChatPanel panel = new ChatPanel();

        assertEquals(new Color(0x12, 0x12, 0x14), panel.getBackground());
        assertTrue(countComponents(panel, JScrollPane.class) >= 1);
        assertTrue(countComponents(panel, JTextField.class) >= 1);
    }

    @Test
    void exposesRequiredComposerAndFooterText() {
        ChatPanel panel = new ChatPanel();
        String text = collectVisibleText(panel);

        assertTrue(text.contains("Tip: Type @ conversation"));
        assertTrue(text.contains("Local"));
        assertTrue(text.contains("continue-netbeans"));
        assertFalse(text.contains("Migrate off Cascade"));
        assertFalse(text.contains("↻ Cascade"));
    }

    @Test
    void startsWithReadyState() {
        ChatPanel panel = new ChatPanel();
        assertTrue(collectVisibleText(panel).contains("Ready. Describe what you want changed."));
    }

    private static int countComponents(Container root, Class<? extends Component> type) {
        int count = 0;
        for (Component component : root.getComponents()) {
            if (type.isInstance(component)) {
                count++;
            }
            if (component instanceof Container) {
                count += countComponents((Container) component, type);
            }
        }
        return count;
    }

    private static String collectVisibleText(Container root) {
        StringBuilder text = new StringBuilder();
        for (Component component : root.getComponents()) {
            if (component instanceof JLabel) {
                text.append(((JLabel) component).getText()).append('\n');
            } else if (component instanceof JTextArea) {
                text.append(((JTextArea) component).getText()).append('\n');
            }
            if (component instanceof Container) {
                text.append(collectVisibleText((Container) component));
            }
        }
        return text.toString();
    }
}