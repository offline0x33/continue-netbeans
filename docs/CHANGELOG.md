# Changelog

Todos as mudanças notáveis do projeto **Continue Beans** serão documentadas neste arquivo.

## [1.0.0] - 2026-02-28

### ✨ Funcionalidades Adicionadas
- **Chat em Tempo Real (Streaming):** Integração completa com LM Studio via API OpenAI-compatible
- **Sistema de Contexto Inteligente:** Comandos `@file` e `@codebase` para enriquecer prompts
- **Gerenciamento de Conversas Multi-Turn:** ConversationManager com truncação inteligente de tokens
- **Validação de Payloads API:** PayloadValidator para garantir conformidade com contrato OpenAI
- **Tratamento Robusto de Erros:** ErrorHandler com retry automático para 429/5xx
- **Indexação Avançada:** CodebaseIndexer com suporte a .gitignore e limites de profundidade
- **UI Interativa:** ChatPanel em Swing com suporte a streaming em tempo real

### 🧪 Testes
- 45 testes unitários (JUnit 5 + Mockito)
- 22 testes BDD (Cucumber) cobrindo 8 features
- Cobertura de integração com LM Studio APIs

### 🔄 Suporte
- NetBeans 12.0 - 20.x
- Java 11, 17, 21+
- LM Studio (v0.x e v1.x)
- Compatível com LocalAI, Ollama (via proxy)

### 🐛 Correções
- Compilação com Java 11 (sem usar métodos Java 12+)
- Parsing correto de fragmentos JSON em streams
- Tratamento de redirects e timeouts HTTP
- Sincronização com EDT para operações assincronamente

### 📦 Build & CI/CD
- GitHub Actions para CI/CD automático
- Maven NBM plugin para geração de bundles NetBeans
- Artifact publishing em releases

---

## [0.9.0-beta] - 2026-02-01

### ✨ Funcionalidades Beta
- Conexão básica com LM Studio
- Chat simples (sem histórico)
- Descoberta de modelos
- Interface preliminar

### 🐛 Problemas Conhecidos
- Sem suporte multi-turn
- Histórico não persistente
- Validação limitada de payloads

---

## Formato de Versão

Este projeto segue [Semantic Versioning](https://semver.org/):
- **MAJOR:** Breaking changes na API ou funcionalidade
- **MINOR:** Novas funcionalidades compatíveis retroativamente
- **PATCH:** Correções de bugs
