package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class LlmClientIntentRoutingMatrixTest {
    private final LlmClient client = new LlmClient();

    @ParameterizedTest
    @MethodSource("conversationalMessages")
    void conversationalMessagesStayConversational(String message) {
        assertFalse(client.shouldUseTaskOrchestrator(message));
    }

    @ParameterizedTest
    @MethodSource("engineeringMessages")
    void engineeringMessagesUseTaskOrchestrator(String message) {
        assertTrue(client.shouldUseTaskOrchestrator(message));
    }

    static Stream<Arguments> conversationalMessages() {
        return Stream.of(
                Arguments.of("olá"), Arguments.of("oi"), Arguments.of("hello"),
                Arguments.of("bom dia"), Arguments.of("como você está?"),
                Arguments.of("me fale desse projeto"), Arguments.of("fale sobre o projeto"),
                Arguments.of("o que é o workspace?"), Arguments.of("descreva o workspace"),
                Arguments.of("explique dependency injection"), Arguments.of("o que é Java?"),
                Arguments.of("quem é você?"), Arguments.of("para que serve Maven?"),
                Arguments.of("me conte sobre o código"), Arguments.of("qual a finalidade do projeto?"));
    }

    static Stream<Arguments> engineeringMessages() {
        return Stream.of(
                Arguments.of("corrija o pom.xml"), Arguments.of("crie uma classe User"),
                Arguments.of("implemente o serviço de login"), Arguments.of("adicione a dependência"),
                Arguments.of("analise o projeto e liste módulos"), Arguments.of("refactor o método X"),
                Arguments.of("fix the failing test"), Arguments.of("build o projeto"),
                Arguments.of("teste a API"), Arguments.of("execute os testes"),
                Arguments.of("leia /home/user/proj/Foo.java"), Arguments.of("@file:src/Main.java explique"),
                Arguments.of("@codebase encontre a configuração"), Arguments.of("git status e corrija"),
                Arguments.of("gere documentação"), Arguments.of("delete o arquivo temporário"),
                Arguments.of("mova a classe para outro pacote"), Arguments.of("configure o workspace"));
    }
}
