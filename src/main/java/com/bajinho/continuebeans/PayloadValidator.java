package com.bajinho.continuebeans;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Validates that payloads conform to OpenAI API contract.
 * Ensures compatibility with both /chat and /completions endpoints.
 */
public class PayloadValidator {

    private static final double MIN_TEMPERATURE = 0.0;
    private static final double MAX_TEMPERATURE = 2.0;
    private static final String[] CHAT_ROLES = {"system", "user", "assistant", "tool"};

    /**
     * Validates a chat/messages format payload.
     * Must have: model, temperature, stream, messages[] array.
     */
    public static boolean isValidChatPayload(JsonObject payload) {
        if (!hasCommonFields(payload)) {
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
            return false;
        }

        for (JsonElement msg : messages) {
            if (!msg.isJsonObject()) {
                return false;
            }
            JsonObject msgObj = msg.getAsJsonObject();
            if (!msgObj.has("role") || !msgObj.has("content")) {
                return false;
            }
            if (!msgObj.get("role").isJsonPrimitive()
                    || !msgObj.getAsJsonPrimitive("role").isString()
                    || !isValidRole(msgObj.get("role").getAsString())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates a completions/prompt format payload.
     * Must have: model, temperature, stream, prompt (string).
     */
    public static boolean isValidCompletionPayload(JsonObject payload) {
        if (!hasCommonFields(payload)) {
            return false;
        }

        if (!payload.has("prompt")) {
            return false;
        }

        JsonElement promptElement = payload.get("prompt");
        return promptElement.isJsonPrimitive() && promptElement.getAsJsonPrimitive().isString();
    }

    private static boolean hasCommonFields(JsonObject payload) {
        if (payload == null || !payload.has("model") || !payload.has("temperature") || !payload.has("stream")) {
            return false;
        }

        JsonElement model = payload.get("model");
        if (!model.isJsonPrimitive() || !model.getAsJsonPrimitive().isString()
                || model.getAsString().trim().isEmpty()) {
            return false;
        }

        JsonElement temperature = payload.get("temperature");
        if (!temperature.isJsonPrimitive() || !temperature.getAsJsonPrimitive().isNumber()) {
            return false;
        }

        double value = temperature.getAsDouble();
        return !Double.isNaN(value) && !Double.isInfinite(value)
                && value >= MIN_TEMPERATURE && value <= MAX_TEMPERATURE;
    }

    private static boolean isValidRole(String role) {
        for (String allowedRole : CHAT_ROLES) {
            if (allowedRole.equalsIgnoreCase(role)) {
                return true;
            }
        }
        return false;
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
        if (!firstMsg.has("role") || !firstMsg.has("content")
                || !"system".equalsIgnoreCase(firstMsg.get("role").getAsString())) {
            return false;
        }

        String expectedPrompt = getSystemPrompt(mode);
        String actualPrompt = firstMsg.get("content").getAsString();
        return actualPrompt.contains(expectedPrompt);
    }
}
