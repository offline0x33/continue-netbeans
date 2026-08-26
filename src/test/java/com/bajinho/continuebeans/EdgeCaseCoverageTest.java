package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

class EdgeCaseCoverageTest {

    @Test
    void urlUtilsHandlesNullAndMultipleLocalhostOccurrences() {
        assertTrue(UrlUtils.resolveUrl(null).isEmpty());
        assertTrue(UrlUtils.resolveUrl("http://localhost:1234/localhost/x")
                .equals("http://127.0.0.1:1234/127.0.0.1/x"));
        assertTrue(UrlUtils.resolveUrl("http://127.0.0.1:1234")
                .equals("http://127.0.0.1:1234"));
    }

    @Test
    void payloadValidatorRejectsMissingFieldsAndWrongTypes() {
        JsonObject chat = new JsonObject();
        chat.addProperty("model", "model");
        chat.addProperty("temperature", 0.7);
        chat.addProperty("stream", true);
        chat.add("messages", new JsonArray());
        assertFalse(PayloadValidator.isValidChatPayload(chat));

        JsonObject completion = new JsonObject();
        completion.addProperty("model", "model");
        completion.addProperty("temperature", 0.7);
        completion.addProperty("stream", true);
        completion.addProperty("prompt", 123);
        assertFalse(PayloadValidator.isValidCompletionPayload(completion));
    }

    @Test
    void payloadValidatorAcceptsUnknownSystemModeWithBasePrompt() {
        String base = PayloadValidator.getSystemPrompt("unknown");
        assertTrue(base.contains("assistente avançado de programação"));

        JsonArray messages = new JsonArray();
        JsonObject system = new JsonObject();
        system.addProperty("role", "system");
        system.addProperty("content", base);
        messages.add(system);
        JsonObject payload = new JsonObject();
        payload.add("messages", messages);

        assertTrue(PayloadValidator.hasValidSystemPrompt(payload, "unknown"));
    }

    @Test
    void errorHandlerDetectsConnectionResetCaseSensitively() {
        java.io.IOException reset = new java.io.IOException("connection reset by peer");
        assertFalse(ErrorHandler.isRetryable(200, reset));
        assertTrue(ErrorHandler.isRetryable(200, new java.io.IOException("Connection reset")));
    }
}
