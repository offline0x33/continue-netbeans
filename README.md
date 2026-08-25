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

### Especificação visual — Chat Dark Theme

A aparência do chat é parte importante do produto e deve ser tratada como um contrato visual. A especificação abaixo descreve a direção visual atual para implementação e reprodução no Figma ou em outra ferramenta UI/UX.

#### 🎨 System Tokens & Design System

##### Color Palette

| Name | Hex Value | Application |
|---|---|---|
| **Background Main** | `#121214` | Fundo geral da aplicação e da aba ativa |
| **Background Secondary** | `#1A1A1E` | Bloco de código, container de tarefas e input principal |
| **Border Dark** | `#27272A` | Bordas de containers, separadores e abas |
| **Text Primary** | `#E4E4E7` | Textos principais, nomes de arquivos e checklist |
| **Text Secondary** | `#A1A1AA` | Metadados, "Thought for Xs", rótulos secundários |
| **Text Code Green** | `#4ADE80` | Sintaxe YAML, diffs positivos e ícones de sucesso |
| **Text Code Red** | `#F87171` | Diffs negativos |
| **Text Link/Accent** | `#60A5FA` | Links clicáveis e referências de arquivos |
| **Badge New Background** | `#163B2A` | Fundo da tag `new` |
| **Badge New Text** | `#4ADE80` | Texto da tag `new` |
| **Warning Background** | `#2D1C11` | Fundo do banner de cota esgotada |
| **Warning Border** | `#522E15` | Borda do banner de aviso |
| **Warning Accent/Text** | `#F97316` | Ícone e texto de alerta |

##### Typography & Fonts

- **UI:** `Inter`, `system-ui`, `-apple-system`, `sans-serif`
- **Código/terminal:** `JetBrains Mono`, `Fira Code`, `Consolas`, `monospace`
- `xs`: 11px / 14px — badges e tags
- `sm`: 13px / 18px — logs de pensamento, checklist, input e footer
- `base`: 14px / 20px — títulos de blocos e avisos

#### 📐 Layout Structure

```text
+-----------------------------------------------------------------------------------+
| Top Navigation Bar                                                                |
+-----------------------------------------------------------------------------------+
| Main Scrollable Area                                                              |
|                                                                                   |
|  - Code Snippet (release.yml)                                                     |
|  - AI Thought Logs & Status Message                                              |
|  - Code Snippet (ci.yml)                                                          |
|  - Tasks Collapsible Panel                                                        |
|  - Warning Banner (Quota Limit)                                                   |
|  - Message Actions Toolbar                                                        |
|                                                                                   |
+-----------------------------------------------------------------------------------+
| Bottom Chat Input Box                                                             |
|   - Placeholder Text                                                              |
|   - Sub-bar (Controls, Selector, Actions)                                         |
| Footer Status Bar                                                                 |
+-----------------------------------------------------------------------------------+
```

#### 🧩 Component Specifications

##### 1. Top Navigation Bar

- Altura: **40px**
- Fundo: `#121214` na aba ativa / `#09090B` no restante da barra
- Aba ativa:
  - ícone circular do plugin
  - título **NetBeans Plugin Integration**
  - botão de fechar `X`
- Ações à direita: ícone de divisão de tela/janela

##### 2. Code Snippet Card

- Fundo: `#1A1A1E`
- Borda: `1px solid #27272A`
- Radius: `8px`
- Padding: `12px 16px`

**Header:** ícone do tipo de arquivo, nome (`release.yml`/`ci.yml`), badge opcional `new` e contadores de diff (`+106` / `-31`).

**Content:**

- fonte monospace 13px, line-height 1.5
- chaves/valores principais: `#4ADE80`
- valores secundários: `#A1A1AA`

##### 3. AI Log Line — “Thought for X s”

- Altura: **28px**
- Fonte: 13px
- Cor: `#A1A1AA`
- Ícone de pensamento + texto `Thought for 1s`, `Thought for 9s`, etc.
- Referências a arquivos aparecem como links azuis, por exemplo `.github/workflows` e `ci.yml`.
- Mensagens de falha/cancelamento aparecem como texto simples e legível.

##### 4. Tasks Panel

- Cabeçalho com chevron e status, por exemplo **5 / 6 tasks done**
- Fundo: `#1A1A1E`
- Borda: `1px solid #27272A`
- Radius: `8px`
- Padding: `12px 16px`
- Tarefa concluída: check circular em `#4ADE80` + texto `#E4E4E7`
- Tarefa pendente: círculo vazado `#52525B` + texto `#A1A1AA`
- Altura aproximada por item: **24px**

##### 5. Quota Warning Banner

- Fundo: `#2D1C11`
- Borda: `1px solid #522E15`
- Radius: `8px`
- Padding: `10px 14px`
- Ícone triangular em `#F97316`
- Link de ação destacado e sublinhado em `#F97316`

Texto de referência:

> Your included daily usage quota is exhausted. **Purchase extra usage** to continue using premium models.

##### 6. Message Actions Toolbar

Posicionado à direita abaixo dos logs e banners.

- Like
- Dislike
- Copy Code
- Sidebar Toggle
- More Options (`...`)
- ícones: 16px
- gap: 12px
- cor padrão: `#A1A1AA`
- hover: `#E4E4E7`

##### 7. Bottom Input Box

- Fundo: `#1A1A1E`
- Borda: `1px solid #27272A`
- Radius: `12px`
- Padding: `12px 16px`

**Linha superior:**

`Tip: Type @ conversation to bring in context from another chat`

**Barra inferior:**

- `+` para anexos/ações
- `< > Code` para alternar modo de código
- seletor de modelo, por exemplo `SWE-1.6 Slow`
- status de sincronização `Cascade`
- microfone
- botão circular de envio com seta para cima

##### 8. Footer Status Bar

- Altura: **28px**
- Fundo: transparente / `#121214`
- Fonte: 12px
- Esquerda: `[Folder Icon] Local` | `[Folder Icon] continue-netbeans`
- Direita: `Migrate off Cascade`

### Diretriz de implementação visual

A implementação deve preservar a hierarquia visual acima mesmo quando algum recurso ainda não existir no backend. Componentes podem inicialmente aparecer como estado desabilitado, vazio ou de placeholder, mas a estrutura visual, espaçamento, densidade e linguagem de interação devem permanecer consistentes.

A consolidação da UI atual deve convergir para essa linguagem visual, evitando criar uma terceira interface paralela.

## Roadmap técnico

- Consolidar os caminhos de UI duplicados sem quebrar compatibilidade do módulo.
- Implementar e validar a especificação visual Dark Theme como referência única para o chat.
- Reduzir código de scaffolding e funcionalidades não conectadas ao fluxo principal.
- Expandir a cobertura dos provedores e integrações MCP com testes de comportamento reais.
- Melhorar cancelamento e ciclo de vida de operações assíncronas do streaming.
- Fortalecer análise estática e documentação de decisões arquiteturais.

## Licença

Este projeto segue a licença Apache 2.0.
