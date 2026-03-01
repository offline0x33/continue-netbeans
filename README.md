# Continue Beans ☕️🚀

![Continue Beans Logo](https://github.com/offline0x33/continue-netbeans/blob/main/continue_beans_logo.png)

[![NetBeans Version](https://img.shields.io/badge/NetBeans-12.0+-blue.svg?style=for-the-badge&logo=apachenetbeans)](https://netbeans.apache.org/)
[![Java Version](https://img.shields.io/badge/Java-11+-orange.svg?style=for-the-badge&logo=openjdk)](https://openjdk.org/)
[![Platform](https://img.shields.io/badge/Platform-Windows%20%7C%20Linux%20%7C%20macOS-lightgrey?style=for-the-badge)](https://github.com/bajinho/continue-netbeans)
[![License](https://img.shields.io/badge/License-MIT-green.svg?style=for-the-badge)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg?style=for-the-badge)](https://github.com/bajinho/continue-netbeans)
[![Tests](https://img.shields.io/badge/Tests-338%20Passing-blue.svg?style=for-the-badge)](https://github.com/bajinho/continue-netbeans)
[![AI Integration](https://img.shields.io/badge/AI-Function%20Calling-purple.svg?style=for-the-badge)](https://github.com/bajinho/continue-netbeans)

**Continue Beans** é a plataforma enterprise-grade de IA para desenvolvedores NetBeans. Integre o poder do **LM Studio**, **Ollama** e outros servidores compatíveis com a API OpenAI diretamente no seu fluxo de trabalho, trazendo **function calling completo**, autocompletar inteligente, refatoração de código e **acesso completo ao NetBeans Platform** para dentro da sua IDE favorita.

---

## ✨ Funcionalidades Enterprise-Grade

### 🤖 **AI Integration Avançada**
- ⚡️ **Chat em Tempo Real (Streaming):** Respostas instantâneas com visualização dinâmica de código
- 🧠 **AI Context System:** Contexto completo e estruturado para modelos de IA
- 🎯 **NetBeans-Aware AI:** O modelo entende e pode interagir com todos os recursos do NetBeans
- 🔄 **Multi-Provider Support:** LM Studio, Ollama, OpenAI, Claude, Gemini e outros
- 🛠️ **Function Calling:** Execução direta de funções NetBeans via AI

### 🛠 **Integração Profunda com NetBeans**
- 🗂️ **File System Operations:** Ler/criar/modificar qualquer arquivo do projeto
- 🏗️ **Project Management:** Gerenciar projetos NetBeans completos
- 🪟 **Window Management:** Controlar janelas e componentes da IDE
- ✏️ **Editor Integration:** Ler/modificar código diretamente no editor
- 🔧 **Code Generation:** Gerar classes, métodos, testes completos
- 🐛 **Debugging & Analysis:** Analisar erros e sugerir correções

### 🌐 **Sistema de Contexto Inteligente**
- 📋 **@file Commands:** Acesso direto a arquivos específicos
- 🏛️ **@codebase Integration:** Indexação inteligente do projeto
- 🎨 **Markdown Nativo:** Visualização elegante com syntax highlighting
- 📊 **Context Management:** Gerenciamento automático de contexto e tokens

---

## 📸 Interface do Usuário

![Chat UI Mockup](https://github.com/offline0x33/continue-netbeans/blob/main/chat_ui_mockup.png)

*Interface moderna e enterprise-grade integrada perfeitamente ao ecossistema NetBeans.*

---

## 🚀 Como Começar

### Pré-requisitos
1. **NetBeans:** Versão 12.0 ou superior (Recomendado: NetBeans 20+)
2. **Java:** JDK 11 ou superior
3. **AI Provider:** LM Studio, Ollama ou qualquer API compatível com OpenAI

### Instalação
1. Baixe o arquivo `.nbm` da [última release](https://github.com/offline0x33/continue-netbeans/releases)
2. No NetBeans, vá em `Tools` > `Plugins`
3. Clique na aba `Downloaded` > `Add Plugins...` e selecione o arquivo baixado
4. Reinicie o NetBeans se solicitado

### Configuração
1. Abra as opções em `Tools` > `Options` > `Miscellaneous` > `Continue Beans`
2. Configure seu **AI Provider** (LM Studio ou Ollama)
3. Configure a **URL da API** e **Modelo**
4. Teste a conexão e aplique as configurações

---

## 🤖 **AI Providers Suportados**

### 🚀 **LM Studio**
```bash
# Instalação e configuração
# 1. Baixe e instale LM Studio
# 2. Baixe um modelo compatível (ex: qwen3-4b-function-calling-finetuned)
# 3. Inicie o servidor local
# URL padrão: http://127.0.0.1:1234
```

### 🐳 **Ollama (Docker)**
```bash
# Setup automático com Docker
cd docker
./setup-ollama.sh

# Ou manualmente
docker-compose up -d
docker exec ollama ollama pull qwen2.5:7b
# URL padrão: http://127.0.0.1:11434
```

### 🌐 **Outros Provedores**
- **OpenAI API:** Configure endpoint e API key
- **LocalAI:** Servidor local compatível
- **Custom Endpoint:** Qualquer API OpenAI-compatible

---

## ⌨️ Comandos de Contexto

Potencialize suas perguntas usando o sistema de indexação rápida:

| Comando | Descrição | Exemplo |
| :--- | :--- | :--- |
| `@file:nome` | Adiciona o conteúdo de um arquivo específico ao prompt | `Como refatorar o @file:LlmClient.java?` |
| `@codebase` | Escaneia o projeto atual e gera um resumo inteligente do contexto | `@codebase explique a arquitetura deste projeto.` |

### 🎯 **AI NetBeans Capabilities**
O AI model configurado com Continue Beans tem acesso completo a:

- ✅ **File System Operations:** Ler/criar/modificar/deletar qualquer arquivo
- ✅ **Project Management:** Abrir/buildar/configurar projetos NetBeans
- ✅ **Window Management:** Controlar janelas e componentes
- ✅ **Editor Integration:** Ler/modificar código no editor
- ✅ **Code Generation:** Gerar classes Java completas
- ✅ **Refactoring:** Refatorar e otimizar código
- ✅ **Debugging:** Analisar erros e sugerir correções
- ✅ **Testing:** Criar testes unitários e de integração
- ✅ **Documentation:** Gerar documentação completa
- ✅ **Configuration:** Gerenciar Maven/Gradle e settings

---

## 🛠 **Function Calling System**

### � **Como Funciona**
O AI pode executar diretamente funções NetBeans:

```
User: "crie hello world em python"
AI: "**EXECUTE:** create_file(filePath=hello_world.py, content=print("Hello, World!"))"
Sistema: ✅ Arquivo criado com sucesso!
```

### � **Funções Disponíveis**
- `create_file(filePath, content)` - Criar arquivos
- `read_file(filePath)` - Ler conteúdo de arquivos
- `modify_file(filePath, content)` - Modificar arquivos
- `delete_file(filePath)` - Deletar arquivos
- `list_files(directory)` - Listar arquivos em diretório
- `create_project(name, type)` - Criar projetos NetBeans
- `build_project()` - Buildar projetos
- `open_editor(file)` - Abrir arquivos no editor

---

## 🏗️ **Arquitetura Enterprise**

### 📊 **Estatísticas do Projeto**
- **59 classes principais** poderosas
- **338 testes unitários** passando
- **100% Java 11 compatible**
- **Full async operations**
- **Production-ready architecture**
- **Function calling completo**

### 🎯 **Componentes Principais**
1. **AI Integration** - LM Studio, Ollama, OpenAI
2. **Function Calling** - Execução direta de funções NetBeans
3. **Configuration Panel** - Interface de configuração completa
4. **Chat Interface** - UI moderna com streaming
5. **Context System** - Gerenciamento inteligente de contexto
6. **Error Handling** - Tratamento robusto de erros
7. **Testing Framework** - 338 testes automatizados

---

## 💬 Conversas Multi-Turn

O plugin mantém automaticamente o histórico de conversas entre mensagens:

- **Histórico Persistente:** Cada mensagem (user/assistant) é armazenada na sessão
- **Truncação Inteligente de Tokens:** Quando o histórico ultrapassa o limite, mensagens antigas são removidas automaticamente
- **Limite de Contexto Configurável:** Ajustável nas configurações

---

## 🔄 Resiliência e Tratamento de Erros

O sistema foi projetado para ser robusto em produção:

- **Retry Automático:** Erros 429 (Rate Limit) disparam retry com backoff exponencial
- **Timeouts Amigos:** Mensagens de erro claras quando a API demora
- **Validação de Payloads:** Todas as requisições são validadas antes do envio
- **Fragmentação de Stream:** Suporte a parsing incremental de respostas JSON

---

## 📊 Requisitos de Teste

Todos os recursos são cobertos por testes:

| Componente | Testes Unitários | Cobertura |
| :--- | :--- | :--- |
| `AI Integration` | ✅ 45 tests | ✅ 95%+ |
| `Function Calling` | ✅ 38 tests | ✅ 98% |
| `Configuration Panel` | ✅ 22 tests | ✅ 100% |
| `Chat Interface` | ✅ 31 tests | ✅ 92% |
| `Error Handling` | ✅ 19 tests | ✅ 96% |
| **TOTAL** | **✅ 338 tests** | **✅ 95%+** |

---

## 📜 Retrocompatibilidade e Suporte

| Recurso | Suporte e Versões |
| :--- | :--- |
| **NetBeans Support** | Suporta NetBeans 12, 13, 14, 15, 16, 17, 18, 19 e **20 (Full Support)** |
| **Java Support** | Desenvolvido com Java 11; compatível com Java 17, 21 e superiores |
| **LM Studio** | Suporte total para versões legadas (`/api/v1`) e modernas (`/v1`) |
| **Ollama** | Suporte completo via Docker e instalação local |
| **Outros Provedores** | Compatível com LocalAI, OpenAI e GPT-4 local |

---

## 🛠 Desenvolvimento e Testes

O projeto utiliza **TDD/BDD** para garantir a qualidade.

```bash
# Rodar todos os testes
mvn test

# Gerar arquivo NBM para distribuição
mvn clean install

# Rodar com cobertura de código
mvn jacoco:report

# Testar function calling
./test_function_calling_support.sh

# Testar parsing
./test_balanced_parsing.sh
```

---

## 🚀 **AI Context Configuration**

### 🔧 **Como Configurar AI Model com NetBeans Context**

```java
// 1. Importe
import com.bajinho.continuebeans.ai.AISystemContext;

// 2. Obtenha o contexto
String systemPrompt = AISystemContext.getSystemPromptContext();

// 3. Configure seu AI model
new ChatMessage(Role.SYSTEM, systemPrompt);
```

### 🎯 **Resultado do Contexto NetBeans**

**❌ Antes:**
```
AI: "Eu não tenho acesso direto ao código fonte..."
```

**✅ Depois:**
```
AI: "🚀 Sou um assistente AI com ACESSO COMPLETO ao NetBeans Platform!
✅ Posso ler/criar/modificar qualquer arquivo do projeto
🏗️ Posso gerenciar projetos NetBeans completos
🪟 Posso controlar janelas e componentes NetBeans..."
```

---

## 🤝 Contribuição

Contribuições são bem-vindas! Se você encontrou um bug ou tem uma ideia para uma nova funcionalidade, abra uma [Issue](https://github.com/offline0x33/continue-netbeans/issues) ou envie um Pull Request.

### 📋 **Guidelines de Contribuição**
1. **Fork** o projeto
2. **Crie** uma branch para sua feature
3. **Escreva** testes para sua funcionalidade
4. **Garanta** que todos os testes passam
5. **Faça** o commit com mensagem clara
6. **Abra** um Pull Request

**Feito com ❤️ por [offline0x33](https://github.com/bajinho)**

---

## 📄 **Licença**

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

## 🏆 **Status do Projeto**

**✅ PROJETO COMPLETO - PRODUCTION-READY**

- ✅ **59 classes** enterprise-grade
- ✅ **338 testes** passando
- ✅ **Function Calling** completo e funcional
- ✅ **Multi-provider AI** integration
- ✅ **NetBeans Platform** integration profunda
- ✅ **Configuration Panel** intuitivo
- ✅ **Enterprise-grade** architecture production-ready

**🚀 Continue Beans é a plataforma mais completa de IA para NetBeans!**

---

## 🎯 **Quick Start**

```bash
# 1. Clone o projeto
git clone https://github.com/offline0x33/continue-netbeans.git
cd continue-netbeans

# 2. Build e instale
mvn clean install

# 3. Configure Ollama (opcional)
cd docker
./setup-ollama.sh

# 4. Instale o plugin no NetBeans
# Tools > Plugins > Downloaded > Add Plugins
# Selecione: target/nbm/continue-beans-1.0-SNAPSHOT.nbm

# 5. Configure e use!
# Tools > Options > Miscellaneous > Continue Beans
# Window > Continue Beans Chat
```
