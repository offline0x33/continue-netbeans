package com.bajinho.continuebeans;

/**
 * User-facing agent modes that shape system prompts and execution path.
 *
 * <ul>
 *   <li>{@link #DOCS} / {@link #PLANNING} — always direct chat (no task graph)</li>
 *   <li>{@link #CODE} — hybrid: chat by default, task graph only for clear engineering intents</li>
 *   <li>{@link #AGENT} — task graph for any non-greeting / non-informational request</li>
 * </ul>
 */
public enum AgentMode {
    CODE("Code",
            "Foque em código limpo, correto e pronto para produção. Responda de forma direta com exemplos de código quando útil.",
            RoutingPolicy.HYBRID),
    PLANNING("Planning",
            "Você está em modo planejamento. NÃO execute mudanças nem invoque ferramentas de escrita. "
                    + "Entregue um plano estruturado (passos, riscos, arquivos tocados, critérios de aceite). "
                    + "Só descreva o que seria feito.",
            RoutingPolicy.CHAT_ONLY),
    DOCS("Docs",
            "Foque em documentação clara, Javadoc, README e explicações didáticas. "
                    + "Não planeje tarefas de engenharia nem altere arquivos — explique e documente.",
            RoutingPolicy.CHAT_ONLY),
    AGENT("Agent",
            "Atue como agente de engenharia autônomo: planeje, execute ferramentas, verifique e só declare DONE quando o critério for atendido.",
            RoutingPolicy.TASK_FIRST);

    public enum RoutingPolicy {
        /** Never enter TaskOrchestrator. */
        CHAT_ONLY,
        /** Task graph only when intent classifier says so. */
        HYBRID,
        /** Task graph for any non-informational request. */
        TASK_FIRST
    }

    private final String label;
    private final String systemHint;
    private final RoutingPolicy routingPolicy;

    AgentMode(String label, String systemHint, RoutingPolicy routingPolicy) {
        this.label = label;
        this.systemHint = systemHint;
        this.routingPolicy = routingPolicy;
    }

    public String getLabel() {
        return label;
    }

    public String getSystemHint() {
        return systemHint;
    }

    public RoutingPolicy getRoutingPolicy() {
        return routingPolicy;
    }

    public boolean isChatOnly() {
        return routingPolicy == RoutingPolicy.CHAT_ONLY;
    }

    public boolean prefersTaskGraph() {
        return routingPolicy == RoutingPolicy.TASK_FIRST;
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
