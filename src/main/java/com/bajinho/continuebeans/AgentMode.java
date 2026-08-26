package com.bajinho.continuebeans;

/** User-facing agent modes that shape system prompts and behavior. */
public enum AgentMode {
    CODE("Code", "Foque em código limpo, correto e pronto para produção."),
    PLANNING("Planning", "Planeje a solução em passos claros antes de codar."),
    DOCS("Docs", "Foque em documentação clara, Javadoc e explicações."),
    AGENT("Agent", "Atue como agente de engenharia: planeje, execute e verifique.");

    private final String label;
    private final String systemHint;

    AgentMode(String label, String systemHint) {
        this.label = label;
        this.systemHint = systemHint;
    }

    public String getLabel() {
        return label;
    }

    public String getSystemHint() {
        return systemHint;
    }

    public static AgentMode defaultMode() {
        return CODE;
    }

    public static AgentMode fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return defaultMode();
        }
        for (AgentMode mode : values()) {
            if (mode.label.equalsIgnoreCase(value.trim()) || mode.name().equalsIgnoreCase(value.trim())) {
                return mode;
            }
        }
        return defaultMode();
    }

    @Override
    public String toString() {
        return label;
    }
}
