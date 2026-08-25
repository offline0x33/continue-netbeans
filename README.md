# Continue Beans ☕

AI assistant for Apache NetBeans.

Continue Beans brings AI-assisted development directly into NetBeans, with local LLM support, workspace context, code generation, streaming responses, developer tools, and NetBeans language services.

## Features

- AI chat integrated into NetBeans
- LM Studio and OpenAI-compatible APIs
- Streaming responses
- Workspace and project context
- File and directory inspection
- Code generation and insertion
- Model discovery
- Conversation history
- MCP and tool integration
- NetBeans language-service integration
- Dark developer-focused interface
- JUnit 5 and Cucumber test suite

## Requirements

- Java 11+
- Apache NetBeans
- Maven 3.9+
- LM Studio or another OpenAI-compatible API provider

## Build

Clone the repository:

```bash
git clone https://github.com/offline0x33/continue-netbeans.git
cd continue-netbeans
```

Build the plugin:

```bash
mvn clean install
```

Run the complete verification suite:

```bash
mvn clean verify
```

The generated NetBeans module is available under `target/*.nbm`.

## AI Providers

Continue Beans uses a provider abstraction so AI backends can evolve independently from the NetBeans UI.

LM Studio is the primary local provider and uses an OpenAI-compatible API. Other compatible providers can be integrated through the same provider layer.

## Workspace Intelligence

The assistant can use the current NetBeans workspace as development context. Workspace tools provide access to project files and directories, while the NetBeans language-service layer provides semantic information for supported languages.

This allows the assistant to work with source code using both file-level context and IDE-level information such as symbols, definitions, references, and diagnostics.

## Architecture

```text
NetBeans
   │
   ▼
Continue Beans UI
   │
   ├── Conversation
   ├── Workspace Tools
   ├── MCP Tools
   └── Language Services
           │
           ▼
       LLM Provider
           │
           └── LM Studio / OpenAI-compatible API
```

The main layers are separated so the UI, AI providers, workspace tooling, and IDE integrations can evolve independently.

## User Interface

Continue Beans uses a dark, developer-oriented interface designed for long coding sessions.

The chat experience includes streaming responses, code blocks, conversation history, model selection, workspace context, tool execution feedback, and developer actions.

Detailed UI specifications belong in the project documentation rather than this README.

## Testing

The project uses JUnit 5, Mockito, Cucumber, and JaCoCo.

Run tests with:

```bash
mvn test
```

Run the complete verification pipeline with:

```bash
mvn verify
```

## Project Status

Continue Beans is under active development.

The core chat experience, local model integration, workspace context, developer tooling, and dark UI are implemented. NetBeans language-service capabilities are being expanded as part of the semantic code-intelligence layer.

## Documentation

Additional technical documentation is maintained under `docs/`.

Detailed architecture, UI specifications, implementation notes, and technical decisions should be maintained there rather than duplicated in this README.

## Contributing

Contributions are welcome.

Before submitting a pull request:

1. Build the project.
2. Run the test suite.
3. Run `mvn verify`.
4. Keep changes focused.
5. Add or update tests for new behavior.

## License

Apache License 2.0.
