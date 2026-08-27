package com.bajinho.continuebeans.ui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;

/**
 * Theme manager for NetBeans integration with dynamic theme switching
 * and automatic color adaptation based on NetBeans look and feel.
 * 
 * @author Continue Beans Team
 */
public class ThemeManager {
    
    private static final Logger LOG = Logger.getLogger(ThemeManager.class.getName());
    private static ThemeManager instance;
    
    private final Map<String, ThemeColor> themeColors;
    private Theme currentTheme;
    
    public enum Theme {
        LIGHT, DARK, NETBEANS_DEFAULT, NETBEANS_DARK, CUSTOM
    }
    
    public static class ThemeColor {
        private final Color primary;
        private final Color secondary;
        private final Color background;
        private final Color foreground;
        private final Color accent;
        private final Color error;
        private final Color warning;
        private final Color info;
        private final Color success;
        
        public ThemeColor(Color primary, Color secondary, Color background, Color foreground,
                        Color accent, Color error, Color warning, Color info, Color success) {
            this.primary = primary;
            this.secondary = secondary;
            this.background = background;
            this.foreground = foreground;
            this.accent = accent;
            this.error = error;
            this.warning = warning;
            this.info = info;
            this.success = success;
        }
        
        public Color getPrimary() { return primary; }
        public Color getSecondary() { return secondary; }
        public Color getBackground() { return background; }
        public Color getForeground() { return foreground; }
        public Color getAccent() { return accent; }
        public Color getError() { return error; }
        public Color getWarning() { return warning; }
        public Color getInfo() { return info; }
        public Color getSuccess() { return success; }
    }
    
    private ThemeManager() {
        this.themeColors = new HashMap<>();
        this.currentTheme = Theme.NETBEANS_DEFAULT;
        initializeThemes();
        detectNetBeansTheme();
    }
    
    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    private void initializeThemes() {
        themeColors.put(Theme.LIGHT.name(), new ThemeColor(
            new Color(51, 51, 51), new Color(102, 102, 102), new Color(255, 255, 255),
            new Color(0, 0, 0), new Color(0, 120, 215), new Color(220, 53, 69),
            new Color(255, 193, 7), new Color(23, 162, 184), new Color(40, 167, 69)
        ));
        themeColors.put(Theme.DARK.name(), new ThemeColor(
            new Color(255, 255, 255), new Color(200, 200, 200), new Color(30, 30, 30),
            new Color(255, 255, 255), new Color(0, 120, 215), new Color(220, 53, 69),
            new Color(255, 193, 7), new Color(23, 162, 184), new Color(40, 167, 69)
        ));
        themeColors.put(Theme.NETBEANS_DEFAULT.name(), detectNetBeansColors());
        themeColors.put(Theme.NETBEANS_DARK.name(), new ThemeColor(
            new Color(240, 240, 240), new Color(180, 180, 180), new Color(43, 43, 43),
            new Color(240, 240, 240), new Color(0, 153, 204), new Color(204, 0, 0),
            new Color(255, 204, 0), new Color(0, 153, 204), new Color(0, 153, 0)
        ));
    }
    
    private ThemeColor detectNetBeansColors() {
        try {
            Color background = getColorFromUIManager("Panel.background", Color.WHITE);
            Color foreground = getColorFromUIManager("Panel.foreground", Color.BLACK);
            Color primary = getColorFromUIManager("Label.foreground", foreground);
            Color accent = getColorFromUIManager("Button.foreground", new Color(0, 120, 215));
            boolean isDark = isDarkColor(background);
            Color error = getColorFromUIManager("OptionPane.errorDialog.titlePane.background", 
                                              isDark ? new Color(220, 53, 69) : new Color(196, 30, 58));
            Color warning = getColorFromUIManager("OptionPane.warningDialog.titlePane.background", 
                                                isDark ? new Color(255, 193, 7) : new Color(255, 152, 0));
            Color info = getColorFromUIManager("OptionPane.informationDialog.titlePane.background", 
                                             isDark ? new Color(23, 162, 184) : new Color(23, 162, 184));
            Color success = isDark ? new Color(40, 167, 69) : new Color(25, 135, 84);
            return new ThemeColor(primary, accent.darker(), background, foreground, accent, error, warning, info, success);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to detect NetBeans theme colors, using defaults", e);
            return themeColors.get(Theme.LIGHT.name());
        }
    }
    
    private Color getColorFromUIManager(String key, Color fallback) {
        Color color = UIManager.getColor(key);
        return color != null ? color : fallback;
    }
    
