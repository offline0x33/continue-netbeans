package com.bajinho.continuebeans;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ChatTransportModeTest {

    @AfterEach
    void restoreDefault() {
        ContinueSettings.setChatTransportMode(ChatTransportMode.STREAM);
    }

    @Test
    void defaultsToStream() {
        assertEquals(ChatTransportMode.STREAM, ChatTransportMode.defaultMode());
        assertNotNull(ChatTransportMode.STREAM.name());
    }

    @Test
    void persistsApiMode() {
        ContinueSettings.setChatTransportMode(ChatTransportMode.API);
        assertEquals(ChatTransportMode.API, ContinueSettings.getChatTransportMode());
    }

    @Test
    void persistsStreamMode() {
        ContinueSettings.setChatTransportMode(ChatTransportMode.STREAM);
        assertEquals(ChatTransportMode.STREAM, ContinueSettings.getChatTransportMode());
    }
}
