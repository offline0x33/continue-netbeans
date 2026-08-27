package com.bajinho.continuebeans.task;

import com.bajinho.continuebeans.AgentMode;
import com.bajinho.continuebeans.ContinueSettings;
import com.bajinho.continuebeans.ConversationManager;
import com.bajinho.continuebeans.LlmClient;
import com.bajinho.continuebeans.ai.AIToolCallingIntegration;
import java.util.Collections;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

/** Executes user goals as explicit tasks and refuses to finish before the plan is complete. */
public final class TaskOrchestrator {
    public interface Listener {
        void onPlanCreated(TaskPlan plan);
        void onTaskStarted(AgentTask task);
        void onTaskVerifying(AgentTask task);
        void onTaskCompleted(AgentTask task);
        void onTaskFailed(AgentTask task);
        void onReplanning(TaskPlan failedPlan);
        void onCompleted(TaskPlan plan);
        void onFailed(String message, TaskPlan plan);

        default void onConversationChunk(String chunk) {
        }
    }

    private static final int MAX_TASK_ATTEMPTS = 3;
    private static final int MAX_REPLANS = 2;
    private static final String NO_PROJECT_MESSAGE =
            "Nenhum projeto aberto no NetBeans. Abra um projeto (File → Open Project) para que eu possa "
                    + "analisar arquivos, executar tarefas de código ou falar sobre o workspace atual.";

    private final TaskPlanner planner;
    private final AIToolCallingIntegration executor;
    private final LlmClient intentClassifier;
    private final ConversationManager conversationManager;
    private final ProjectContext projectContext;

    public TaskOrchestrator() {
        this(new TaskPlanner(), new AIToolCallingIntegration(), new LlmClient(), new NetBeansProjectContext());
    }

    public TaskOrchestrator(TaskPlanner planner, AIToolCallingIntegration executor) {
        this(planner, executor, null, null);
    }

    TaskOrchestrator(TaskPlanner planner, AIToolCallingIntegration executor, LlmClient intentClassifier) {
        this(planner, executor, intentClassifier, new NetBeansProjectContext());
    }

    TaskOrchestrator(TaskPlanner planner, AIToolCallingIntegration executor,
            LlmClient intentClassifier, ProjectContext projectContext) {
        this.planner = planner;
        this.executor = executor;
        this.intentClassifier = intentClassifier;
        this.projectContext = projectContext;
        this.conversationManager = new ConversationManager();
    }

    public void refreshProjectContext() {
        if (projectContext == null) {
            return;
        }
        Optional<String> root = projectContext.currentProjectRoot();
        executor.setWorkspaceRoot(root.orElse(null));
    }

    public CompletableFuture<TaskPlan> executeGoal(String goal, String provider, Listener listener) {
        return CompletableFuture.supplyAsync(() -> {
            TaskPlan plan = null;
            String planningGoal = goal;
            int replans = 0;
            try {
                refreshProjectContext();
                AgentMode mode = ContinueSettings.getAgentMode();

                // Project-dependent requests are rejected before invoking any classifier/model when
                // there is no open project. This keeps failure deterministic and avoids network calls.
                if (requiresProjectContext(goal) && currentProjectRoot().isEmpty()) {
                    return failWithoutExecution(goal, listener, NO_PROJECT_MESSAGE);
                }

                // The two-argument constructor is the explicit task-execution path; a classifier,
                // when present, may still route conversational requests away from planning.
                boolean useTasks = intentClassifier == null
                        || intentClassifier.shouldUseTaskOrchestrator(goal, mode);
                if (!useTasks) {
                    return executeConversation(goal, provider, listener, mode);
                }

                while (replans <= MAX_REPLANS) {
                    plan = planner.createPlan(planningGoal);
                    listener.onPlanCreated(plan);
                    executePlan(plan, provider, listener);

                    if (plan.isComplete()) {
                        listener.onCompleted(plan);
                        return plan;
                    }

                    if (!plan.hasBlockedTask()) {
                        listener.onFailed("Plano não chegou a DONE apesar do loop de execução.", plan);
                        return plan;
                    }

                    if (isMissingProjectFailure(plan)
                            || (currentProjectRoot().isEmpty() && requiresProjectContext(goal))) {
                        listener.onFailed(NO_PROJECT_MESSAGE, plan);
                        return plan;
                    }

                    if (replans == MAX_REPLANS) {
                        listener.onFailed("Limite de replanejamentos atingido. "
                                + "Tente reformular o pedido ou abra um projeto no NetBeans.", plan);
                        return plan;
                    }

                    replans++;
                    listener.onReplanning(plan);
                    planningGoal = buildReplanGoal(goal, plan);
                }
                return plan;
            } catch (Exception e) {
                String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                listener.onFailed(message, plan);
                throw new RuntimeException(message, e);
            }
        });
    }

    private Optional<String> currentProjectRoot() {
        return projectContext == null ? Optional.empty() : projectContext.currentProjectRoot();
    }

