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

    public synchronized void addMessage(String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);
        messages.add(message);
        truncateIfNeeded();
    }

    public synchronized void addMessage(JsonObject message) {
        messages.add(message.deepCopy());
        truncateIfNeeded();
    }

    public synchronized JsonArray getMessagesArray() {
        JsonArray array = new JsonArray();
        for (JsonObject msg : messages) {
            array.add(msg.deepCopy());
        }
        return array;
    }

    public synchronized String getConversationHistory() {
        StringBuilder sb = new StringBuilder();
        for (JsonObject msg : messages) {
            String role = msg.get("role").getAsString();
            String content = msg.get("content").getAsString();
            sb.append(String.format("[%s]: %s\n", role, content));
        }
        return sb.toString();
    }

    public synchronized int getTokenCount() {
        int count = 0;
        for (JsonObject msg : messages) {
            String content = msg.get("content").getAsString();
            count += estimateTokens(content);
        }
        return count;
    }

    /** Truncate oldest non-system messages, including an oversized single message. */
    private void truncateIfNeeded() {
        while (getTokenCount() > maxTokens && !messages.isEmpty()) {
            int removable = -1;
            for (int i = 0; i < messages.size(); i++) {
                String role = messages.get(i).get("role").getAsString();
                if (!"system".equalsIgnoreCase(role)) {
                    removable = i;
                    break;
                }
            }
            if (removable < 0) {
                break;
            }
            messages.remove(removable);
        }
    }

    public synchronized void clear() {
        messages.clear();
    }

    public synchronized int getMessageCount() {
        return messages.size();
    }

    public synchronized List<JsonObject> getLastMessages(int count) {
        int startIndex = Math.max(0, messages.size() - count);
        List<JsonObject> result = new ArrayList<>();
        for (JsonObject message : messages.subList(startIndex, messages.size())) {
            result.add(message.deepCopy());
        }
        return result;
    }

    private int estimateTokens(String text) {
        if (text == null) {
            return 0;
        }
        int wordCount = text.trim().split("\\s+").length;
        return Math.max(1, (int) Math.ceil(wordCount * 1.3));
    }

    public synchronized void setMaxTokens(int maxTokens) {
        if (maxTokens < 0) {
            throw new IllegalArgumentException("maxTokens não pode ser negativo.");
        }
        this.maxTokens = maxTokens;
        truncateIfNeeded();
    }

    public synchronized boolean isAtTokenLimit() {
        return getTokenCount() >= maxTokens;
    }

    public synchronized int getRemainingTokens() {
        return Math.max(0, maxTokens - getTokenCount());
    }

    @Override
    public synchronized String toString() {
        return String.format("ConversationManager{messages=%d, tokens=%d/%d, remaining=%d}",
                messages.size(), getTokenCount(), maxTokens, getRemainingTokens());
    }
}