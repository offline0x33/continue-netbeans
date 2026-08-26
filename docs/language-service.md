# NetBeans Language Service

Continue Beans uses Apache NetBeans' native Java language infrastructure for semantic Java context.

The integration is intentionally thin: source parsing, symbol resolution and source positions remain owned by NetBeans instead of introducing a second Java parser.