    private TaskPlan failWithoutExecution(String goal, Listener listener, String message) {
        AgentTask contextTask = new AgentTask(
                "Contexto do projeto",
                goal,
                "Projeto NetBeans aberto e disponível para análise.",
                Collections.<String>emptyList());
        contextTask.block(message);
        TaskPlan plan = new TaskPlan(goal, Collections.singletonList(contextTask));
        listener.onPlanCreated(plan);
        listener.onTaskFailed(contextTask);
        listener.onFailed(message, plan);
        return plan;
    }

    private boolean requiresProjectContext(String goal) {
        if (goal == null) {
            return false;
        }
        String normalized = goal.toLowerCase(Locale.ROOT);
        return normalized.contains("projeto aberto")
                || normalized.contains("analisar o projeto")
                || normalized.contains("analise o projeto")
                || normalized.contains("analisar projeto")
                || normalized.contains("analise projeto")
                || normalized.contains("analyze the project")
                || normalized.contains("analyze project")
                || normalized.contains("código do projeto")
                || normalized.contains("codigo do projeto")
                || normalized.contains("desse projeto")
                || normalized.contains("deste projeto")
                || normalized.contains("sobre o projeto")
                || normalized.contains("do projeto")
                || normalized.contains("no projeto")
                || normalized.contains("projeto atual")
                || normalized.contains("este projeto")
                || normalized.contains("workspace")
                || normalized.contains("pom.xml")
                || normalized.contains("build.gradle")
                || normalized.contains("@codebase")
                || normalized.contains("@file:");
    }

    private boolean isMissingProjectFailure(TaskPlan plan) {
        if (plan == null) {
            return false;
        }
        for (AgentTask task : plan.getTasks()) {
            String error = task.getLastError();
            if (error == null) {
                continue;
            }
            String lower = error.toLowerCase(Locale.ROOT);
            if (lower.contains("nenhum projeto")
                    || lower.contains("no project")
                    || lower.contains("workspace root")
                    || lower.contains("projeto aberto")) {
                return true;
            }
        }
        return false;
    }

    private TaskPlan executeConversation(String message, String provider, Listener listener, AgentMode mode) {
        AgentTask responseTask = new AgentTask(
                "Assistant",
                message,
                "A resposta conversacional foi gerada pelo modelo.",
                Collections.<String>emptyList());
        TaskPlan plan = new TaskPlan(message, Collections.singletonList(responseTask));

        listener.onPlanCreated(plan);
        listener.onTaskStarted(responseTask);
        conversationManager.addMessage("user", message);

        if (intentClassifier == null) {
            AIToolCallingIntegration.AIResponse response = executor
                    .processRequestWithToolCalling(conversationManager.getMessagesArray(), provider)
                    .join();
            if ("error".equalsIgnoreCase(response.getType())) {
                String error = response.getContent() == null || response.getContent().isBlank()
                        ? "Falha ao gerar resposta conversacional."
                        : response.getContent();
                responseTask.fail(error);
                listener.onTaskFailed(responseTask);
                listener.onFailed(error, plan);
                return plan;
            }
            String content = response.getContent() == null || response.getContent().isBlank()
                    ? "Não consegui gerar uma resposta neste momento."
                    : response.getContent();
            conversationManager.addMessage("assistant", content);
            responseTask.complete(content);
            listener.onTaskCompleted(responseTask);
            listener.onCompleted(plan);
            return plan;
        }

        String modeLabel = mode == null ? ContinueSettings.getAgentMode().getLabel() : mode.getLabel();
        StringBuilder responseContent = new StringBuilder();
        CompletableFuture<Void> responseFuture = new CompletableFuture<>();
        intentClassifier.perguntarIAStreaming(
                "",
                message,
                ContinueSettings.getModel(),
                modeLabel,
                chunk -> {
                    if (chunk != null && !chunk.isEmpty()) {
                        responseContent.append(chunk);
                        listener.onConversationChunk(chunk);
                    }
                },
                responseFuture::completeExceptionally,
                () -> responseFuture.complete(null));

        try {
            responseFuture.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            String error = cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage();
            responseTask.fail(error);
            listener.onTaskFailed(responseTask);
            listener.onFailed(error, plan);
            return plan;
        }

        String response = responseContent.toString();
        if (response.isBlank()) {
            String error = "O modelo não retornou conteúdo.";
            responseTask.fail(error);
            listener.onTaskFailed(responseTask);
            listener.onFailed(error, plan);
            return plan;
        }

        conversationManager.addMessage("assistant", response);
        responseTask.complete(response);
        listener.onTaskCompleted(responseTask);
        listener.onCompleted(plan);
        return plan;
    }

