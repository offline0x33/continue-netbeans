package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LlmClientIntentRoutingTest {

    @Test
    void conversationalMessagesStayOnDirectChatPath() {
        LlmClient client = new LlmClient();

        assertFalse(client.shouldUseTaskOrchestrator("Olá"));
        assertFalse(client.shouldUseTaskOrchestrator("Como você está?"));
        assertFalse(client.shouldUseTaskOrchestrator("Explique o que é Maven"));
    }

    @Test
    void engineeringRequestsUseTaskOrchestrator() {
        LlmClient client = new LlmClient();

        assertTrue(client.shouldUseTaskOrchestrator("corrija o pom.xml"));
        assertTrue(client.shouldUseTaskOrchestrator("crie uma classe UserService"));
        assertTrue(client.shouldUseTaskOrchestrator("leia /home/bajinho/projeto/pom.xml"));
        assertTrue(client.shouldUseTaskOrchestrator("@codebase encontre a configuração"));
    }
}
