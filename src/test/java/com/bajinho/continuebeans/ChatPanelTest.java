package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.util.function.Supplier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChatPanelTest {

    private ChatPanel chatPanel;

    @BeforeEach
    void setUp() throws Exception {
        onEdt(() -> {
            chatPanel = new ChatPanel();
        });
    }

    @Test
    void testSendPromptWithValidText() throws Exception {
        onEdt(() -> {
            JTextField inputField = findTextField(chatPanel);
            JButton sendButton = findSendButton(chatPanel);
            assertNotNull(inputField);
            assertNotNull(sendButton);

            inputField.setText("test prompt");
            sendButton.doClick();
        });
    }

    @Test
    void testModeSelectorInitialization() throws Exception {
        onEdt(() -> {
            JComboBox<String> modeSelector = findModeSelector(chatPanel);
            assertNotNull(modeSelector);
            assertTrue(modeSelector.getItemCount() >= 1);
            Object selected = modeSelector.getSelectedItem();
            assertNotNull(selected);
            assertTrue(!String.valueOf(selected).isBlank());
        });
    }

    @Test
    void testFooterStatusInitialization() throws Exception {
        onEdt(() -> {
            JLabel localStatus = findLabelContaining(chatPanel, "Local");
            JLabel migrationStatus = findLabelContaining(chatPanel, "Migrate off Cascade");
            assertNotNull(localStatus);
            assertTrue(localStatus.getText().contains("continue-netbeans"));
            assertTrue(migrationStatus == null, "Legacy Cascade footer must not be present");
        });
    }

    @Test
    void testChatOutputInitialization() throws Exception {
        onEdt(() -> {
            JLabel initialThought = findLabelContaining(chatPanel, "Ready. Describe what you want changed.");
            assertNotNull(initialThought);
        });
    }

    private static void onEdt(ThrowingRunnable action) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
            return;
        }
        final Throwable[] failure = {null};
        SwingUtilities.invokeAndWait(() -> {
            try {
                action.run();
            } catch (Throwable throwable) {
                failure[0] = throwable;
            }
        });
        if (failure[0] != null) {
            if (failure[0] instanceof Exception) {
                throw (Exception) failure[0];
            }
            if (failure[0] instanceof Error) {
                throw (Error) failure[0];
            }
            throw new RuntimeException(failure[0]);
        }
    }

    private static JTextField findTextField(Component root) {
        return findComponent(root, JTextField.class);
    }

    private static JButton findSendButton(Component root) {
        return findButton(root, "↑");
    }

    private static JComboBox<String> findModeSelector(Component root) {
        return findComponent(root, JComboBox.class);
    }

    private static <T extends Component> T findComponent(Component root, Class<T> type) {
        if (type.isInstance(root)) {
            return type.cast(root);
        }
        if (root instanceof Container) {
            for (Component child : ((Container) root).getComponents()) {
                T found = findComponent(child, type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static JButton findButton(Component root, String text) {
        if (root instanceof JButton && text.equals(((JButton) root).getText())) {
            return (JButton) root;
        }
        if (root instanceof Container) {
            for (Component child : ((Container) root).getComponents()) {
                JButton found = findButton(child, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static JLabel findLabelContaining(Component root, String text) {
        if (root instanceof JLabel && ((JLabel) root).getText() != null
                && ((JLabel) root).getText().contains(text)) {
            return (JLabel) root;
        }
        if (root instanceof Container) {
            for (Component child : ((Container) root).getComponents()) {
                JLabel found = findLabelContaining(child, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unused")
    private static <T> T getField(Object target, String name, Class<T> type) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(target));
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }
}
