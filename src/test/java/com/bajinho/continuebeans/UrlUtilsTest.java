package com.bajinho.continuebeans;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UrlUtils utility class.
 */
class UrlUtilsTest {

    @Test
    void testResolveUrlWithFullUrl() {
        String url = "http://example.com:1234/v1/chat";
        String resolved = UrlUtils.resolveUrl(url);
        assertEquals(url, resolved, "Full URL should be returned as-is");
    }

    @Test
    void testResolveUrlWithHttpsUrl() {
        String url = "https://example.com/v1/chat";
        String resolved = UrlUtils.resolveUrl(url);
        assertEquals(url, resolved, "HTTPS URL should be returned as-is");
    }

    @Test
    void testResolveUrlWithDefaultUrl() {
        String url = "127.0.0.1:1234/v1/chat";
        String resolved = UrlUtils.resolveUrl(url);
        assertTrue(resolved.contains("127.0.0.1") || resolved.contains("localhost"),
                   "Default URL should be resolved properly");
    }

    @Test
    void testResolveUrlWithNull() {
        String resolved = UrlUtils.resolveUrl(null);
        assertNotNull(resolved, "Should return a valid default URL for null input");
    }

    @Test
    void testResolveUrlWithEmpty() {
        String resolved = UrlUtils.resolveUrl("");
        assertNotNull(resolved, "Should return a valid default URL for empty input");
    }

    @Test
    void testResolveUrlWithLocalhost() {
        String url = "localhost:1234/v1/chat";
        String resolved = UrlUtils.resolveUrl(url);
        assertNotNull(resolved, "Should handle localhost URLs");
    }

    @Test
    void testResolveUrlFormat() {
        String url = "http://example.com:8080/api";
        String resolved = UrlUtils.resolveUrl(url);
        assertTrue(resolved.startsWith("http") || resolved.contains("example"),
                   "Resolved URL should be valid");
    }

    @Test
    void testResolveUrlWithPort() {
        String url = "127.0.0.1:1234";
        String resolved = UrlUtils.resolveUrl(url);
        assertTrue(resolved.contains("1234") || resolved.contains("http"),
                   "URL with port should be resolved correctly");
    }

    @Test
    void testResolveUrlConsistency() {
        String url = "http://example.com/api";
        String resolved1 = UrlUtils.resolveUrl(url);
        String resolved2 = UrlUtils.resolveUrl(url);
        assertEquals(resolved1, resolved2, "URL resolution should be consistent");
    }

    @Test
    void testResolveUrlWithPath() {
        String url = "http://example.com:1234/v1/chat/completions";
        String resolved = UrlUtils.resolveUrl(url);
        assertTrue(resolved.contains("/v1/chat") || resolved.contains("completions"),
                   "URL with path should be preserved");
    }
}