    private void executePlan(TaskPlan plan, String provider, Listener listener) {
        while (!plan.isComplete()) {
            AgentTask task = plan.nextRunnableTask();
            if (task == null) {
                if (plan.hasBlockedTask()) {
                    return;
                }
                throw new IllegalStateException("Nenhuma tarefa executável restante; plano inconsistente.");
            }

            boolean completed = false;
            while (!completed && task.getAttempts() < MAX_TASK_ATTEMPTS) {
                task.start();
                listener.onTaskStarted(task);

                AIToolCallingIntegration.AIResponse response = executor
                        .processRequestWithToolCalling(buildTaskPrompt(plan, task), provider)
                        .join();
                if (response == null || "error".equalsIgnoreCase(response.getType())) {
                    String error = response == null || response.getContent() == null || response.getContent().isBlank()
                            ? "Falha ao executar a tarefa (resposta vazia do provedor AI)."
                            : response.getContent();
                    task.fail(error);
                    listener.onTaskFailed(task);
                    if (isHardFailure(error)) {
                        task.block(error);
                        return;
                    }
                    continue;
                }

                String executionResult = response.getContent() == null ? "" : response.getContent();
                task.verifying(executionResult);
                listener.onTaskVerifying(task);
                AIToolCallingIntegration.AIResponse verification = executor
                        .processRequestWithToolCalling(buildVerificationPrompt(plan, task, executionResult), provider)
                        .join();
                String verdict = verification == null || verification.getContent() == null
                        ? ""
                        : verification.getContent().trim();

                if (isDoneVerdict(verdict)) {
                    task.complete(executionResult);
                    listener.onTaskCompleted(task);
                    completed = true;
                } else {
                    String failMsg = verdict.isBlank()
                            ? "Verificação falhou: o verificador não retornou DONE."
                            : "Verificação falhou: " + truncate(verdict, 500);
                    task.fail(failMsg);
                    listener.onTaskFailed(task);
                }
            }

            if (!completed) {
                String blockReason = task.getLastError() == null || task.getLastError().isBlank()
                        ? "Não foi possível concluir a tarefa após " + MAX_TASK_ATTEMPTS + " tentativas."
                        : "Não foi possível concluir a tarefa após " + MAX_TASK_ATTEMPTS
                                + " tentativas. Último erro: " + task.getLastError();
                task.block(blockReason);
                return;
            }
        }
    }

    private boolean isHardFailure(String error) {
        if (error == null) {
            return false;
        }
        String lower = error.toLowerCase(Locale.ROOT);
        return lower.contains("nenhum projeto")
                || lower.contains("no project")
                || lower.contains("url da api")
                || lower.contains("modelo ai não configurado")
                || lower.contains("não configurado");
    }

    private String buildReplanGoal(String originalGoal, TaskPlan failedPlan) {
        StringBuilder context = new StringBuilder();
        context.append(originalGoal).append("\n\n")
                .append("CONTEXTO OBRIGATÓRIO DE REPLANEJAMENTO:\n")
                .append("O plano anterior não foi concluído. Não repita cegamente as mesmas tarefas. "
                        + "Analise as falhas abaixo e crie uma estratégia corrigida.\n");
        for (AgentTask task : failedPlan.getTasks()) {
            if (task.getStatus() == TaskStatus.FAILED || task.getStatus() == TaskStatus.BLOCKED) {
                context.append("- Tarefa: ").append(task.getTitle())
                        .append("; tentativas: ").append(task.getAttempts())
                        .append("; erro: ").append(task.getLastError()).append('\n');
            }
        }
        context.append("Inclua explicitamente uma tarefa de verificação que prove a correção da falha.");
        return context.toString();
    }

    private String buildTaskPrompt(TaskPlan plan, AgentTask task) {
        return "Você está executando uma tarefa dentro de um plano maior.\n"
                + "NÃO declare o objetivo geral concluído.\n"
                + "Trabalhe somente na tarefa atual e use as ferramentas disponíveis.\n\n"
                + "OBJETIVO GERAL:\n" + plan.getGoal() + "\n\n"
                + "TAREFA:\n" + task.getTitle() + "\n\n"
                + "INSTRUÇÃO:\n" + task.getInstruction() + "\n\n"
                + "CRITÉRIO DE CONCLUSÃO:\n" + task.getCompletionCriteria() + "\n\n"
                + "Execute as mudanças necessárias e informe exatamente o que foi feito.";
    }

    private String buildVerificationPrompt(TaskPlan plan, AgentTask task, String executionResult) {
        return "Você é o verificador de uma tarefa de engenharia.\n"
                + "Inspecione o projeto com as ferramentas disponíveis quando necessário.\n"
                + "Responda obrigatoriamente começando por DONE ou NOT_DONE.\n\n"
                + "OBJETIVO:\n" + plan.getGoal() + "\n\n"
                + "TAREFA:\n" + task.getTitle() + "\n\n"
                + "CRITÉRIO:\n" + task.getCompletionCriteria() + "\n\n"
                + "RESULTADO INFORMADO PELO AGENTE:\n" + executionResult;
    }

    private boolean isDoneVerdict(String verdict) {
        return verdict.equalsIgnoreCase("DONE") || verdict.toUpperCase().startsWith("DONE:");
    }

    private String truncate(String value, int limit) {
        if (value == null || value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit) + "...";
    }
}
