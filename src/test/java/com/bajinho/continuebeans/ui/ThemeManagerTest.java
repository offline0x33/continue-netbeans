package com.bajinho.continuebeans.ui;

import org.junit.jupiter.api.Test;
import javax.swing.UIManager;
import java.awt.Color;
import static org.junit.jupiter.api.Assertions.*;

class ThemeManagerTest {
    @Test void singletonAndThemesWork() {
        ThemeManager m=ThemeManager.getInstance();
        assertSame(m,ThemeManager.getInstance());
        assertNotNull(m.getCurrentThemeColors());
        assertNotNull(m.getThemeColors(ThemeManager.Theme.LIGHT));
        assertNotNull(m.getThemeColors(ThemeManager.Theme.DARK));
        assertTrue(m.getAvailableThemes().length>=4);
    }
    @Test void switchingAndFallbackWork() {
        ThemeManager m=ThemeManager.getInstance();
        m.setCurrentTheme(ThemeManager.Theme.DARK);
        assertTrue(m.isDarkTheme());
        assertEquals(new Color(30,30,30),m.getColor("background"));
        assertEquals(m.getColor("primary"),m.getColor("unknown"));
        m.toggleTheme();
        assertEquals(ThemeManager.Theme.LIGHT,m.getCurrentTheme());
    }
    @Test void customThemeAndUiApplicationWork() {
        ThemeManager m=ThemeManager.getInstance();
        ThemeManager.ThemeColor c=new ThemeManager.ThemeColor(Color.BLACK,Color.GRAY,Color.WHITE,Color.DARK_GRAY,Color.BLUE,Color.RED,Color.ORANGE,Color.CYAN,Color.GREEN);
        m.createCustomTheme("TEST_CUSTOM",c);
        assertSame(c,m.getThemeColors("TEST_CUSTOM"));
        m.setCurrentTheme(ThemeManager.Theme.LIGHT);
        m.applyThemeToUIManager();
        assertEquals(Color.WHITE,UIManager.getColor("ContinueBeans.background"));
        assertEquals(Color.BLACK,UIManager.getColor("ContinueBeans.foreground"));
    }
}