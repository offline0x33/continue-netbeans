package com.bajinho.continuebeans;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LlmClientRoutingCoverageTest {

    private final LlmClient client = new LlmClient();

    @AfterEach
    void resetMode() {
        ContinueSettings.setAgentMode(AgentMode.CODE);
    }

    @Test
    void nullMessageNeverBecomesTask() {
        assertFalse(client.shouldUseTaskOrchestrator(null, AgentMode.CODE));
    }

    @Test
    void blankMessageNeverBecomesTask() {
        assertFalse(client.shouldUseTaskOrchestrator("   ", AgentMode.CODE));
    }

    @Test
    void chatOnlyModesAlwaysStayOutOfTaskGraph() {
        assertFalse(client.shouldUseTaskOrchestrator("implemente uma classe", AgentMode.DOCS));
        assertFalse(client.shouldUseTaskOrchestrator("implemente uma classe", AgentMode.PLANNING));
    }

    @Test
    void agentModeRoutesEngineeringAndNaturalLanguageRequestsToTaskGraph() {
        assertTrue(client.shouldUseTaskOrchestrator("implemente uma classe", AgentMode.AGENT));
        assertTrue(client.shouldUseTaskOrchestrator("faça isso direito", AgentMode.AGENT));
    }

    @Test
    void agentModeStillFiltersGreetings() {
        assertFalse(client.shouldUseTaskOrchestrator("oi", AgentMode.AGENT));
        assertFalse(client.shouldUseTaskOrchestrator("como você está?", AgentMode.AGENT));
    }

    @Test
    void codeModeRecognizesExplicitFileReference() {
        assertTrue(client.shouldUseTaskOrchestrator("edite @file:src/main/java/App.java", AgentMode.CODE));
        assertTrue(client.shouldUseTaskOrchestrator("analise @codebase", AgentMode.CODE));
    }

    @Test
    void codeModeRecognizesAbsoluteWorkspacePath() {
        assertTrue(client.shouldUseTaskOrchestrator("leia /workspace/project/App.java", AgentMode.CODE));
        assertTrue(client.shouldUseTaskOrchestrator("build /tmp/project", AgentMode.CODE));
    }

    @Test
    void codeModeRecognizesTaskActionWords() {
        assertTrue(client.shouldUseTaskOrchestrator("corrija o teste quebrado", AgentMode.CODE));
        assertTrue(client.shouldUseTaskOrchestrator("refatore o serviço", AgentMode.CODE));
        assertTrue(client.shouldUseTaskOrchestrator("write a new parser", AgentMode.CODE));
    }

    @Test
    void codeModeKeepsInformationalQuestionsInChat() {
        assertFalse(client.shouldUseTaskOrchestrator("o que é um workspace?", AgentMode.CODE));
        assertFalse(client.shouldUseTaskOrchestrator("me explique o projeto", AgentMode.CODE));
    }

    @Test
    void codeModeRejectsPunctuationOnlyInput() {
        assertFalse(client.shouldUseTaskOrchestrator("!!! ???", AgentMode.CODE));
    }

    @Test
    void workspaceToolsRequireExplicitWorkspaceIntent() {
        assertTrue(client.shouldUseWorkspaceTools("ler /home/user/project/App.java"));
        assertTrue(client.shouldUseWorkspaceTools("analise @codebase"));
        assertFalse(client.shouldUseWorkspaceTools("olá, explique Java"));
        assertFalse(client.shouldUseWorkspaceTools("/home/user/project/App.java"));
    }

    @Test
    void workspaceToolsRecognizeAdditionalAbsolutePathActions() {
        assertTrue(client.shouldUseWorkspaceTools("open /opt/project/pom.xml"));
        assertTrue(client.shouldUseWorkspaceTools("editar /tmp/App.java"));
        assertTrue(client.shouldUseWorkspaceTools("compile /workspace/project"));
        assertTrue(client.shouldUseWorkspaceTools("execute /var/tmp/run.sh"));
        assertTrue(client.shouldUseWorkspaceTools("crie /home/user/project/Test.java"));
    }

    @Test
    void workspaceToolsRejectUnknownActionWithAbsolutePath() {
        assertFalse(client.shouldUseWorkspaceTools("visite /home/user/project"));
    }

    @Test
    void defaultRoutingUsesConfiguredMode() {
        ContinueSettings.setAgentMode(AgentMode.AGENT);
        assertTrue(client.shouldUseTaskOrchestrator("faça a alteração", null));

        ContinueSettings.setAgentMode(AgentMode.PLANNING);
        assertFalse(client.shouldUseTaskOrchestrator("faça a alteração", null));
    }

    @Test
    void resolveUrlDelegatesToUrlUtils() {
        String endpoint = "http://localhost:1234";
        String completionEndpoint = endpoint + "/v1/chat/completions";

        try (MockedStatic<UrlUtils> urlUtils = mockStatic(UrlUtils.class)) {
            urlUtils.when(() -> UrlUtils.resolveUrl(endpoint)).thenReturn(endpoint);
            urlUtils.when(() -> UrlUtils.resolveUrl(completionEndpoint)).thenReturn(completionEndpoint);

            assertEquals(endpoint, client.resolveUrl(endpoint));
            assertEquals(completionEndpoint, client.resolveUrl(completionEndpoint));

            urlUtils.verify(() -> UrlUtils.resolveUrl(endpoint));
            urlUtils.verify(() -> UrlUtils.resolveUrl(completionEndpoint));
        }
    }
}