    private boolean isDarkColor(Color color) {
        double luminance = (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;
        return luminance < 0.5;
    }
    
    private void detectNetBeansTheme() {
        try {
            Color panelBackground = UIManager.getColor("Panel.background");
            if (panelBackground != null) {
                if (isDarkColor(panelBackground)) {
                    currentTheme = Theme.NETBEANS_DARK;
                } else {
                    currentTheme = Theme.NETBEANS_DEFAULT;
                }
            }
            LOG.info("Detected NetBeans theme: " + currentTheme);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to detect NetBeans theme, using default", e);
            currentTheme = Theme.NETBEANS_DEFAULT;
        }
    }
    
    public Theme getCurrentTheme() { return currentTheme; }
    
    public void setCurrentTheme(Theme theme) {
        this.currentTheme = theme;
        LOG.info("Theme set to: " + theme);
    }
    
    public ThemeColor getCurrentThemeColors() { return themeColors.get(currentTheme.name()); }
    
    public ThemeColor getThemeColors(Theme theme) { return themeColors.get(theme.name()); }

    /**
     * Gets theme colors for a theme registered by name.
     *
     * @param themeName the registered theme name
     * @return the theme colors, or null when no theme is registered with that name
     */
    public ThemeColor getThemeColors(String themeName) {
        return themeColors.get(themeName);
    }
    
    public Color getColor(String colorType) {
        ThemeColor colors = getCurrentThemeColors();
        switch (colorType.toLowerCase()) {
            case "primary": return colors.getPrimary();
            case "secondary": return colors.getSecondary();
            case "background": return colors.getBackground();
            case "foreground": return colors.getForeground();
            case "accent": return colors.getAccent();
            case "error": return colors.getError();
            case "warning": return colors.getWarning();
            case "info": return colors.getInfo();
            case "success": return colors.getSuccess();
            default: return colors.getPrimary();
        }
    }
    
    public void applyThemeToUIManager() {
        ThemeColor colors = getCurrentThemeColors();
        UIManager.put("ContinueBeans.primary", new ColorUIResource(colors.getPrimary()));
        UIManager.put("ContinueBeans.secondary", new ColorUIResource(colors.getSecondary()));
        UIManager.put("ContinueBeans.background", new ColorUIResource(colors.getBackground()));
        UIManager.put("ContinueBeans.foreground", new ColorUIResource(colors.getForeground()));
        UIManager.put("ContinueBeans.accent", new ColorUIResource(colors.getAccent()));
        UIManager.put("ContinueBeans.error", new ColorUIResource(colors.getError()));
        UIManager.put("ContinueBeans.warning", new ColorUIResource(colors.getWarning()));
        UIManager.put("ContinueBeans.info", new ColorUIResource(colors.getInfo()));
        UIManager.put("ContinueBeans.success", new ColorUIResource(colors.getSuccess()));
        LOG.info("Applied theme colors to UIManager");
    }
    
    public void createCustomTheme(String name, ThemeColor colors) {
        themeColors.put(name, colors);
        LOG.info("Created custom theme: " + name);
    }
    
    public Color getChatBackground() {
        Color editorPane = UIManager.getColor("EditorPane.background");
        if (editorPane != null) return editorPane;
        Color textArea = UIManager.getColor("TextArea.background");
        if (textArea != null) return textArea;
        return getCurrentThemeColors().getBackground();
    }
    
    public Color getChatForeground() {
        Color editorPane = UIManager.getColor("EditorPane.foreground");
        if (editorPane != null) return editorPane;
        Color textArea = UIManager.getColor("TextArea.foreground");
        if (textArea != null) return textArea;
        return getCurrentThemeColors().getForeground();
    }
    
    public Color getAccentColor() { return getCurrentThemeColors().getAccent(); }
    
    public boolean isDarkTheme() { return isDarkColor(getCurrentThemeColors().getBackground()); }
    
    public void toggleTheme() {
        if (isDarkTheme()) setCurrentTheme(Theme.LIGHT);
        else setCurrentTheme(Theme.DARK);
        applyThemeToUIManager();
    }
    
    public String[] getAvailableThemes() { return themeColors.keySet().toArray(new String[0]); }
    
    public void refreshThemeDetection() {
        detectNetBeansTheme();
        themeColors.put(Theme.NETBEANS_DEFAULT.name(), detectNetBeansColors());
        LOG.info("Refreshed theme detection");
    }
}
