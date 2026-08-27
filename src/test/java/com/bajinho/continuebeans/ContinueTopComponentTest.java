package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bajinho.continuebeans.ai.LMStudioTextIntegration;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.CompletableFuture;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

/** Regression tests for the NetBeans TopComponent container. */
class ContinueTopComponentTest {

    private ContinueTopComponent topComponent;
    private MockedConstruction<LMStudioTextIntegration> lmStudioConstruction;

    @BeforeEach
    void setUp() throws Exception {
        lmStudioConstruction = org.mockito.Mockito.mockConstruction(LMStudioTextIntegration.class,
                (mock, context) -> org.mockito.Mockito.when(mock.testConnection())
                        .thenReturn(CompletableFuture.completedFuture(false)));
        onEdt(() -> topComponent = new ContinueTopComponent());
    }

    @AfterEach
    void tearDown() {
        if (lmStudioConstruction != null) {
            lmStudioConstruction.close();
        }
    }

    @Test
    void initializesCanonicalChatPanel() throws Exception {
        onEdt(() -> {
            assertNotNull(topComponent);
            assertEquals("Continue Beans", topComponent.getName());
            assertInstanceOf(BorderLayout.class, topComponent.getLayout());
            ChatPanel chatPanel = findComponent(topComponent, ChatPanel.class);
            assertNotNull(chatPanel, "TopComponent must contain the canonical ChatPanel");
        });
    }

    @Test
    void usesChatPanelAsOnlyCentralContent() throws Exception {
        onEdt(() -> {
            BorderLayout layout = (BorderLayout) topComponent.getLayout();
            Component center = layout.getLayoutComponent(BorderLayout.CENTER);
            assertInstanceOf(ChatPanel.class, center);
            assertEquals(1, topComponent.getComponentCount(),
                    "TopComponent should not maintain a second legacy chat implementation");
        });
    }

    @Test
    void topComponentStartsClosed() throws Exception {
        onEdt(() -> assertFalse(topComponent.isOpened()));
    }

    @Test
    void chatPanelRetainsDarkTheme() throws Exception {
        onEdt(() -> {
            ChatPanel chatPanel = findComponent(topComponent, ChatPanel.class);
            assertNotNull(chatPanel);
            assertEquals(new java.awt.Color(0x12, 0x12, 0x14), chatPanel.getBackground());
        });
    }

    @Test
    void initializationIsDeterministicOnEdt() throws Exception {
        onEdt(() -> assertTrue(SwingUtilities.isEventDispatchThread()));
    }

    private static <T extends Component> T findComponent(Container root, Class<T> type) {
        for (Component component : root.getComponents()) {
            if (type.isInstance(component)) {
                return type.cast(component);
            }
            if (component instanceof Container) {
                T nested = findComponent((Container) component, type);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static void onEdt(Runnable action) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            action.run();
            return;
        }
        SwingUtilities.invokeAndWait(action);
    }
}
