# Continue Beans ☕️🚀

Assistente de IA para Apache NetBeans, com foco em desenvolvimento assistido por IA local e compatível com APIs no estilo OpenAI.

## Estado atual

![CI/CD](https://github.com/offline0x33/continue-netbeans/actions/workflows/ci.yml/badge.svg)

O `main` mantém uma cadeia de validação contínua para Java 11, 17 e 21, testes automatizados, build do módulo NBM, cobertura e análise de qualidade.

### O que já está funcionando

- Chat integrado ao NetBeans com interface dedicada.
- Integração principal com LM Studio por API compatível com OpenAI.
- Streaming de respostas para o painel do NetBeans.
- Seleção de modelo e modos `Ask`, `Code` e `Planning`.
- Inclusão de código selecionado do editor como contexto.
- Aplicação ou inserção de blocos de código gerados pela IA, com confirmação do usuário.
- Descoberta de modelos disponíveis no servidor local.
- Gerenciamento de histórico de conversa com truncamento por limite de tokens.
- Histórico de conversa protegido contra acesso concorrente e exposição do estado interno.
- Suite de testes unitários com JUnit 5 e integração com Cucumber.
- Empacotamento como plugin NetBeans (`.nbm`).

## Provedores

A arquitetura usa a abstração `LlmProvider`, permitindo evoluir o suporte a outros backends sem acoplar a UI ao protocolo específico.

O fluxo principal atualmente utiliza `LmStudioProvider`. O projeto também foi estruturado para acomodar outros serviços compatíveis, mas essas integrações não devem ser consideradas equivalentes ao caminho principal até possuírem cobertura e validação próprias.

## Arquitetura resumida

```text
NetBeans UI
    |
    v
ContinueTopComponent / ProfessionalTopComponent
    |
    v
LlmClient
    |
    v
LlmProvider
    |
    +--> LmStudioProvider
    |
    v
ConversationManager
```

A camada de conversa mantém o contexto da sessão e aplica truncamento automático conforme o limite configurado.

## Build local

Requisitos:

- Java 11 ou superior para desenvolvimento e execução dos testes;
- Maven 3.9+;
- Apache NetBeans compatível com a plataforma `RELEASE200` usada pelo projeto;
- LM Studio ou outro servidor compatível com a API configurada para recursos de IA.

Execute:

```bash
mvn clean install
```

Para a validação completa usada no CI:

```bash
mvn clean verify
```

O projeto produz o plugin NetBeans em `target/*.nbm`.

## CI/CD

O pipeline oficial executa:

```text
Java 11 ──┐
Java 17 ──┼──> testes ──> build ──> NBM
Java 21 ──┘                  |
                             +--> cobertura
                             +--> qualidade
                             +--> release
```

A matriz atual cobre Java 11, 17 e 21. O pipeline também verifica a existência do artefato NBM antes de publicar releases.

## Qualidade e testes

Os testes usam JUnit 5, Mockito e Cucumber/JUnit Platform. A configuração de JaCoCo mantém a verificação de cobertura integrada ao ciclo `verify`, com exclusões explícitas para áreas que ainda não fazem parte do núcleo coberto.

As correções recentes também endureceram o histórico de conversas contra concorrência e adicionaram testes de regressão para esse comportamento.

## Interface

O projeto possui uma interface principal baseada no `ProfessionalTopComponent`/`ProfessionalChatPanel` e componentes legados que ainda fazem parte da base de código. A consolidação desses caminhos deve ser tratada como evolução arquitetural, não como funcionalidade já concluída.

## Roadmap técnico

- Consolidar os caminhos de UI duplicados sem quebrar compatibilidade do módulo.
- Reduzir código de scaffolding e funcionalidades não conectadas ao fluxo principal.
- Expandir a cobertura dos provedores e integrações MCP com testes de comportamento reais.
- Melhorar cancelamento e ciclo de vida de operações assíncronas do streaming.
- Fortalecer análise estática e documentação de decisões arquiteturais.

## Licença

Este projeto segue a licença Apache 2.0.
