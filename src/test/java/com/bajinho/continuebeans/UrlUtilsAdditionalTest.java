package com.bajinho.continuebeans;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional tests for UrlUtils to increase code coverage.
 */
class UrlUtilsAdditionalTest {

    @Test
    void testResolveUrlWithMultipleLocalhost() {
        String result = UrlUtils.resolveUrl("http://localhost:8080/localhost/test");
        assertEquals("http://127.0.0.1:8080/127.0.0.1/test", result);
    }

    @Test
    void testResolveUrlWithLocalhostAsPort() {
        String result = UrlUtils.resolveUrl("localhost");
        assertEquals("127.0.0.1", result);
    }

    @Test
    void testResolveUrlWithHttpLocalhostPort() {
        String result = UrlUtils.resolveUrl("http://localhost:1234");
        assertEquals("http://127.0.0.1:1234", result);
    }

    @Test
    void testResolveUrlWithHttpsLocalhostPort() {
        String result = UrlUtils.resolveUrl("https://localhost:1234");
        assertEquals("https://127.0.0.1:1234", result);
    }

    @Test
    void testResolveUrlWithLocalhostAndPath() {
        String result = UrlUtils.resolveUrl("http://localhost:1234/api/test");
        assertEquals("http://127.0.0.1:1234/api/test", result);
    }

    @Test
    void testResolveUrlWithoutLocalhost() {
        String result = UrlUtils.resolveUrl("http://example.com:8080");
        assertEquals("http://example.com:8080", result);
    }

    @Test
    void testResolveUrlWithIpAddress() {
        String result = UrlUtils.resolveUrl("http://192.168.1.1:8080");
        assertEquals("http://192.168.1.1:8080", result);
    }

    @Test
    void testResolveUrlWithNull() {
        String result = UrlUtils.resolveUrl(null);
        assertEquals("", result, "Should return empty string for null input");
    }

    @Test
    void testResolveUrlWithEmptyString() {
        String result = UrlUtils.resolveUrl("");
        assertEquals("", result, "Should return empty string");
    }

    @Test
    void testResolveUrlWithSpace() {
        String result = UrlUtils.resolveUrl("   ");
        assertEquals("   ", result, "Should preserve spaces");
    }

    @Test
    void testResolveUrlWithSpecialCharacters() {
        String result = UrlUtils.resolveUrl("http://localhost:8080/?key=value&test=123");
        assertTrue(result.contains("127.0.0.1"), "Should replace localhost");
        assertTrue(result.contains("key=value"), "Should preserve query params");
    }

    @Test
    void testResolveUrlLocalHostCaseSensitive() {
        String result = UrlUtils.resolveUrl("http://LOCALHOST:8080");
        // The method is case-sensitive, so it won't replace LOCALHOST
        assertEquals("http://LOCALHOST:8080", result);
    }

    @Test
    void testResolveUrlWithMultiplePorts() {
        String result = UrlUtils.resolveUrl("http://localhost:8080:localhost:9090");
        assertEquals("http://127.0.0.1:8080:127.0.0.1:9090", result);
    }

    @Test
    void testResolveUrlWithFragment() {
        String result = UrlUtils.resolveUrl("http://localhost:8080/path#section");
        assertEquals("http://127.0.0.1:8080/path#section", result);
    }

    @Test
    void testResolveUrlWithUserInfo() {
        String result = UrlUtils.resolveUrl("http://user:pass@localhost:8080");
        assertEquals("http://user:pass@127.0.0.1:8080", result);
    }
}
