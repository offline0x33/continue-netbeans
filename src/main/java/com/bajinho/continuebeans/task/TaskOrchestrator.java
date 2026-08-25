package com.bajinho.continuebeans.task;

import com.bajinho.continuebeans.ai.AIToolCallingIntegration;
import java.util.concurrent.CompletableFuture;

/**
 * Executes user goals as explicit tasks and refuses to finish before the plan is complete.
 */
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
    }

    private static final int MAX_TASK_ATTEMPTS = 3;
    private static final int MAX_REPLANS = 2;

    private final TaskPlanner planner;
    private final AIToolCallingIntegration executor;

    public TaskOrchestrator() {
        this(new TaskPlanner(), new AIToolCallingIntegration());
    }

    public TaskOrchestrator(TaskPlanner planner, AIToolCallingIntegration executor) {
        this.planner = planner;
        this.executor = executor;
    }

    public CompletableFuture<TaskPlan> executeGoal(String goal, String provider, Listener listener) {
        return CompletableFuture.supplyAsync(() -> {
            TaskPlan plan = null;
            int replans = 0;
            try {
                while (replans <= MAX_REPLANS) {
                    plan = planner.createPlan(goal);
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

                    if (replans == MAX_REPLANS) {
                        listener.onFailed("Limite de replanejamentos atingido.", plan);
                        return plan;
                    }

                    replans++;
                    listener.onReplanning(plan);
                }
                return plan;
            } catch (Exception e) {
                String message = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                listener.onFailed(message, plan);
                throw new RuntimeException(message, e);
            }
        });
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
                if ("error".equalsIgnoreCase(response.getType())) {
                    task.fail(response.getContent());
                    listener.onTaskFailed(task);
                    continue;
                }

                task.verifying(response.getContent());
                listener.onTaskVerifying(task);
                AIToolCallingIntegration.AIResponse verification = executor
                        .processRequestWithToolCalling(buildVerificationPrompt(plan, task, response.getContent()), provider)
                        .join();
                String verdict = verification.getContent() == null ? "" : verification.getContent().trim();

                if (isDoneVerdict(verdict)) {
                    task.complete(response.getContent());
                    listener.onTaskCompleted(task);
                    completed = true;
                } else {
                    task.fail("Verificação falhou: " + truncate(verdict, 500));
                    listener.onTaskFailed(task);
                }
            }

            if (!completed) {
                task.block("Não foi possível concluir a tarefa após " + MAX_TASK_ATTEMPTS + " tentativas.");
                return;
            }
        }
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
