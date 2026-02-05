# Terasology Coding Style Guide

This guide captures the specific coding patterns and Gradle conventions found in the Terasology codebase, focusing on modern Gradle Kotlin DSL (KTS) usage and engine-specific architectures.

## 1. Build System & Gradle (KTS)

The project uses Gradle Kotlin DSL with a centralized `build-logic` composite build.

### Dependency Management
- **Version Catalogs**: Use the `libs` catalog in `settings.gradle.kts` for common dependencies.
- **Constraints**: Use `constraints { ... }` blocks to force specific transitive dependency versions (e.g., forcing a newer `bytebuddy` for Java 17 compatibility).
- **Dependency Substitution**: Use `resolutionStrategy.dependencySubstitution` to swap remote module dependencies with local project sources when available.
- **Platform BOMs**: Use `platform()` for grouping related dependencies (e.g., LWJGL).

### Task Configuration
- **Lazy Registration**: Favor `tasks.register<Type>("name")` over `tasks.create`.
- **Property Delegation**: Use `val propertyName by extra("value")` for sharing variables across subprojects.
- **Inputs/Outputs**: Always define `outputs.dir` or `outputs.file` for custom extraction/copy tasks to support Gradle's up-to-date checks and automatic `clean` task generation.
- **Duplication Strategy**: Explicitly set `duplicatesStrategy = DuplicatesStrategy.EXCLUDE` when merging resources or classes from multiple source sets.

### Java Toolchains & Compatibility
- **JVM Toolchain**: Strictly target Java 17 using `jvmToolchain(17)`.
- **Validation**: Include runtime checks in build scripts to warn users if the `JavaVersion.current()` deviates from the expected version.

## 2. Java & Engine Architecture

### Dependency Injection & Context
- **The `@In` Annotation**: Use the `@In` annotation for automatic service injection within NUI screens and engine components.
- **Context Registry**: Use `context.get(Class<T>)` to retrieve engine services and `CoreRegistry.put()` for global accessibility when necessary.

### Resource & Asset Handling
- **ResourceUrn**: Always use `ResourceUrn` for identifying assets (e.g., `engine:universeSetupScreen`).
- **Asset Templates**: Store static configuration templates in the `/templates` directory and use custom tasks like `CopyButNeverOverwrite` to initialize them into the project root.

### UI Development (NUI)
- **Binding Pattern**: Use `Binding<T>` and `ReadOnlyBinding<T>` to link UI widgets (like `UIText` or `UIDropdown`) to underlying configuration data.
- **Widget Subscription**: Use `WidgetUtil.trySubscribe(this, "widgetId", handler)` for cleaner event handling.
- **Wait Popups**: Long-running operations (like world preview generation) must be wrapped in a `WaitPopup` using `Callable` operations to keep the UI responsive.

## 3. Automation & CI (Jenkins)

- **Declarative Pipelines**: Use Jenkins Declarative Pipeline syntax with `post { always { ... } }` for test result aggregation.
- **Static Analysis Integration**: Build stages should explicitly trigger `recordIssues` for Checkstyle, PMD, and SpotBugs.
- **Flaky Test Management**: Segregate tests using JUnit 5 tags:
    - `unitTest`: Fast, excludes `MteTest`/`TteTest`.
    - `integrationTest`: Slow, includes `MteTest`/`TteTest`, excludes `flaky`.
    - `integrationTestFlaky`: Only runs tests tagged with both integration tags and `flaky`.

## 4. Metadata & Versioning

- **Module Metadata**: Engine and module versions should be mastered in `module.txt` (JSON format) and read into the build system via `JsonSlurper`.
- **Version Info**: The build must generate a `versionInfo.properties` file containing Git hashes, build numbers, and timestamps to be bundled within the JAR for runtime diagnostics.