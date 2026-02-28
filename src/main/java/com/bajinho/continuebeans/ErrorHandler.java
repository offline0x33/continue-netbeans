package com.bajinho.continuebeans;

/**
 * Centralized error handling strategy for HTTP operations.
 * Follows enterprise-grade resilience patterns with automatic retries.
 */
public class ErrorHandler {

    private static final int MAX_RETRIES = 2;
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * Checks if an error is retryable.
     * Retryable: 429 (rate limit), 5xx (server error), network timeouts
     */
    public static boolean isRetryable(int statusCode, Throwable cause) {
        if (statusCode == 429 || statusCode >= 500) {
            return true;
        }

        if (cause instanceof java.net.http.HttpTimeoutException
                || cause instanceof java.net.SocketTimeoutException
                || cause instanceof java.io.IOException && cause.getMessage() != null
                        && cause.getMessage().contains("Connection reset")) {
            return true;
        }

        return false;
    }

    /**
     * Formats a user-friendly error message from HTTP status code.
     */
    public static String formatErrorMessage(int statusCode, String url) {
        switch (statusCode) {
            case 400:
                return "Erro HTTP 400: Requisição mal formatada. Verifique a configuração da URL.";
            case 401:
                return "Erro HTTP 401: Autenticação necessária. Configure suas credenciais.";
            case 403:
                return "Erro HTTP 403: Acesso negado ao endpoint.";
            case 404:
                return "Erro HTTP 404: Endpoint não encontrado. Verifique a URL: " + url;
            case 429:
                return "Erro HTTP 429: Limite de requisições excedido. Tentando novamente...";
            case 500:
                return "Erro HTTP 500: Erro interno do servidor LM Studio.";
            case 502:
                return "Erro HTTP 502: Gateway indisponível. Verifique se o LM Studio está rodando.";
            case 503:
                return "Erro HTTP 503: Serviço temporariamente indisponível.";
            case 504:
                return "Erro HTTP 504: Timeout no gateway.";
            default:
                return "Erro HTTP " + statusCode + " em " + url;
        }
    }

    /**
     * Formats timeout error message.
     */
    public static String formatTimeoutMessage() {
        return "Timeout: O servidor LM Studio não respondeu a tempo. Verifique a conexão de rede.";
    }

    /**
     * Formats network error message.
     */
    public static String formatNetworkError(String message) {
        return "Erro de conexão: " + (message != null ? message
                : "Não foi possível conectar ao servidor LM Studio.");
    }

    /**
     * Gets retry delay in milliseconds based on retry count.
     * Implements exponential backoff: 1s, 2s, 4s...
     */
    public static long getRetryDelay(int retryAttempt) {
        return RETRY_DELAY_MS * (long) Math.pow(2, retryAttempt - 1);
    }

    /**
     * Checks if we've exhausted retries.
     */
    public static boolean hasExhaustedRetries(int retryAttempt) {
        return retryAttempt > MAX_RETRIES;
    }
}
