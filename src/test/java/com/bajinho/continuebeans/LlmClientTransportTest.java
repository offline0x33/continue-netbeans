package com.bajinho.continuebeans;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LlmClientTransportTest {

    @AfterEach
    void restoreDefault() {
        ContinueSettings.setChatTransportMode(ChatTransportMode.STREAM);
    }

    @Test
    void apiModeUsesProviderAsk() throws Exception {
        ContinueSettings.setChatTransportMode(ChatTransportMode.API);
        LlmProvider provider = mock(LlmProvider.class);
        when(provider.ask(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture("api-response"));

        LlmClient client = clientWithProvider(provider);
        assertEquals("api-response", client.perguntarIAAsync("", "Olá", "model", "").join());
        verify(provider).ask("", "Olá", "model", "");
    }

    @Test
    void streamModeUsesProviderStream() throws Exception {
        ContinueSettings.setChatTransportMode(ChatTransportMode.STREAM);
        LlmProvider provider = mock(LlmProvider.class);
        org.mockito.Mockito.doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<String> chunk = invocation.getArgument(4);
            Runnable complete = invocation.getArgument(6);
            chunk.accept("stream-");
            chunk.accept("response");
            complete.run();
            return null;
        }).when(provider).stream(anyString(), anyString(), anyString(), anyString(), any(), any(), any());

        LlmClient client = clientWithProvider(provider);
        assertEquals("stream-response", client.perguntarIAAsync("", "Olá", "model", "").join());
        verify(provider).stream(anyString(), anyString(), anyString(), anyString(), any(), any(), any());
    }

    private LlmClient clientWithProvider(LlmProvider provider) throws Exception {
        LlmClient client = new LlmClient();
        Field providerField = LlmClient.class.getDeclaredField("provider");
        providerField.setAccessible(true);
        providerField.set(client, provider);
        return client;
    }
}
