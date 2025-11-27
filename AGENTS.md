# Agent Workflow & Lessons Learned

This document serves as a living guide for AI agents working on the Terasology project. It captures workflow best practices, platform-specific quirks, and useful commands to ensure efficient collaboration.

## Platform: Windows & PowerShell

General coding on Linux and Mac are well-understood by AI, while Java on Windows with PowerShell is less so, meaning we need extra tips and options here.

- **Path Separators**: Use forward slashes `/` for Gradle tasks and Java paths where possible, but be aware that Windows system paths use backslashes `\`.
- **Command Syntax**: PowerShell handles quotes and environment variables differently than Bash.
    - *Env Vars*: `$env:VAR="value"; command`
    - *Chaining*: Use `;` instead of `&&` if you want unconditional execution, or `if ($?) { command }` for conditional.
    - **File Search**: To find files recursively:
      ```powershell
      Get-ChildItem -Path . -Filter filename.txt -Recurse
      ```
- **Environment Setup**:
    - **JAVA_HOME**: PowerShell terminals in the IDE may not inherit system variables correctly. Set it explicitly for the session plus update the PATH:
      ```powershell
      $env:JAVA_HOME = "D:\Dev\Java\TemurinJDK17"
      $env:Path = "$env:JAVA_HOME\bin;$env:Path"
      ```
    - **Path**: You can verify java is accessible with `& "$env:JAVA_HOME\bin\java" -version`.

## Gradle & Build System
- **Verbosity**: Gradle output can be overwhelming.
    - Avoid `--info` or `--debug` unless necessary.
    - If output is truncated, check XML reports: `build/test-results/test/TEST-*.xml`.
- **Running Tests**:
    - Use specific filters: `./gradlew :project:test --tests "package.ClassName"`
    - Example: `./gradlew :engine-tests:test --tests "org.terasology.engine.BuildValidationTest"`
- **Task Execution**:
    - **Force Run**: To force a test to run without rebuilding the whole project, use `cleanTest` before the test task:
      `./gradlew :project:cleanTest :project:test --tests "..."`
    - **Nuclear Option**: `--rerun-tasks` will rerun EVERYTHING. Use sparingly.
    - **Clean Builds**: When in doubt (class not found, weird linkage errors), run `./gradlew clean`.

## Context Management
- **Logs**: Do not dump full log files into the chat context.
    - Use `grep` (or `Select-String` in PS) to find relevant lines.
    - Read specific blocks of XML reports.
- **File Viewing**: Use `view_file` with line ranges to inspect relevant code sections.

## Testing Strategy: "Meta-Test Suite"
We are building a validation suite from the ground up to verify the test infrastructure itself.
1.  **Build & Classpath**: Verify resources can be loaded.
2.  **Module Loading**: Verify `ModuleManager` works.
3.  **DI & Registry**: Verify injection and `CoreRegistry`.
4.  **Entity System**: Verify `EntityManager` and Events.
5.  **MTE/Multiplayer**: Verify full game environment.

## Common Issues
- **Context Pollution**: `CoreRegistry` is a deprecated static singleton meant to be removed. Tests running in the same JVM must be careful about cleaning it up or isolating contexts.
- **Module Permissions**: The `SecurityManager` (via `ModuleSecurityManager`) can block access to classes. Ensure modules have correct dependencies and permissions.
