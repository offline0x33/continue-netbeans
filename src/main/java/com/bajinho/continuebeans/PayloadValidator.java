package com.bajinho.continuebeans;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Validates that payloads conform to OpenAI API contract.
 * Ensures compatibility with both /chat and /completions endpoints.
 */
public class PayloadValidator {

    /**
     * Validates a chat/messages format payload.
     * Must have: model, temperature, stream, messages[] array
     */
    public static boolean isValidChatPayload(JsonObject payload) {
        if (!payload.has("model") || !payload.has("temperature") || !payload.has("stream")) {
            return false;
        }

        if (!payload.has("messages")) {
            return false;
        }

        JsonElement messagesElement = payload.get("messages");
        if (!messagesElement.isJsonArray()) {
            return false;
        }

        JsonArray messages = messagesElement.getAsJsonArray();
        if (messages.size() == 0) {
            return false; // Must have at least one message
        }

        // Validate each message has role and content
        for (JsonElement msg : messages) {
            if (!msg.isJsonObject()) {
                return false;
            }
            JsonObject msgObj = msg.getAsJsonObject();
            if (!msgObj.has("role") || !msgObj.has("content")) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates a completions/prompt format payload.
     * Must have: model, temperature, stream, prompt (string)
     */
    public static boolean isValidCompletionPayload(JsonObject payload) {
        if (!payload.has("model") || !payload.has("temperature") || !payload.has("stream")) {
            return false;
        }

        if (!payload.has("prompt")) {
            return false;
        }

        JsonElement promptElement = payload.get("prompt");
        return promptElement.isJsonPrimitive() && promptElement.getAsJsonPrimitive().isString();
    }

    /**
     * Determines endpoint type from URL.
     * Returns "chat" or "completion".
     */
    public static String getEndpointType(String url) {
        if (url != null && url.contains("/chat")) {
            return "chat";
        }
        return "completion";
    }

    /**
     * Gets system prompt based on mode.
     */
    public static String getSystemPrompt(String mode) {
        String basePrompt = "Você é um AI assistente avançado de programação profissional.";
        if ("Code".equalsIgnoreCase(mode)) {
            return basePrompt + " Foque em código limpo.";
        } else if ("Planning".equalsIgnoreCase(mode)) {
            return basePrompt + " Planeje antes de codar.";
        }
        return basePrompt;
    }

    /**
     * Validates the system prompt in a chat payload.
     */
    public static boolean hasValidSystemPrompt(JsonObject payload, String mode) {
        if (!payload.has("messages")) {
            return false;
        }

        JsonArray messages = payload.getAsJsonArray("messages");
        if (messages.size() == 0) {
            return false;
        }

        JsonObject firstMsg = messages.get(0).getAsJsonObject();
        if (!"system".equalsIgnoreCase(firstMsg.get("role").getAsString())) {
            return false;
        }

        String expectedPrompt = getSystemPrompt(mode);
        String actualPrompt = firstMsg.get("content").getAsString();
        return actualPrompt.contains(expectedPrompt);
    }
}
