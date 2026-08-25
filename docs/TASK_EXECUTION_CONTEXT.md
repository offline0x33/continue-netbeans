# Task Execution Context

Cada plano possui um `TaskExecutionContext` compartilhado durante toda a execução.

Ele registra resultados verificados e falhas das tarefas para que tarefas posteriores e o replanejamento recebam contexto real do que já aconteceu.

Fluxo:

```text
Goal
 ↓
Task A
 ↓ DONE
Execution Context
 ↓
Task B recebe contexto de A
 ↓
Verification
 ↓
Execution Context
 ↓
Task C / Replanning
```

O contexto é memória do plano, não memória global da aplicação. Uma nova solicitação inicia um novo contexto.
