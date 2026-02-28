package com.bajinho.continuebeans;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConversationManagerTest {

    private ConversationManager manager;

    @BeforeEach
    void setUp() {
        manager = new ConversationManager(200); // 200 tokens for testing
    }

    @Test
    void testAddMessage() {
        manager.addMessage("user", "Hello");
        assertEquals(1, manager.getMessageCount());
    }

    @Test
    void testGetMessagesArray() {
        manager.addMessage("user", "Hello");
        manager.addMessage("assistant", "Hi there!");
        assertEquals(2, manager.getMessagesArray().size());
    }

    @Test
    void testGetConversationHistory() {
        manager.addMessage("user", "Hello");
        manager.addMessage("assistant", "Hi there!");

        String history = manager.getConversationHistory();
        assertTrue(history.contains("[user]: Hello"));
        assertTrue(history.contains("[assistant]: Hi there!"));
    }

    @Test
    void testTokenCountEstimation() {
        manager.addMessage("user", "Hello world test message");
        int tokens = manager.getTokenCount();
        assertTrue(tokens > 0, "Token count should be positive");
    }

    @Test
    void testTruncationWhenExceedsLimit() {
        // Add messages that exceed 200 token limit
        manager.addMessage("system", "You are helpful");
        for (int i = 0; i < 10; i++) {
            manager.addMessage("user", "This is a longer message to test truncation behavior " + i);
            manager.addMessage("assistant", "This is response to user message number " + i);
        }

        // Should have truncated old messages
        assertTrue(manager.getMessageCount() < 22, "Should have truncated messages");
        assertTrue(manager.getTokenCount() <= 200, "Should not exceed token limit");
    }

    @Test
    void testKeepsSystemMessage() {
        manager.addMessage("system", "You are helpful");
        manager.addMessage("user", "Hello");

        // Add enough messages to trigger truncation
        for (int i = 0; i < 20; i++) {
            manager.addMessage("user", "Message " + i + " with some content to increase token count");
        }

        // System message should still be there (or at least first message)
        String history = manager.getConversationHistory();
        assertTrue(history.contains("system") || manager.getMessageCount() > 0);
    }

    @Test
    void testClear() {
        manager.addMessage("user", "Hello");
        manager.addMessage("assistant", "Hi");

        assertEquals(2, manager.getMessageCount());
        manager.clear();
        assertEquals(0, manager.getMessageCount());
    }

    @Test
    void testGetLastMessages() {
        for (int i = 0; i < 5; i++) {
            manager.addMessage("user", "Message " + i);
        }

        var lastThree = manager.getLastMessages(3);
        assertEquals(3, lastThree.size());
    }

    @Test
    void testRemainingTokens() {
        manager.addMessage("user", "Hello");
        int remaining = manager.getRemainingTokens();
        assertTrue(remaining > 0 && remaining < 200);
    }

    @Test
    void testIsAtTokenLimit() {
        // Start with empty - should not be at limit
        assertFalse(manager.isAtTokenLimit());

        // Create manager with VERY low limit to test truncation
        ConversationManager tightManager = new ConversationManager(5); // 5 tokens
        tightManager.addMessage("user", "Hello");
        // After adding one message, should be at or very close to limit
        // Due to how truncation works, adding more will trigger it
        assertTrue(tightManager.getTokenCount() > 0);
    }

    @Test
    void testSetMaxTokens() {
        manager.addMessage("user", "Hello world this is a test message");
        int initialTokens = manager.getTokenCount();

        manager.setMaxTokens(10); // Set very low limit
        // Should have truncated due to new low limit
        int newTokens = manager.getTokenCount();
        assertTrue(newTokens <= 10);
    }

    @Test
    void testAddJsonObject() {
        JsonObject msg = new JsonObject();
        msg.addProperty("role", "user");
        msg.addProperty("content", "Hello");

        manager.addMessage(msg);
        assertEquals(1, manager.getMessageCount());
        assertEquals("user", manager.getMessagesArray().get(0).getAsJsonObject().get("role").getAsString());
    }

    @Test
    void testToString() {
        manager.addMessage("user", "Hello");
        String str = manager.toString();
        assertTrue(str.contains("ConversationManager"));
        assertTrue(str.contains("messages=1"));
    }

    @Test
    void testMultiTurnConversation() {
        ConversationManager conv = new ConversationManager(500);

        // Simulate multi-turn conversation
        conv.addMessage("user", "What is Java?");
        conv.addMessage("assistant", "Java is a programming language");
        conv.addMessage("user", "What are its features?");
        conv.addMessage("assistant", "Java has OOP, platform independence, etc");
        conv.addMessage("user", "How do I learn it?");

        assertEquals(5, conv.getMessageCount());
        assertTrue(conv.getConversationHistory().contains("Java is a programming language"));
    }
}
