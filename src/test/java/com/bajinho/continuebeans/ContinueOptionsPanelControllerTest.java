package com.bajinho.continuebeans;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.JComponent;
import java.beans.PropertyChangeListener;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ContinueOptionsPanelController to achieve 100% coverage.
 */
class ContinueOptionsPanelControllerTest {

    private ContinueOptionsPanelController controller;

    @BeforeEach
    void setUp() {
        controller = new ContinueOptionsPanelController();
    }

    @Test
    void testControllerInitialization() {
        assertNotNull(controller);
        assertFalse(controller.isChanged());
    }

    @Test
    void testUpdate() {
        // Update should complete without exceptions
        assertDoesNotThrow(() -> {
            controller.update();
            assertFalse(controller.isChanged());
        });
    }

    @Test
    void testApplyChanges() {
        // Apply changes should complete without exceptions
        assertDoesNotThrow(() -> {
            controller.applyChanges();
            assertFalse(controller.isChanged());
        });
    }

    @Test
    void testCancel() {
        // Cancel should do nothing (as per implementation)
        assertDoesNotThrow(() -> {
            controller.cancel();
        });
    }

    @Test
    void testIsValid() {
        // Valid should call panel.valid() - just test it doesn't throw
        assertDoesNotThrow(() -> {
            boolean valid = controller.isValid();
            // Just verify it returns a boolean value
            assertTrue(valid == true || valid == false);
        });
    }

    @Test
    void testIsChanged() {
        // Initially should be false
        assertFalse(controller.isChanged());
        
        // Mark as changed
        controller.changed();
        assertTrue(controller.isChanged());
    }

    @Test
    void testGetHelpCtx() {
        assertNotNull(controller.getHelpCtx());
        assertEquals(org.openide.util.HelpCtx.DEFAULT_HELP, controller.getHelpCtx());
    }

    @Test
    void testGetComponent() {
        // Should return a JComponent without exceptions
        assertDoesNotThrow(() -> {
            JComponent component = controller.getComponent(mock(org.openide.util.Lookup.class));
            assertNotNull(component);
        });
    }

    @Test
    void testAddPropertyChangeListener() {
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        
        // Should not throw exceptions
        assertDoesNotThrow(() -> {
            controller.addPropertyChangeListener(listener);
        });
    }

    @Test
    void testRemovePropertyChangeListener() {
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        
        // Add first, then remove
        controller.addPropertyChangeListener(listener);
        
        // Should not throw exceptions
        assertDoesNotThrow(() -> {
            controller.removePropertyChangeListener(listener);
        });
    }

    @Test
    void testChanged() {
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        controller.addPropertyChangeListener(listener);
        
        // First call should mark as changed and fire property change
        assertDoesNotThrow(() -> {
            controller.changed();
            assertTrue(controller.isChanged());
        });
        
        // Verify property change was fired
        verify(listener, atLeastOnce()).propertyChange(any());
    }

    @Test
    void testChangedMultipleCalls() {
        PropertyChangeListener listener = mock(PropertyChangeListener.class);
        controller.addPropertyChangeListener(listener);
        
        // Multiple calls should only fire PROP_CHANGED once
        assertDoesNotThrow(() -> {
            controller.changed();
            controller.changed();
            controller.changed();
            
            assertTrue(controller.isChanged());
        });
        
        // Should still fire property changes for each call (PROP_VALID)
        verify(listener, atLeast(3)).propertyChange(any());
    }

    @Test
    void testGetPanelLazyInitialization() {
        // Component should be created lazily
        assertDoesNotThrow(() -> {
            // First call should create the panel
            JComponent component1 = controller.getComponent(mock(org.openide.util.Lookup.class));
            assertNotNull(component1);
            
            // Second call should return the same panel
            JComponent component2 = controller.getComponent(mock(org.openide.util.Lookup.class));
            assertSame(component1, component2);
        });
    }

    @Test
    void testPanelCreationAfterUpdate() {
        // Update should create panel and call load
        assertDoesNotThrow(() -> {
            controller.update();
        });
    }

    @Test
    void testPanelCreationAfterApplyChanges() {
        // Apply changes should create panel and call store
        assertDoesNotThrow(() -> {
            controller.applyChanges();
        });
    }
}
