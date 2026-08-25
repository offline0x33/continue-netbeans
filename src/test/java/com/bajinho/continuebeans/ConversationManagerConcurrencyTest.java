package com.bajinho.continuebeans;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConversationManagerConcurrencyTest {

    @Test
    void concurrentWritersAndReadersKeepConversationConsistent() throws Exception {
        ConversationManager manager = new ConversationManager(10000);
        int writers = 8;
        int messagesPerWriter = 25;
        ExecutorService executor = Executors.newFixedThreadPool(writers + 2);
        CountDownLatch start = new CountDownLatch(1);
        List<Throwable> failures = new ArrayList<>();

        for (int writer = 0; writer < writers; writer++) {
            final int writerId = writer;
            executor.submit(() -> {
                await(start);
                try {
                    for (int i = 0; i < messagesPerWriter; i++) {
                        manager.addMessage("user", "writer-" + writerId + "-message-" + i);
                    }
                } catch (Throwable failure) {
                    synchronized (failures) {
                        failures.add(failure);
                    }
                }
            });
        }

        for (int reader = 0; reader < 2; reader++) {
            executor.submit(() -> {
                await(start);
                try {
                    for (int i = 0; i < 100; i++) {
                        manager.getMessageCount();
                        manager.getTokenCount();
                        manager.getConversationHistory();
                        manager.getMessagesArray();
                        manager.getLastMessages(10);
                    }
                } catch (Throwable failure) {
                    synchronized (failures) {
                        failures.add(failure);
                    }
                }
            });
        }

        start.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        assertTrue(failures.isEmpty(), () -> "Concurrent access failures: " + failures);
        assertEquals(writers * messagesPerWriter, manager.getMessageCount());
    }

    @Test
    void returnedMessagesAreDefensiveCopies() {
        ConversationManager manager = new ConversationManager(100);
        manager.addMessage("user", "original");

        JsonObject arrayMessage = manager.getMessagesArray().get(0).getAsJsonObject();
        arrayMessage.getAsJsonObject().addProperty("content", "mutated-array-copy");
        assertEquals("original", manager.getConversationHistory().split("\\n")[0].substring(8));

        List<JsonObject> lastMessages = manager.getLastMessages(1);
        lastMessages.get(0).addProperty("content", "mutated-list-copy");
        assertEquals("original", manager.getConversationHistory().split("\\n")[0].substring(8));
    }

    @Test
    void concurrentWritesRespectTokenLimit() throws Exception {
        ConversationManager manager = new ConversationManager(120);
        int writers = 6;
        ExecutorService executor = Executors.newFixedThreadPool(writers);
        CountDownLatch start = new CountDownLatch(1);

        for (int writer = 0; writer < writers; writer++) {
            final int writerId = writer;
            executor.submit(() -> {
                await(start);
                for (int i = 0; i < 30; i++) {
                    manager.addMessage("user", "writer-" + writerId + " payload " + i + " with enough text to exercise truncation");
                }
            });
        }

        start.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        assertTrue(manager.getTokenCount() <= 120);
        assertFalse(manager.getConversationHistory().contains("null"));
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for concurrent test start", exception);
        }
    }
}
