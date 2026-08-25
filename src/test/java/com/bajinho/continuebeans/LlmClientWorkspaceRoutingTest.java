package com.bajinho.continuebeans;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LlmClientWorkspaceRoutingTest {

    @Test
    void routesPortugueseAbsolutePathReadRequestToWorkspaceTools() {
        LlmClient client = new LlmClient();

        assertTrue(client.shouldUseWorkspaceTools(
                "então leia o código /home/bajinho/github/continue-netbeans/src"));
    }

    @Test
    void routesExplicitContextCommandsToWorkspaceTools() {
        LlmClient client = new LlmClient();

        assertTrue(client.shouldUseWorkspaceTools("leia @codebase e encontre os pontos de tool calling"));
        assertTrue(client.shouldUseWorkspaceTools("leia @file:src/main/java/com/bajinho/continuebeans/LlmClient.java"));
    }

    @Test
    void keepsOrdinaryConversationOnStreamingProviderPath() {
        LlmClient client = new LlmClient();

        assertFalse(client.shouldUseWorkspaceTools("explique como funciona dependency injection em Java"));
    }
}
