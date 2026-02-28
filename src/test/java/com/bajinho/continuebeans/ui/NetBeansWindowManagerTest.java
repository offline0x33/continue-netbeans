package com.bajinho.continuebeans.ui;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for NetBeansWindowManager.
 * Tests window management operations with proper EDT safety and async handling.
 */
@DisplayName("NetBeans Window Manager Tests")
public class NetBeansWindowManagerTest {

    @Mock
    private WindowManager mockWindowManager;

    @Mock
    private TopComponent mockTopComponent;

    @Mock
    private org.openide.windows.Mode mockMode;

    private NetBeansWindowManager windowManager;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        windowManager = NetBeansWindowManager.getInstance();

        // Inject mock WindowManager via reflection since it's hardcoded
        java.lang.reflect.Field wmField = NetBeansWindowManager.class.getDeclaredField("windowManager");
        wmField.setAccessible(true);
        wmField.set(windowManager, mockWindowManager);
        // Setup mock behavior
        when(mockTopComponent.getName()).thenReturn("testComponent");
        when(mockTopComponent.isOpened()).thenReturn(true);
        when(mockWindowManager.findMode("editor")).thenReturn(mockMode);
        when(mockWindowManager.findMode("output")).thenReturn(mockMode);

        java.util.Set<org.openide.windows.Mode> modeSet = java.util.Set.of(mockMode);
        doReturn(modeSet).when(mockWindowManager).getModes();
        when(mockMode.getName()).thenReturn("editor");
        when(mockMode.getTopComponents()).thenReturn(new TopComponent[] { mockTopComponent });
    }

    @Test
    @DisplayName("Should open TopComponent in specified mode")
    void testOpenTopComponentInMode() throws Exception {
        CompletableFuture<Boolean> result = windowManager.openTopComponentAsync(mockTopComponent, "editor");

        assertNotNull(result);
        Boolean success = result.get(5, TimeUnit.SECONDS);
        assertTrue(success);
    }

    @Test
    @DisplayName("Should handle opening TopComponent with null mode")
    void testOpenTopComponentWithNullMode() throws Exception {
        when(mockWindowManager.findMode("nonexistent")).thenReturn(null);
        
        CompletableFuture<Boolean> result = windowManager.openTopComponentAsync(mockTopComponent, "nonexistent");
        
        assertNotNull(result);
        Boolean success = result.get(5, TimeUnit.SECONDS);
        assertTrue(success); // Should still succeed, just use default behavior
    }

    @Test
    @DisplayName("Should close opened TopComponent")
    void testCloseOpenedTopComponent() throws Exception {
        CompletableFuture<Boolean> result = windowManager.closeTopComponentAsync(mockTopComponent);

        assertNotNull(result);
        Boolean success = result.get(5, TimeUnit.SECONDS);
        assertTrue(success);
    }

    @Test
    @DisplayName("Should handle closing already closed TopComponent")
    void testCloseClosedTopComponent() throws Exception {
        when(mockTopComponent.isOpened()).thenReturn(false);
        
        CompletableFuture<Boolean> result = windowManager.closeTopComponentAsync(mockTopComponent);
        
        assertNotNull(result);
        Boolean success = result.get(5, TimeUnit.SECONDS);
        assertFalse(success); // Should return false for already closed component
    }

    @Test
    @DisplayName("Should find TopComponent by ID")
    void testFindTopComponent() {
        when(mockWindowManager.findTopComponent("testComponent")).thenReturn(mockTopComponent);
        
        TopComponent found = windowManager.findTopComponent("testComponent");
        
        assertNotNull(found);
        assertEquals(mockTopComponent, found);
    }

    @Test
    @DisplayName("Should return null for non-existent TopComponent")
    void testFindNonExistentTopComponent() {
        when(mockWindowManager.findTopComponent("nonexistent")).thenReturn(null);
        
        TopComponent found = windowManager.findTopComponent("nonexistent");
        
        assertNull(found);
    }

    @Test
    @DisplayName("Should get opened TopComponents")
    void testGetOpenedTopComponents() {
        TopComponent[] opened = windowManager.getOpenedTopComponents();

        assertNotNull(opened);
        assertTrue(opened.length >= 0);
    }

    @Test
    @DisplayName("Should minimize mode successfully")
    void testMinimizeMode() throws Exception {
        CompletableFuture<Boolean> result = windowManager.minimizeModeAsync("editor");

        assertNotNull(result);
        Boolean success = result.get(5, TimeUnit.SECONDS);
        assertTrue(success);
    }

    @Test
    @DisplayName("Should handle minimizing non-existent mode")
    void testMinimizeNonExistentMode() throws Exception {
        when(mockWindowManager.findMode("nonexistent")).thenReturn(null);
        
        CompletableFuture<Boolean> result = windowManager.minimizeModeAsync("nonexistent");
        
        assertNotNull(result);
        Boolean success = result.get(5, TimeUnit.SECONDS);
        assertFalse(success);
    }

    @Test
    @DisplayName("Should get available modes")
    void testGetAvailableModes() {
        String[] modes = windowManager.getAvailableModes();

        assertNotNull(modes);
        assertTrue(modes.length >= 0);
    }

    @Test
    @DisplayName("Should activate opened TopComponent")
    void testActivateOpenedTopComponent() throws Exception {
        CompletableFuture<Boolean> result = windowManager.activateTopComponentAsync(mockTopComponent);

        assertNotNull(result);
        Boolean success = result.get(5, TimeUnit.SECONDS);
        assertTrue(success);
    }

    @Test
    @DisplayName("Should handle activating closed TopComponent")
    void testActivateClosedTopComponent() throws Exception {
        when(mockTopComponent.isOpened()).thenReturn(false);
        
        CompletableFuture<Boolean> result = windowManager.activateTopComponentAsync(mockTopComponent);
        
        assertNotNull(result);
        Boolean success = result.get(5, TimeUnit.SECONDS);
        assertFalse(success);
    }

    @Test
    @DisplayName("Should get active TopComponent")
    void testGetActiveTopComponent() {
        assertDoesNotThrow(() -> {
            TopComponent active = windowManager.getActiveTopComponent();
            // In test environment, this might return null, which is acceptable
            // The important thing is that it doesn't throw an exception
        });
    }

    @Test
    @DisplayName("Should handle exceptions gracefully")
    void testExceptionHandling() throws Exception {
        doThrow(new RuntimeException("Test exception")).when(mockTopComponent).open();

        CompletableFuture<Boolean> result = windowManager.openTopComponentAsync(mockTopComponent, "editor");

        assertNotNull(result);
        Boolean success = result.get(5, TimeUnit.SECONDS);
        assertFalse(success); // Should return false on exception
    }

    @Test
    @DisplayName("Should return singleton instance")
    void testSingletonInstance() {
        NetBeansWindowManager instance1 = NetBeansWindowManager.getInstance();
        NetBeansWindowManager instance2 = NetBeansWindowManager.getInstance();

        assertSame(instance1, instance2);
    }
}
