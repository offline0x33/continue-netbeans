# Model Selection UI

The chat model selector is a functional control, not a visual placeholder.

- Models are discovered from the configured OpenAI-compatible provider.
- The selected model is persisted through `ContinueSettings`.
- The selected model is reused by the chat/task execution flow.
- The UI must not expose legacy Cascade controls.
- When discovery is unavailable, the selector remains usable with the persisted model when one exists.
