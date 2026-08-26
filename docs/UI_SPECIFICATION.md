# Continue Beans — UI Specification

This document defines the canonical visual language for the Continue Beans chat interface in Apache NetBeans.

## Design direction

The chat uses a dark developer-oriented interface optimized for long coding sessions. The visual hierarchy is based on compact status rows, code/result cards, task progress, tool activity and a persistent bottom composer.

## Design tokens

| Token | Value | Usage |
|---|---|---|
| Background Main | `#121214` | Main chat surface |
| Background Secondary | `#1A1A1E` | Cards, task panel, composer |
| Border | `#27272A` | Cards, separators and controls |
| Text Primary | `#E4E4E7` | Main content |
| Text Secondary | `#A1A1AA` | Status and metadata |
| Text Muted | `#71717A` | Hints and secondary labels |
| Accent Blue | `#60A5FA` | Links and active references |
| Success Green | `#4ADE80` | Completed state and positive diff |
| Error Red | `#F87171` | Errors and negative diff |
| Warning Orange | `#F97316` | Warnings |
| Warning Background | `#2D1C11` | Warning cards |
| Warning Border | `#522E15` | Warning cards |
| Send Background | `#3F3F46` | Composer send control |

## Typography

- UI: `Inter`, `system-ui`, `-apple-system`, sans-serif
- Code: `JetBrains Mono`, `Fira Code`, `Consolas`, monospace
- Small metadata: 12–13px
- Primary UI text: 13px
- Code: 12–13px

## Layout

```text
+-----------------------------------------------------------------------------------+
| Chat tab / integration header                                                    |
+-----------------------------------------------------------------------------------+
| Scrollable conversation                                                          |
|                                                                                   |
|   User / assistant messages                                                      |
|   Thought and tool status rows                                                   |
|   Code / result cards                                                            |
|   Tasks panel                                                                    |
|   Warning / error states                                                         |
|   Message actions                                                                |
|                                                                                   |
+-----------------------------------------------------------------------------------+
| Chat composer                                                                    |
|   Context hint                                                                    |
|   Input                                                                          |
|   Controls / model / sync / send                                                 |
+-----------------------------------------------------------------------------------+
| Workspace status                                                                 |
+-----------------------------------------------------------------------------------+
```

## Required components

### Header

Compact 40px header with the active Continue Beans tab, integration title and window actions.

### Conversation

Conversation content is vertically stacked and scrollable. Status rows must remain visually lighter than primary messages.

### Thought / tool status

Short metadata rows communicate planning, execution, verification and file/tool references without competing with the main answer.

### Code/result card

Code and tool output appear in a bordered secondary-background card with a compact header, monospace content and optional diff/result metadata.

### Tasks

The task panel shows progress and individual task states. Completed tasks use the success state; pending tasks remain visually muted.

### Warning / error

Warnings and failures use the orange/red semantic palette and must remain visually distinct from ordinary assistant text.

### Message actions

Actions are aligned at the bottom/right of an assistant result and include feedback, copy, secondary view and overflow actions.

### Composer

The composer is persistent at the bottom of the view and contains context hint, input, tool/model controls, synchronization status and send action.

### Footer

The footer exposes workspace/project status without competing with the conversation.

## Implementation rule

The specification is a product contract. New UI code must use the canonical `ChatPanel` path and must not introduce competing chat panels or parallel TopComponents.

Visual changes should be backed by automated Swing/component tests whenever practical.
