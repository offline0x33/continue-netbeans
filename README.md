# Continue Beans ☕

AI assistant for Apache NetBeans.

Continue Beans brings AI-assisted development directly into NetBeans, with local LLM support, workspace context, code generation, streaming responses, developer tools, and native NetBeans language services.

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
- Native NetBeans Java language services
- Dark developer-focused interface
- JUnit 5 and Cucumber test suite

## Requirements

- Java 11+
- Apache NetBeans
- Maven 3.9+
- LM Studio or another OpenAI-compatible API provider

## Build

```bash
git clone https://github.com/offline0x33/continue-netbeans.git
cd continue-netbeans
mvn clean install
```

Run the complete verification suite:

```bash
mvn clean verify
```

The generated NetBeans module is available under `target/*.nbm`.

## AI Providers

Continue Beans uses a provider abstraction so AI backends can evolve independently from the NetBeans UI.

LM Studio is the primary local provider and uses an OpenAI-compatible API.

## Workspace Intelligence

The assistant can use the current NetBeans workspace as development context. Workspace tools provide access to project files and directories, while native NetBeans language services provide semantic information for Java source code.

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
   └── Native Language Services
           │
           ▼
       LLM Provider
           │
           └── LM Studio / OpenAI-compatible API
```

The main layers are separated so the UI, AI providers, workspace tooling, and IDE integrations can evolve independently.

## User Interface

Continue Beans uses a dark, developer-oriented chat interface as its canonical NetBeans view.

The current UI provides the chat surface, conversation streaming, task progress, tool/status feedback, workspace context, model controls, warnings, message actions, and workspace status.

The detailed visual contract is maintained in [`docs/UI_SPECIFICATION.md`](docs/UI_SPECIFICATION.md). Remaining visual-fidelity work is tracked separately and does not define the availability of the core chat experience.

## Testing

The project uses JUnit 5, Mockito, Cucumber, and JaCoCo.

```bash
mvn test
mvn verify
```

## Project Status

Continue Beans is under active development.

The core chat experience, workspace tooling, large-context handling, native NetBeans Java language services, and canonical Dark Theme entry point are implemented. Visual-fidelity refinements continue in the canonical `ChatPanel`.

## Documentation

Additional technical documentation is maintained under [`docs/`](docs/).

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
