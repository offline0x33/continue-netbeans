# Maven Build Policy

- Java 11 is the compatibility baseline. Maven compilation must use `--release 11`.
- JaCoCo must remain attached to Surefire; additional JVM arguments must use Surefire late evaluation (`@{argLine}`) so the coverage agent is not overwritten.
- Unit tests must not be excluded from Surefire merely to hide platform/JDK incompatibilities.
- NetBeans JDK module access flags required by tests are explicit in Surefire.
- Checkstyle and PMD remain CI-owned quality gates until their existing violation backlog is migrated into the Maven lifecycle.
