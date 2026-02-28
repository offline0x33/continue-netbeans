package com.bajinho.continuebeans;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extended tests for PayloadValidator to achieve 100% coverage.
 */
class PayloadValidatorExtendedTest {

    private Gson gson = new Gson();

    @Test
    void testValidChatPayloadMinimal() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gpt-4");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", "Hello");
        messages.add(message);

        payload.add("messages", messages);

        assertTrue(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    void testValidChatPayloadMultipleMessages() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gpt-4");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);

        JsonArray messages = new JsonArray();
        
        JsonObject msg1 = new JsonObject();
        msg1.addProperty("role", "system");
        msg1.addProperty("content", "You are helpful");
        messages.add(msg1);
        
        JsonObject msg2 = new JsonObject();
        msg2.addProperty("role", "user");
        msg2.addProperty("content", "Question");
        messages.add(msg2);

        payload.add("messages", messages);

        assertTrue(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    void testInvalidChatPayloadMissingModel() {
        JsonObject payload = new JsonObject();
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", "Test");
        messages.add(message);
        payload.add("messages", messages);

        assertFalse(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    void testInvalidChatPayloadMissingTemperature() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gpt-4");
        payload.addProperty("stream", true);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", "Test");
        messages.add(message);
        payload.add("messages", messages);

        assertFalse(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    void testInvalidChatPayloadMissingStream() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gpt-4");
        payload.addProperty("temperature", 0.7);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", "Test");
        messages.add(message);
        payload.add("messages", messages);

        assertFalse(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    void testInvalidChatPayloadMissingMessages() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gpt-4");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);

        assertFalse(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    void testInvalidChatPayloadMessagesNotArray() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gpt-4");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);
        payload.addProperty("messages", "not an array");

        assertFalse(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    void testInvalidChatPayloadEmptyMessages() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gpt-4");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);
        payload.add("messages", new JsonArray());

        assertFalse(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    void testInvalidChatPayloadMessageNotObject() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gpt-4");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);

        JsonArray messages = new JsonArray();
        messages.add("not an object");

        payload.add("messages", messages);

        assertFalse(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    void testInvalidChatPayloadMessageMissingRole() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gpt-4");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("content", "Hello");
        messages.add(message);

        payload.add("messages", messages);

        assertFalse(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    void testInvalidChatPayloadMessageMissingContent() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gpt-4");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        messages.add(message);

        payload.add("messages", messages);

        assertFalse(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    void testValidCompletionPayload() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gpt-4");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);
        payload.addProperty("prompt", "This is a test prompt");

        assertTrue(PayloadValidator.isValidCompletionPayload(payload));
    }

    @Test
    void testInvalidCompletionPayloadMissingModel() {
        JsonObject payload = new JsonObject();
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);
        payload.addProperty("prompt", "Test");

        assertFalse(PayloadValidator.isValidCompletionPayload(payload));
    }

    @Test
    void testInvalidCompletionPayloadMissingPrompt() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gpt-4");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);

        assertFalse(PayloadValidator.isValidCompletionPayload(payload));
    }

    @Test
    void testValidChatPayloadWithAdditionalFields() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gpt-4");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);
        payload.addProperty("max_tokens", 1000);
        payload.addProperty("top_p", 0.9);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "user");
        message.addProperty("content", "Test");
        messages.add(message);

        payload.add("messages", messages);

        assertTrue(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    void testMessageWithMultipleFields() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "gpt-4");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);

        JsonArray messages = new JsonArray();
        JsonObject message = new JsonObject();
        message.addProperty("role", "assistant");
        message.addProperty("content", "Response");
        message.addProperty("name", "assistant");
        messages.add(message);

        payload.add("messages", messages);

        assertTrue(PayloadValidator.isValidChatPayload(payload));
    }
}
