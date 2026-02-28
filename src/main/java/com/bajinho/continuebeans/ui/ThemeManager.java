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
    
    /**
     * Predefined themes for NetBeans integration.
     */
    public enum Theme {
        LIGHT, DARK, NETBEANS_DEFAULT, NETBEANS_DARK, CUSTOM
    }
    
    /**
     * Theme color definitions.
     */
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
        
        // Getters
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
    
    /**
     * Private constructor for singleton.
     */
    private ThemeManager() {
        this.themeColors = new HashMap<>();
        this.currentTheme = Theme.NETBEANS_DEFAULT;
        initializeThemes();
        detectNetBeansTheme();
    }
    
    /**
     * Gets the singleton instance.
     * @return The ThemeManager instance
     */
    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }
    
    /**
     * Initializes predefined themes.
     */
    private void initializeThemes() {
        // Light theme
        themeColors.put(Theme.LIGHT.name(), new ThemeColor(
            new Color(51, 51, 51),      // primary
            new Color(102, 102, 102),   // secondary
            new Color(255, 255, 255),   // background
            new Color(0, 0, 0),         // foreground
            new Color(0, 120, 215),     // accent
            new Color(220, 53, 69),     // error
            new Color(255, 193, 7),     // warning
            new Color(23, 162, 184),     // info
            new Color(40, 167, 69)       // success
        ));
        
        // Dark theme
        themeColors.put(Theme.DARK.name(), new ThemeColor(
            new Color(255, 255, 255),   // primary
            new Color(200, 200, 200),   // secondary
            new Color(30, 30, 30),       // background
            new Color(255, 255, 255),   // foreground
            new Color(0, 120, 215),     // accent
            new Color(220, 53, 69),     // error
            new Color(255, 193, 7),     // warning
            new Color(23, 162, 184),     // info
            new Color(40, 167, 69)       // success
        ));
        
        // NetBeans default theme (detected)
        themeColors.put(Theme.NETBEANS_DEFAULT.name(), detectNetBeansColors());
        
        // NetBeans dark theme
        themeColors.put(Theme.NETBEANS_DARK.name(), new ThemeColor(
            new Color(240, 240, 240),   // primary
            new Color(180, 180, 180),   // secondary
            new Color(43, 43, 43),       // background
            new Color(240, 240, 240),   // foreground
            new Color(0, 153, 204),     // accent
            new Color(204, 0, 0),        // error
            new Color(255, 204, 0),     // warning
            new Color(0, 153, 204),     // info
            new Color(0, 153, 0)        // success
        ));
    }
    
    /**
     * Detects the current NetBeans theme colors.
     * @return ThemeColor based on current NetBeans look and feel
     */
    private ThemeColor detectNetBeansColors() {
        try {
            // Try to get colors from UIManager
            Color background = getColorFromUIManager("Panel.background", Color.WHITE);
            Color foreground = getColorFromUIManager("Panel.foreground", Color.BLACK);
            Color primary = getColorFromUIManager("Label.foreground", foreground);
            Color accent = getColorFromUIManager("Button.foreground", new Color(0, 120, 215));
            
            // Determine if it's dark or light theme
            boolean isDark = isDarkColor(background);
            
            Color error = getColorFromUIManager("OptionPane.errorDialog.titlePane.background", 
                                              isDark ? new Color(220, 53, 69) : new Color(196, 30, 58));
            Color warning = getColorFromUIManager("OptionPane.warningDialog.titlePane.background", 
                                                isDark ? new Color(255, 193, 7) : new Color(255, 152, 0));
            Color info = getColorFromUIManager("OptionPane.informationDialog.titlePane.background", 
                                             isDark ? new Color(23, 162, 184) : new Color(23, 162, 184));
            Color success = isDark ? new Color(40, 167, 69) : new Color(25, 135, 84);
            
            return new ThemeColor(primary, accent.darker(), background, foreground, 
                                accent, error, warning, info, success);
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to detect NetBeans theme colors, using defaults", e);
            return themeColors.get(Theme.LIGHT.name());
        }
    }
    
    /**
     * Gets a color from UIManager with fallback.
     * @param key The UIManager key
     * @param fallback The fallback color
     * @return The color from UIManager or fallback
     */
    private Color getColorFromUIManager(String key, Color fallback) {
        Color color = UIManager.getColor(key);
        return color != null ? color : fallback;
    }
    
    /**
     * Determines if a color is dark.
     * @param color The color to check
     * @return True if the color is dark
     */
    private boolean isDarkColor(Color color) {
        // Calculate luminance
        double luminance = (0.299 * color.getRed() + 
                          0.587 * color.getGreen() + 
                          0.114 * color.getBlue()) / 255;
        return luminance < 0.5;
    }
    
    /**
     * Detects the current NetBeans theme.
     */
    private void detectNetBeansTheme() {
        try {
            // Check if we're in a dark theme by looking at the panel background
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
    
    /**
     * Gets the current theme.
     * @return The current theme
     */
    public Theme getCurrentTheme() {
        return currentTheme;
    }
    
    /**
     * Sets the current theme.
     * @param theme The theme to set
     */
    public void setCurrentTheme(Theme theme) {
        this.currentTheme = theme;
        LOG.info("Theme set to: " + theme);
    }
    
    /**
     * Gets the theme colors for the current theme.
     * @return The current theme colors
     */
    public ThemeColor getCurrentThemeColors() {
        return themeColors.get(currentTheme.name());
    }
    
    /**
     * Gets theme colors for a specific theme.
     * @param theme The theme to get colors for
     * @return The theme colors
     */
    public ThemeColor getThemeColors(Theme theme) {
        return themeColors.get(theme.name());
    }
    
    /**
     * Gets a specific color from the current theme.
     * @param colorType The color type (e.g., "primary", "background")
     * @return The color
     */
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
    
    /**
     * Updates UIManager colors with the current theme.
     */
    public void applyThemeToUIManager() {
        ThemeColor colors = getCurrentThemeColors();
        
        // Update UIManager with theme colors
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
    
    /**
     * Creates a custom theme.
     * @param name The theme name
     * @param colors The theme colors
     */
    public void createCustomTheme(String name, ThemeColor colors) {
        themeColors.put(name, colors);
        LOG.info("Created custom theme: " + name);
    }
    
    /**
     * Gets the background color for chat components.
     * @return The appropriate background color
     */
    public Color getChatBackground() {
        // Try NetBeans-specific colors first
        Color editorPane = UIManager.getColor("EditorPane.background");
        if (editorPane != null) {
            return editorPane;
        }
        
        Color textArea = UIManager.getColor("TextArea.background");
        if (textArea != null) {
            return textArea;
        }
        
        // Fallback to current theme
        return getCurrentThemeColors().getBackground();
    }
    
    /**
     * Gets the foreground color for chat components.
     * @return The appropriate foreground color
     */
    public Color getChatForeground() {
        // Try NetBeans-specific colors first
        Color editorPane = UIManager.getColor("EditorPane.foreground");
        if (editorPane != null) {
            return editorPane;
        }
        
        Color textArea = UIManager.getColor("TextArea.foreground");
        if (textArea != null) {
            return textArea;
        }
        
        // Fallback to current theme
        return getCurrentThemeColors().getForeground();
    }
    
    /**
     * Gets the accent color for UI elements.
     * @return The accent color
     */
    public Color getAccentColor() {
        return getCurrentThemeColors().getAccent();
    }
    
    /**
     * Checks if the current theme is dark.
     * @return True if the current theme is dark
     */
    public boolean isDarkTheme() {
        return isDarkColor(getCurrentThemeColors().getBackground());
    }
    
    /**
     * Toggles between light and dark themes.
     */
    public void toggleTheme() {
        if (isDarkTheme()) {
            setCurrentTheme(Theme.LIGHT);
        } else {
            setCurrentTheme(Theme.DARK);
        }
        applyThemeToUIManager();
    }
    
    /**
     * Gets all available theme names.
     * @return Array of theme names
     */
    public String[] getAvailableThemes() {
        return themeColors.keySet().toArray(new String[0]);
    }
    
    /**
     * Refreshes the theme detection (useful when NetBeans theme changes).
     */
    public void refreshThemeDetection() {
        detectNetBeansTheme();
        // Update NETBEANS_DEFAULT theme with detected colors
        themeColors.put(Theme.NETBEANS_DEFAULT.name(), detectNetBeansColors());
        LOG.info("Refreshed theme detection");
    }
}
