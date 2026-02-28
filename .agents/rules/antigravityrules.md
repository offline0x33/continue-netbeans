---
trigger: always_on
---

# 🧠 Regras de Desenvolvimento - Continue Beans ☕️🚀

## (Arquiteto Senior | Expert em Plugins NetBeans & IA Local)

### 🔑 1. Arquitetura do Plugin (NetBeans Platform)

O foco é a extensibilidade da IDE. Mantemos a separação clara entre a UI (Swing/TopComponent) e a lógica de comunicação com o LLM:

* **Core:** Java 11 utilizando `org.openide.*` para integração com o sistema de arquivos e janelas.
* **UI:** Swing com `RSyntaxTextArea` para renderização de código e Markdown.
* **Comunicação:** `LlmClient` robusto, agnóstico de modelo, com suporte a streaming e retries.
* **DI/Lookup:** Uso extensivo de `org.openide.util.Lookup` para desacoplamento de serviços.

### 🧪 2. Qualidade, BDD e Testes (Obrigatório)

Não existe código pronto sem validação. O ciclo de vida de cada feature deve seguir:

* **Cucumber (BDD):** Cenários definidos em `.feature` testando o comportamento do assistente.
* **JUnit 5 / Mockito:** Testes unitários para parsers, lógica de context-window e manipulação de tokens.
* **NetBeans Test Harness:** Garantir que o plugin carrega e interage corretamente com o `Lookup` da IDE sem causar deadlocks na AWT Event Thread.

### 🔐 3. Versionamento e Fluxo Git (Lei do Projeto)

* **Git Push Obrigatório:** Cada sub-task ou issue finalizada exige um `git push`. Não trabalhamos com código represado localmente.
* **Pre-Push Check:** Antes do push, o comando `mvn clean install` deve passar. Isso garante que o bundle `.nbm` pode ser gerado e que os testes passaram.
* **Zero Regressão:** Se uma alteração quebrar o suporte ao `@codebase` ou à indexação de arquivos, o commit deve ser revertido imediatamente.

---

# ⚠️ REGRAS CRÍTICAS - CONTINUE BEANS SOC

### ❌ PROIBIÇÕES

1. **NÃO altere o `pom.xml` sem necessidade:** Especialmente as dependências do NetBeans API (`RELEASE200`). Mudanças de versão aqui podem quebrar a retrocompatibilidade com versões antigas do NetBeans.
2. **NÃO bloqueie a EDT (Event Dispatch Thread):** Chamadas ao LM Studio/API devem ser obrigatoriamente assíncronas (Threads separadas ou `CompletableFuture`).
3. **NÃO use bibliotecas pesadas de UI externas:** O plugin deve permanecer leve, priorizando componentes nativos do NetBeans ou `RSyntaxTextArea`.

### ✅ OBRIGAÇÕES

1. **Testes antes do Push:** Rodar a suíte Cucumber/JUnit. Código quebrado no repositório é considerado falha crítica.
2. **Documentação de Contexto:** Sempre que criar um novo comando `@comando`, atualize a tabela de comandos de contexto no README.
3. **Logs de Diagnóstico:** Use `org.openide.util.Exceptions` e o log do NetBeans para capturar erros de conexão com o LM Studio.

---

## 🛠️ Ferramentas de Teste no NetBeans para o Plugin

Para você que usa NetBeans, o fluxo de teste ideal integrado ao Maven é:

1. **Aba "Test Results":** O NetBeans exibe nativamente o progresso do JUnit 5. Mantenha-a aberta.
2. **Cucumber Integration:** O plugin que sugeri ("Cucumber Case") ajudará a ler os arquivos `.feature` que você já tem no `pom.xml`.
3. **Run with Visual VM:** Como estamos lidando com IA e streaming de texto, monitore o consumo de memória do plugin durante o desenvolvimento para evitar leaks na IDE do usuário.

