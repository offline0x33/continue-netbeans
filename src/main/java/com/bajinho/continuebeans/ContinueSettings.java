package com.bajinho.continuebeans;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

public class ContinueSettings {

    private static final String PREF_API_URL = "apiUrl";
    private static final String PREF_MODEL = "model";
    private static final String PREF_TEMPERATURE = "temperature";
    private static final String PREF_AGENT_MODE = "agentMode";
    private static final String PREF_CHAT_TRANSPORT_MODE = "chatTransportMode";

    private static final String DEFAULT_API_URL = "http://127.0.0.1:1234/v1/chat/completions";
    private static final String DEFAULT_MODEL = "";
    private static final double DEFAULT_TEMPERATURE = 0.7;

    private static Preferences getPreferences() {
        return NbPreferences.forModule(ContinueSettings.class);
    }

    public static String getApiUrl() {
        return getPreferences().get(PREF_API_URL, DEFAULT_API_URL);
    }

    public static void setApiUrl(String apiUrl) {
        getPreferences().put(PREF_API_URL, apiUrl);
    }

    public static String getModel() {
        return getPreferences().get(PREF_MODEL, DEFAULT_MODEL);
    }

    public static void setModel(String model) {
        getPreferences().put(PREF_MODEL, model);
    }

    public static double getTemperature() {
        return getPreferences().getDouble(PREF_TEMPERATURE, DEFAULT_TEMPERATURE);
    }

    public static void setTemperature(double temperature) {
        getPreferences().putDouble(PREF_TEMPERATURE, temperature);
    }

    public static AgentMode getAgentMode() {
        String value = getPreferences().get(PREF_AGENT_MODE, AgentMode.defaultMode().name());
        try {
            return AgentMode.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return AgentMode.fromLabel(value);
        }
    }

    public static void setAgentMode(AgentMode mode) {
        AgentMode selected = mode == null ? AgentMode.defaultMode() : mode;
        getPreferences().put(PREF_AGENT_MODE, selected.name());
    }

    public static ChatTransportMode getChatTransportMode() {
        String value = getPreferences().get(PREF_CHAT_TRANSPORT_MODE, ChatTransportMode.defaultMode().name());
        try {
            return ChatTransportMode.valueOf(value);
        } catch (IllegalArgumentException ignored) {
            return ChatTransportMode.defaultMode();
        }
    }

    public static void setChatTransportMode(ChatTransportMode mode) {
        ChatTransportMode selected = mode == null ? ChatTransportMode.defaultMode() : mode;
        getPreferences().put(PREF_CHAT_TRANSPORT_MODE, selected.name());
    }
}
