package com.bajinho.continuebeans;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PayloadValidatorTest {

    @Test
    public void testValidChatPayload() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "llama3");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);

        JsonArray messages = new JsonArray();
        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        sysMsg.addProperty("content", "You are helpful");
        messages.add(sysMsg);

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", "Hello");
        messages.add(userMsg);

        payload.add("messages", messages);

        assertTrue(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    public void testInvalidChatPayloadMissingModel() {
        JsonObject payload = new JsonObject();
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);

        JsonArray messages = new JsonArray();
        payload.add("messages", messages);

        assertFalse(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    public void testInvalidChatPayloadMissingMessages() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "llama3");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);

        assertFalse(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    public void testInvalidChatPayloadEmptyMessages() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "llama3");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);
        payload.add("messages", new JsonArray());

        assertFalse(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    public void testInvalidChatPayloadMissingMessageRole() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "llama3");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);

        JsonArray messages = new JsonArray();
        JsonObject msg = new JsonObject();
        msg.addProperty("content", "Hello");
        messages.add(msg);
        payload.add("messages", messages);

        assertFalse(PayloadValidator.isValidChatPayload(payload));
    }

    @Test
    public void testValidCompletionPayload() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "llama3");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);
        payload.addProperty("prompt", "What is AI?");

        assertTrue(PayloadValidator.isValidCompletionPayload(payload));
    }

    @Test
    public void testInvalidCompletionPayloadMissingPrompt() {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", "llama3");
        payload.addProperty("temperature", 0.7);
        payload.addProperty("stream", true);

        assertFalse(PayloadValidator.isValidCompletionPayload(payload));
    }

    @Test
    public void testGetEndpointTypeChat() {
        assertEquals("chat", PayloadValidator.getEndpointType("http://localhost:1234/v1/chat/completions"));
    }

    @Test
    public void testGetEndpointTypeCompletion() {
        assertEquals("completion", PayloadValidator.getEndpointType("http://localhost:1234/v1/completions"));
    }

    @Test
    public void testGetSystemPromptCode() {
        String prompt = PayloadValidator.getSystemPrompt("Code");
        assertTrue(prompt.contains("código limpo"));
    }

    @Test
    public void testGetSystemPromptPlanning() {
        String prompt = PayloadValidator.getSystemPrompt("Planning");
        assertTrue(prompt.contains("Planeje antes de codar"));
    }

    @Test
    public void testHasValidSystemPromptCode() {
        JsonObject payload = new JsonObject();
        JsonArray messages = new JsonArray();

        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        sysMsg.addProperty("content", "Você é um AI assistente avançado de programação profissional. Foque em código limpo.");
        messages.add(sysMsg);

        payload.add("messages", messages);

        assertTrue(PayloadValidator.hasValidSystemPrompt(payload, "Code"));
    }

    @Test
    public void testHasInvalidSystemPrompt() {
        JsonObject payload = new JsonObject();
        JsonArray messages = new JsonArray();

        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        sysMsg.addProperty("content", "Wrong prompt");
        messages.add(sysMsg);

        payload.add("messages", messages);

        assertFalse(PayloadValidator.hasValidSystemPrompt(payload, "Code"));
    }
}
