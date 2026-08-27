package com.bajinho.continuebeans.ui;

import org.junit.jupiter.api.Test;

import javax.swing.UIManager;
import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

class ThemeManagerTest {
    @Test
    void singletonAndPredefinedThemesWork() {
        ThemeManager manager = ThemeManager.getInstance();
        assertSame(manager, ThemeManager.getInstance());
        assertNotNull(manager.getCurrentThemeColors());
        assertNotNull(manager.getThemeColors(ThemeManager.Theme.LIGHT));
        assertNotNull(manager.getThemeColors(ThemeManager.Theme.DARK));
        assertNotNull(manager.getThemeColors(ThemeManager.Theme.NETBEANS_DARK));
        assertTrue(manager.getAvailableThemes().length >= 4);
    }

    @Test
    void switchingThemesAndColorsWorks() {
        ThemeManager manager = ThemeManager.getInstance();
        manager.setCurrentTheme(ThemeManager.Theme.DARK);
        assertEquals(ThemeManager.Theme.DARK, manager.getCurrentTheme());
        assertEquals(new Color(30, 30, 30), manager.getColor("background"));
        assertEquals(Color.WHITE, manager.getColor("primary"));
        assertEquals(manager.getColor("primary"), manager.getColor("unknown"));
        assertTrue(manager.isDarkTheme());
        manager.toggleTheme();
        assertEquals(ThemeManager.Theme.LIGHT, manager.getCurrentTheme());
    }

    @Test
    void customThemeAndUiManagerApplicationWork() {
        ThemeManager manager = ThemeManager.getInstance();
        ThemeManager.ThemeColor custom = new ThemeManager.ThemeColor(
                Color.BLACK, Color.GRAY, Color.WHITE, Color.DARK_GRAY,
                Color.BLUE, Color.RED, Color.ORANGE, Color.CYAN, Color.GREEN);
        manager.createCustomTheme("TEST_CUSTOM", custom);
        assertSame(custom, manager.getThemeColors("TEST_CUSTOM"));
        manager.setCurrentTheme(ThemeManager.Theme.LIGHT);
        manager.applyThemeToUIManager();
        assertEquals(Color.WHITE, UIManager.getColor("ContinueBeans.background"));
        assertEquals(Color.BLACK, UIManager.getColor("ContinueBeans.foreground"));
    }

    @Test
    void chatColorsPreferUiManagerAndFallback() {
        ThemeManager manager = ThemeManager.getInstance();
        Color oldBackground = UIManager.getColor("EditorPane.background");
        Color oldForeground = UIManager.getColor("EditorPane.foreground");
        try {
            UIManager.put("EditorPane.background", Color.MAGENTA);
            UIManager.put("EditorPane.foreground", Color.ORANGE);
            assertEquals(Color.MAGENTA, manager.getChatBackground());
            assertEquals(Color.ORANGE, manager.getChatForeground());
        } finally {
            UIManager.put("EditorPane.background", oldBackground);
            UIManager.put("EditorPane.foreground", oldForeground);
        }
    }
}
