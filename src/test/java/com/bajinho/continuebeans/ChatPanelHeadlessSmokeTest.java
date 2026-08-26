package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ChatPanelHeadlessSmokeTest {
    private String originalHeadless;
    private String originalApiUrl;

    @BeforeEach
    void setUp() {
        originalHeadless = System.getProperty("java.awt.headless");
        originalApiUrl = ContinueSettings.getApiUrl();
        System.setProperty("java.awt.headless", "true");
        ContinueSettings.setApiUrl("");
    }

    @AfterEach
    void tearDown() {
        ContinueSettings.setApiUrl(originalApiUrl);
        if (originalHeadless == null) {
            System.clearProperty("java.awt.headless");
        } else {
            System.setProperty("java.awt.headless", originalHeadless);
        }
    }

    @Test
    void constructsWithoutTopLevelWindows() {
        ChatPanel panel = new ChatPanel();
        assertNotNull(panel);
        assertNotNull(panel.getLlmClient());
        assertFalse(panel.isProcessing());
    }

    @Test
    void clearChatResetsProcessingAndCanBeCalledRepeatedly() {
        ChatPanel panel = new ChatPanel();
        panel.clearChat();
        panel.clearChat();
        assertFalse(panel.isProcessing());
        assertNotNull(panel.getLlmClient());
    }
}
