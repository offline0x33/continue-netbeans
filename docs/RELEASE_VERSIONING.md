# Release Versioning

The release workflow calculates the next semantic patch version automatically from the latest stable Git tag.

- `v2.0.0` -> `v2.0.1`
- `v2.0.1` -> `v2.0.2`
- If no stable tag exists, the workflow uses the POM version without `-SNAPSHOT` as the base.

The calculated version is applied to Maven only for the release build. The source POM can remain on a development `-SNAPSHOT` version.

The workflow aborts if the calculated tag already exists, preventing an accidental release overwrite.