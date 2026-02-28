package com.bajinho.continuebeans;

public class UrlUtils {
    /**
     * Resolves localhost to 127.0.0.1 to avoid some Java networking issues with
     * dual-stack environments.
     */
    public static String resolveUrl(String url) {
        if (url == null) {
            return "";
        }
        if (url.contains("localhost")) {
            return url.replace("localhost", "127.0.0.1");
        }
        return url;
    }
}
