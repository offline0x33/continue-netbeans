# Continue Beans ☕️🚀

![Continue Beans Logo](https://github.com/offline0x33/continue-netbeans/blob/main/continue_beans_logo.png)

[![NetBeans Version](https://img.shields.io/badge/NetBeans-12.0+-blue.svg?style=for-the-badge&logo=apachenetbeans)](https://netbeans.apache.org/)
[![Java Version](https://img.shields.io/badge/Java-11+-orange.svg?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey?style=for-the-badge)](https://github.com/bajinho/continue-netbeans)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)](LICENSE)

**Continue Beans** é o assistente definitivo de IA para desenvolvedores NetBeans. Integre o poder do **LM Studio** e de outros servidores compatíveis com a API OpenAI diretamente no seu fluxo de trabalho, trazendo autocompletar inteligente, refatoração de código e planejamento de arquitetura para dentro da sua IDE favorita.

---

## ✨ Funcionalidades Premium

- ⚡️ **Chat em Tempo Real (Streaming):** Respostas instantâneas com visualização dinâmica de código.
- 🛠 **Ações Diretas no Editor:** Substitua seleções ou insira código gerado pela IA com apenas um clique.
- 🧠 **Contexto Inteligente:** Use comandos como `@file` e `@codebase` para alimentar a IA com arquivos locais.
- 🌐 **Descoberta de Modelos:** Detecção automática de modelos rodando no LM Studio (v1 e api/v1).
- 🔄 **Resiliência Integrada:** Retry automático para erros de Rate Limit (429) e falhas de rede.
- 🎨 **Markdown Nativo:** Visualização elegante de explicações e blocos de código com syntax highlighting.

---

## 📸 Interface do Usuário

![Chat UI Mockup](https://github.com/offline0x33/continue-netbeans/blob/main/chat_ui_mockup.png)

*Interface moderna e integrada perfeitamente ao ecossistema NetBeans.*

---

## 🚀 Como Começar

### Pré-requisitos
1. **NetBeans:** Versão 12.0 ou superior (Recomendado: NetBeans 20+).
2. **Java:** JDK 11 ou superior.
3. **LM Studio:** Servidor local ativo (ou qualquer API compatível com OpenAI).

### Instalação
1. Baixe o arquivo `.nbm` da aba de [Releases](https://github.com/bajinho/continue-netbeans/releases).
2. No NetBeans, vá em `Tools` > `Plugins`.
3. Clique na aba `Downloaded` > `Add Plugins...` e selecione o arquivo baixado.
4. Reinicie o NetBeans se solicitado.

### Configuração
1. Abra as opções em `Tools` > `Options` > `Miscellaneous` > `Continue Beans`.
2. Configure a **URL da API** (Padrão: `http://127.0.0.1:1234/v1/chat/completions`).
3. (Opcional) Ajuste a **Temperatura** e o **Modelo Padrão**.

---

## ⌨️ Comandos de Contexto

Potencialize suas perguntas usando o sistema de indexação rápida:

| Comando | Descrição | Exemplo |
| :--- | :--- | :--- |
| `@file:nome` | Adiciona o conteúdo de um arquivo específico ao prompt. | `Como refatorar o @file:LlmClient.java?` |
| `@codebase` | Escaneia o projeto atual e gera um resumo inteligente do contexto. | `@codebase explique a arquitetura deste projeto.` |

### Recursos Avançados de `@codebase`
- ✅ **Suporte a .gitignore:** Respeita automaticamente padrões de exclusão (node_modules, .git, build, etc).
- 📏 **Limite de Profundidade:** Máximo 5 níveis de diretórios para evitar contexto excessivo.
- 🔢 **Limite de Arquivos:** Escaneia no máximo 50 arquivos por requisição.
- 🛡️ **Smart Truncation:** Se o contexto exceder 4000 caracteres, é truncado com nota `[Contexto Truncado]`.

---

## 💬 Conversas Multi-Turn

O plugin mantém automaticamente o histórico de conversas entre mensagens:

- **Histórico Persistente:** Cada mensagem (user/assistant) é armazenada na sessão.
- **Truncação Inteligente de Tokens:** Quando o histórico ultrapassa 4000 tokens, mensagens antigas são removidas automaticamente.
- **Limite de Contexto Configurável:** Padrão 4000 tokens; ajustável nas configurações.

**Exemplo:**
```
User: Explique a função loadModel()
Assistant: A função loadModel() carrega um modelo LLM no LM Studio...
User: Como ela trata erros?
Assistant: Ela usa ConversationManager para manter contexto...
```

---

## 🔄 Resiliência e Tratamento de Erros

O sistema foi projetado para ser robusto em produção:

- **Retry Automático:** Erros 429 (Rate Limit) disparam retry com backoff exponencial.
- **Timeouts Amigos:** Mensagens de erro claras quando a API demora mais de 5 minutos.
- **Validação de Payloads:** Todas as requisições são validadas antes do envio.
- **Fragmentação de Stream:** Suporte a parsing incremental de respostas JSON.

---

## 📊 Requisitos de Teste

Todos os recursos são cobertos por testes:

| Componente | Testes Unitários | Testes BDD (Cucumber) |
| :--- | :--- | :--- |
| `LmStudioProvider` | ✅ 3 tests | ✅ 4 scenarios |
| `ConversationManager` | ✅ 14 tests | ✅ 1 scenario |
| `ErrorHandler` | ✅ 9 tests | ✅ 3 scenarios |
| `CodebaseIndexer` | ✅ 6 tests | ✅ 2 scenarios |
| `PayloadValidator` | ✅ 13 tests | ✅ 2 scenarios |
| **TOTAL** | **✅ 45 tests** | **✅ 22 scenarios** |

---

## 📜 Retrocompatibilidade e Suporte

| Recurso | Suporte e Versões |
| :--- | :--- |
| **NetBeans Support** | Suporta NetBeans 12, 13, 14, 15, 16, 17, 18, 19 e **20 (Full Support)**. |
| **Java Support** | Desenvolvido com Java 11; compatível com Java 17, 21 e superiores. |
| **LM Studio** | Suporte total para versões legadas (`/api/v1`) e modernas (`/v1`). |
| **Outros Provedores** | Compatível com LocalAI, Ollama (via proxy OpenAI) e GPT-4 local. |

---

## 🛠 Desenvolvimento e Testes

O projeto utiliza **BDD (Behavior-Driven Development)** para garantir a qualidade.

```bash
# Rodar todos os testes
mvn test

# Gerar arquivo NBM para distribuição
mvn install
```

---

## 🤝 Contribuição

Contribuições são bem-vindas! Se você encontrou um bug ou tem uma ideia para uma nova funcionalidade, abra uma [Issue](https://github.com/bajinho/continue-netbeans/issues) ou envie um Pull Request.

**Feito com ❤️ por [offline0x33](https://github.com/bajinho)**
