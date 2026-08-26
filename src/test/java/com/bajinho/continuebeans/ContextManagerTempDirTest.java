package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ContextManagerTempDirTest {

    @Test
    void plainPromptIncludesProjectContextAndStructure() throws Exception {
        Path root = Files.createTempDirectory("continue-context-");
        Files.writeString(root.resolve("README.md"), "hello context");

        String result = ContextManager.processContext("explique o projeto", root.toString());

        assertTrue(result.contains("Contexto: Você está assistindo no projeto '" + root.getFileName() + "'."));
        assertTrue(result.contains("README.md"));
    }

    @Test
    void explicitFileContextIncludesFileContent() throws Exception {
        Path root = Files.createTempDirectory("continue-context-");
        Path file = root.resolve("README.md");
        Files.writeString(file, "conteudo-do-arquivo");

        String result = ContextManager.processContext("@file:README.md explique", root.toString());

        assertTrue(result.contains("@file:README.md"));
        assertTrue(result.contains("conteudo-do-arquivo"));
    }

    @Test
    void missingWorkDirectoryDoesNotPreventPlainPrompt() {
        assertTrue(ContextManager.processContext("olá", null).contains("olá"));
    }
}
