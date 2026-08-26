package com.bajinho.continuebeans.ui;

import java.awt.Color;
import java.awt.Font;

/** Design tokens shared by the canonical Continue Beans chat UI. */
public final class UiTokens {

    public static final Color BACKGROUND_MAIN = new Color(0x12, 0x12, 0x14);
    public static final Color BACKGROUND_SECONDARY = new Color(0x1A, 0x1A, 0x1E);
    public static final Color BORDER = new Color(0x27, 0x27, 0x2A);
    public static final Color TEXT_PRIMARY = new Color(0xE4, 0xE4, 0xE7);
    public static final Color TEXT_SECONDARY = new Color(0xA1, 0xA1, 0xAA);
    public static final Color TEXT_MUTED = new Color(0x71, 0x71, 0x7A);
    public static final Color ACCENT_BLUE = new Color(0x60, 0xA5, 0xFA);
    public static final Color SUCCESS_GREEN = new Color(0x4A, 0xDE, 0x80);
    public static final Color ERROR_RED = new Color(0xF8, 0x71, 0x71);
    public static final Color WARNING_ORANGE = new Color(0xF9, 0x73, 0x16);
    public static final Color WARNING_BACKGROUND = new Color(0x2D, 0x1C, 0x11);
    public static final Color WARNING_BORDER = new Color(0x52, 0x2E, 0x15);
    public static final Color SEND_BACKGROUND = new Color(0x3F, 0x3F, 0x46);

    public static final Font UI = new Font("Inter", Font.PLAIN, 13);
    public static final Font UI_MEDIUM = new Font("Inter", Font.BOLD, 13);
    public static final Font SMALL = new Font("Inter", Font.PLAIN, 12);
    public static final Font CODE = new Font("JetBrains Mono", Font.PLAIN, 12);

    private UiTokens() {
    }
}
