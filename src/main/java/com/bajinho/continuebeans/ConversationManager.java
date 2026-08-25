package com.bajinho.continuebeans;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages multi-turn conversations with smart token-based truncation.
 * Implements context window optimization to fit within LLM token limits.
 */
public class ConversationManager {

    private static final int DEFAULT_MAX_TOKENS = 4000;

    private final List<JsonObject> messages = new ArrayList<>();
    private int maxTokens;

    public ConversationManager(int maxTokens) {
        this.maxTokens = maxTokens;
    }

    public ConversationManager() {
        this(DEFAULT_MAX_TOKENS);
    }

    /**
     * Add a message to conversation history.
     * Automatically truncates old messages if context window is exceeded.
     */
    public synchronized void addMessage(String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);

        messages.add(message);
        truncateIfNeeded();
    }

    /**
     * Add a message object directly.
     */
    public synchronized void addMessage(JsonObject message) {
        messages.add(message);
        truncateIfNeeded();
    }

    /**
     * Get all messages as JsonArray.
     */
    public synchronized JsonArray getMessagesArray() {
        JsonArray array = new JsonArray();
        for (JsonObject msg : messages) {
            array.add(msg.deepCopy());
        }
        return array;
    }

    /**
     * Get messages formatted as string for display.
     */
    public synchronized String getConversationHistory() {
        StringBuilder sb = new StringBuilder();
        for (JsonObject msg : messages) {
            String role = msg.get("role").getAsString();
            String content = msg.get("content").getAsString();
            sb.append(String.format("[%s]: %s\n", role, content));
        }
        return sb.toString();
    }

    /**
     * Get current token count of all messages.
     */
    public synchronized int getTokenCount() {
        int count = 0;
        for (JsonObject msg : messages) {
            String content = msg.get("content").getAsString();
            count += estimateTokens(content);
        }
        return count;
    }

    /**
     * Truncate oldest messages if conversation exceeds token limit.
     * Keeps system message (if present) as it's important for context.
     */
    private void truncateIfNeeded() {
        while (getTokenCount() > maxTokens && messages.size() > 1) {
            for (int i = 0; i < messages.size(); i++) {
                String role = messages.get(i).get("role").getAsString();
                if (!"system".equalsIgnoreCase(role)) {
                    messages.remove(i);
                    break;
                }
            }

            if (getTokenCount() > maxTokens && messages.size() == 1) {
                String role = messages.get(0).get("role").getAsString();
                if (!"system".equalsIgnoreCase(role)) {
                    messages.remove(0);
                }
            }
        }
    }

    /**
     * Clear all messages.
     */
    public synchronized void clear() {
        messages.clear();
    }

    /**
     * Get number of messages.
     */
    public synchronized int getMessageCount() {
        return messages.size();
    }

    /**
     * Get last N messages (useful for context preview).
     * Returned messages are copies so callers cannot mutate internal state.
     */
    public synchronized List<JsonObject> getLastMessages(int count) {
        int startIndex = Math.max(0, messages.size() - count);
        List<JsonObject> result = new ArrayList<>();
        for (JsonObject message : messages.subList(startIndex, messages.size())) {
            result.add(message.deepCopy());
        }
        return result;
    }

    /**
     * Estimate token count from text.
     * Using rough estimate: 1 token ≈ 4 characters.
     */
    private int estimateTokens(String text) {
        if (text == null) {
            return 0;
        }
        int wordCount = text.trim().split("\\s+").length;
        return Math.max(1, (int) Math.ceil(wordCount * 1.3));
    }

    /**
     * Set custom max tokens.
     */
    public synchronized void setMaxTokens(int maxTokens) {
        this.maxTokens = maxTokens;
        truncateIfNeeded();
    }

    /**
     * Check if conversation is at token limit.
     */
    public synchronized boolean isAtTokenLimit() {
        return getTokenCount() >= maxTokens;
    }

    /**
     * Get remaining token capacity.
     */
    public synchronized int getRemainingTokens() {
        return Math.max(0, maxTokens - getTokenCount());
    }

    /**
     * Get summary of conversation (for logging).
     */
    @Override
    public synchronized String toString() {
        return String.format("ConversationManager{messages=%d, tokens=%d/%d, remaining=%d}",
                messages.size(), getTokenCount(), maxTokens, getRemainingTokens());
    }
}
