package com.bajinho.continuebeans;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LmStudioProviderTest {

    @Test
    public void testListModelsFiltersLoadedInstances() throws Exception {
        // Mock HttpClient and HttpResponse
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        // JSON with loaded_instances for one model and none for another
        String json = "{\"data\":[{" +
                "\"id\":\"modelLoaded\",\"loaded_instances\":[{\"id\":\"instance1\"}]}," +
                "{\"id\":\"modelNotLoaded\"}]}";
        when(mockResponse.body()).thenReturn(json);
        when(mockClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        LmStudioProvider provider = new LmStudioProvider(mockClient, new Gson());
        List<String> models = provider.listModels().join();
        // Should contain only the loaded model
        assertEquals(1, models.size());
        assertTrue(models.contains("modelLoaded"));
        assertFalse(models.contains("modelNotLoaded"));
    }
}
