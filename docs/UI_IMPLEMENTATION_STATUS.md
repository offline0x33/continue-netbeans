# UI implementation status

The canonical chat entry point is `ChatPanel`.

Implemented in the current main branch:

- Dark chat surface and 40px top bar.
- Scrollable conversation area.
- Task panel driven by `TaskOrchestrator` state.
- Warning and failure cards.
- Persistent composer with model selector and workspace footer.
- Message action row.
- Workspace context and native NetBeans Java language services.

Still being refined for visual fidelity:

- Rich file/code cards with real file metadata and diff counters.
- More precise message action semantics and copy behavior.
- Visual treatment for file links and tool activity.
- Exact spacing, hover, focus and icon states from the product specification.

The specification in `docs/UI_SPECIFICATION.md` is the visual contract; implementation claims must match the code in `ChatPanel`.
