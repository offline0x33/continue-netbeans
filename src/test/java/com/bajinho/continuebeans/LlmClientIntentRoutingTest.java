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
        assertFalse(client.shouldUseTaskOrchestrator("Explique o que é Java"));
        assertFalse(client.shouldUseTaskOrchestrator("me fale desse projeto"));
        assertFalse(client.shouldUseTaskOrchestrator("fale sobre o projeto"));
        assertFalse(client.shouldUseTaskOrchestrator("o que é este projeto?"));
        assertFalse(client.shouldUseTaskOrchestrator("descreva o workspace"));
    }

    @Test
    void engineeringRequestsUseTaskOrchestrator() {
        LlmClient client = new LlmClient();

        assertTrue(client.shouldUseTaskOrchestrator("corrija o pom.xml"));
        assertTrue(client.shouldUseTaskOrchestrator("crie uma classe UserService"));
        assertTrue(client.shouldUseTaskOrchestrator("leia /home/bajinho/projeto/pom.xml"));
        assertTrue(client.shouldUseTaskOrchestrator("@codebase encontre a configuração"));
        assertTrue(client.shouldUseTaskOrchestrator("analise o projeto e liste os módulos"));
        assertTrue(client.shouldUseTaskOrchestrator("adicione dependência no pom"));
    }
}
