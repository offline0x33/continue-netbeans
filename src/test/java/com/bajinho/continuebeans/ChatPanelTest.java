package com.bajinho.continuebeans;

import com.bajinho.continuebeans.ai.LMStudioTextIntegration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChatPanel.
 * All Swing mutations and assertions are executed synchronously on the EDT so
 * failures are propagated to the test thread instead of being lost in AWT logs.
 */
class ChatPanelTest {

    private ChatPanel chatPanel;
    private MockedConstruction<LMStudioTextIntegration> lmStudioConstruction;

    @BeforeEach
    void setUp() throws Exception {
        lmStudioConstruction = mockConstruction(LMStudioTextIntegration.class,
                (mock, context) -> when(mock.testConnection()).thenReturn(CompletableFuture.completedFuture(false)));
        onEdt(() -> chatPanel = new ChatPanel());
    }

    @AfterEach
    void tearDown() {
        if (lmStudioConstruction != null) {
            lmStudioConstruction.close();
        }
    }

    @Test
    void testChatPanelInitialization() throws Exception {
        onEdt(() -> {
            assertNotNull(chatPanel);
            assertNotNull(chatPanel.getLlmClient());
            assertFalse(chatPanel.isProcessing());
        });
    }

    @Test
    void testClearChat() throws Exception {
        onEdt(() -> chatPanel.clearChat());
    }

    @Test
    void testGetLlmClient() throws Exception {
        onEdt(() -> assertNotNull(chatPanel.getLlmClient()));
    }

    @Test
    void testIsProcessingInitialState() throws Exception {
        onEdt(() -> assertFalse(chatPanel.isProcessing()));
    }

    @Test
    void testSendPromptWithEmptyText() throws Exception {
        onEdt(() -> {
            JTextField inputField = findTextField(chatPanel);
            assertNotNull(inputField);
            inputField.setText("");
            inputField.postActionEvent();
            assertFalse(chatPanel.isProcessing());
        });
    }

    @Test
    void testSendPromptWithWhitespaceOnly() throws Exception {
        onEdt(() -> {
            JTextField inputField = findTextField(chatPanel);
            assertNotNull(inputField);
            inputField.setText("   ");
            inputField.postActionEvent();
            assertFalse(chatPanel.isProcessing());
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
            assertNotNull(modeSelector.getSelectedItem());
            assertFalse(String.valueOf(modeSelector.getSelectedItem()).isBlank());
        });
    }

    @Test
    void testFooterStatusInitialization() throws Exception {
        onEdt(() -> {
            JLabel localStatus = findLabelContaining(chatPanel, "Local");
            JLabel migrationStatus = findLabelContaining(chatPanel, "Migrate off Cascade");
            assertNotNull(localStatus);
            assertTrue(localStatus.getText().contains("continue-netbeans"));
            assertNull(migrationStatus, "Legacy Cascade footer must not be present");
        });
    }

    @Test
    void testChatOutputInitialization() throws Exception {
        onEdt(() -> {
            JLabel initialThought = findLabelContaining(chatPanel, "Ready. Describe what you want changed.");
            assertNotNull(initialThought);
        });
    }

    @Test
    void testPanelLayout() throws Exception {
        onEdt(() -> {
            assertInstanceOf(BorderLayout.class, chatPanel.getLayout());
            BorderLayout layout = (BorderLayout) chatPanel.getLayout();
            assertEquals(0, layout.getHgap());
            assertEquals(0, layout.getVgap());
        });
    }

    @Test
    void testBorderInitialization() throws Exception {
        onEdt(() -> assertInstanceOf(EmptyBorder.class, chatPanel.getBorder()));
    }

    private static void onEdt(Runnable assertion) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            assertion.run();
            return;
        }
        SwingUtilities.invokeAndWait(assertion);
    }

    private static JTextField findTextField(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JTextField) {
                return (JTextField) component;
            }
            if (component instanceof Container) {
                JTextField found = findTextField((Container) component);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static JButton findSendButton(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JButton && "↑".equals(((JButton) component).getText())) {
                return (JButton) component;
            }
            if (component instanceof Container) {
                JButton found = findSendButton((Container) component);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static JComboBox<String> findModeSelector(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JComboBox) {
                return (JComboBox<String>) component;
            }
            if (component instanceof Container) {
                JComboBox<String> found = findModeSelector((Container) component);
                if (found != null) {
                    return found;
                }
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
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
