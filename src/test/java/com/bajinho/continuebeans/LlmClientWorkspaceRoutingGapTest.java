package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LlmClientWorkspaceRoutingGapTest {
    private final LlmClient client = new LlmClient();

    @Test
    void blankAndNullMessagesDoNotUseWorkspaceTools() {
        assertFalse(client.shouldUseWorkspaceTools(null));
        assertFalse(client.shouldUseWorkspaceTools(""));
        assertFalse(client.shouldUseWorkspaceTools("   "));
        assertFalse(client.shouldUseWorkspaceTools("/home/user/project/Foo.java"));
    }

    @Test
    void explicitFileAndCodebaseReferencesUseWorkspaceTools() {
        assertTrue(client.shouldUseWorkspaceTools("@file:src/Main.java"));
        assertTrue(client.shouldUseWorkspaceTools("@codebase encontre UserService"));
    }

    @Test
    void absolutePathsRequireActionVerb() {
        String base = "/home/user/project/Foo.java ";
        assertTrue(client.shouldUseWorkspaceTools(base + "leia"));
        assertTrue(client.shouldUseWorkspaceTools(base + "read"));
        assertTrue(client.shouldUseWorkspaceTools(base + "liste"));
        assertTrue(client.shouldUseWorkspaceTools(base + "list"));
        assertTrue(client.shouldUseWorkspaceTools(base + "abra"));
        assertTrue(client.shouldUseWorkspaceTools(base + "open"));
        assertTrue(client.shouldUseWorkspaceTools(base + "analise"));
        assertTrue(client.shouldUseWorkspaceTools(base + "edite"));
        assertTrue(client.shouldUseWorkspaceTools(base + "corrija"));
        assertTrue(client.shouldUseWorkspaceTools(base + "build"));
        assertTrue(client.shouldUseWorkspaceTools(base + "crie"));
    }

    @Test
    void windowsStyleAbsolutePathIsRecognizedWhenCombinedWithAction() {
        assertTrue(client.shouldUseWorkspaceTools("C:\\project\\Main.java read"));
        assertFalse(client.shouldUseWorkspaceTools("C:\\project\\Main.java"));
    }
}
