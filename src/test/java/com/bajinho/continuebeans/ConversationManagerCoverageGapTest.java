package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConversationManagerCoverageGapTest {

    @Test
    void storesMessagesAndReturnsIndependentSnapshots() {
        ConversationManager manager = new ConversationManager(100);
        manager.addMessage("user", "hello");
        manager.addMessage("assistant", "world");

        assertEquals(2, manager.getMessageCount());
        assertEquals("[user]: hello\n[assistant]: world\n", manager.getConversationHistory());
        JsonArray array = manager.getMessagesArray();
        array.get(0).getAsJsonObject().addProperty("content", "changed");
        assertEquals("hello", manager.getLastMessages(2).get(0).get("content").getAsString());
    }

    @Test
    void lastMessagesRespectOrderAndBounds() {
        ConversationManager manager = new ConversationManager(100);
        manager.addMessage("user", "one");
        manager.addMessage("assistant", "two");
        manager.addMessage("user", "three");

        List<JsonObject> lastTwo = manager.getLastMessages(2);
        assertEquals(2, lastTwo.size());
        assertEquals("two", lastTwo.get(0).get("content").getAsString());
        assertEquals("three", lastTwo.get(1).get("content").getAsString());
        assertEquals(0, manager.getLastMessages(0).size());
        assertEquals(3, manager.getLastMessages(99).size());
    }

    @Test
    void truncationKeepsSystemMessageAndTracksRemainingCapacity() {
        ConversationManager manager = new ConversationManager(5);
        manager.addMessage("system", "system message");
        manager.addMessage("user", "one two three four five six");
        assertEquals(1, manager.getMessageCount());
        assertEquals("system", manager.getLastMessages(1).get(0).get("role").getAsString());
        manager.clear();
        assertEquals(0, manager.getMessageCount());
        assertEquals(5, manager.getRemainingTokens());
        assertTrue(manager.toString().contains("messages=0"));
    }

    @Test
    void changingTokenBudgetCanTriggerTruncationAndLimitFlag() {
        ConversationManager manager = new ConversationManager(100);
        manager.addMessage("user", "one two three four five");
        manager.setMaxTokens(1);
        assertEquals(0, manager.getMessageCount());
        assertEquals(0, manager.getTokenCount());
        assertEquals(1, manager.getRemainingTokens());
        assertFalse(manager.isAtTokenLimit());
    }
}