package com.bajinho.continuebeans;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LmStudioProviderLoadModelTest {

    @Test
    public void testLoadModelSuccess() throws Exception {
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));
        LmStudioProvider provider = new LmStudioProvider(mockClient, new Gson());
        assertTrue(provider.loadModel("modelA").join());
    }

    @Test
    public void testLoadModelFailure() throws Exception {
        HttpClient mockClient = mock(HttpClient.class);
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));
        LmStudioProvider provider = new LmStudioProvider(mockClient, new Gson());
        assertFalse(provider.loadModel("modelB").join());
    }
}
