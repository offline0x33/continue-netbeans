package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

class PayloadValidatorUrlUtilsGapTest {

    @Test
    void rejectsBlankOrMissingModels() {
        assertFalse(PayloadValidator.isValidChatPayload(chatPayload(null, 0.7, "user")));
        assertFalse(PayloadValidator.isValidChatPayload(chatPayload("", 0.7, "user")));
        assertFalse(PayloadValidator.isValidCompletionPayload(completionPayload("   ", 0.7)));
    }

    @Test
    void rejectsTemperatureOutsideSupportedRange() {
        assertFalse(PayloadValidator.isValidChatPayload(chatPayload("model", -0.01, "user")));
        assertFalse(PayloadValidator.isValidChatPayload(chatPayload("model", 2.01, "user")));
        assertTrue(PayloadValidator.isValidChatPayload(chatPayload("model", 0.0, "user")));
        assertTrue(PayloadValidator.isValidChatPayload(chatPayload("model", 2.0, "user")));
    }

    @Test
    void rejectsInvalidRolesButAcceptsSupportedRoles() {
        assertFalse(PayloadValidator.isValidChatPayload(chatPayload("model", 0.7, "invalid")));
        assertTrue(PayloadValidator.isValidChatPayload(chatPayload("model", 0.7, "system")));
        assertTrue(PayloadValidator.isValidChatPayload(chatPayload("model", 0.7, "user")));
        assertTrue(PayloadValidator.isValidChatPayload(chatPayload("model", 0.7, "assistant")));
        assertTrue(PayloadValidator.isValidChatPayload(chatPayload("model", 0.7, "tool")));
    }

    @Test
    void rejectsNonNumericTemperature() {
        JsonObject payload = chatPayload("model", 0.7, "user");
        payload.addProperty("temperature", "hot");
        assertFalse(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    void endpointResolutionHandlesNullBlankAndHosts() {
        assertTrue(PayloadValidator.getEndpointType(null).equals("completion"));
        assertTrue(PayloadValidator.getEndpointType("").equals("completion"));
        assertTrue(PayloadValidator.getEndpointType("http://localhost/v1/chat/completions").equals("chat"));
        assertTrue(PayloadValidator.getEndpointType("http://127.0.0.1/v1/completions").equals("completion"));
    }

    @Test
    void urlResolutionKeepsBlankAndRemoteHostsAndRewritesLocalhost() {
        assertTrue(UrlUtils.resolveUrl(null).isEmpty());
        assertTrue(UrlUtils.resolveUrl("").isEmpty());
        assertTrue(UrlUtils.resolveUrl("   ").equals("   "));
        assertTrue(UrlUtils.resolveUrl("http://localhost:1234/v1").equals("http://127.0.0.1:1234/v1"));
        assertTrue(UrlUtils.resolveUrl("http://127.0.0.1:1234/v1").equals("http://127.0.0.1:1234/v1"));
        assertTrue(UrlUtils.resolveUrl("https://example.com/v1").equals("https://example.com/v1"));
    }

    private static JsonObject chatPayload(String model, double temperature, String role) {
        JsonObject payload = new JsonObject();
        if (model != null) {
            payload.addProperty("model", model);
        }
        payload.addProperty("temperature", temperature);
        payload.addProperty("stream", true);
        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", "test");
        messages.add(message);
        payload.add("messages", messages);
        return payload;
    }

    private static JsonObject completionPayload(String model, double temperature) {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", model);
        payload.addProperty("temperature", temperature);
        payload.addProperty("stream", true);
        payload.addProperty("prompt", "test");
        return payload;
    }
}
