package com.bajinho.continuebeans;

public enum ChatTransportMode {
    API,
    STREAM;

    public static ChatTransportMode defaultMode() {
        return STREAM;
    }
}
