# Future Tasks Roadmap

## 1. MTE Module Synchronization
*   **Context**: Changes were made to `EntitySystemSetupUtil` and `NetworkEventSystemDecorator` in the `engine` to fix network event propagation in tests.
*   **Task**: Propagate these changes to the standalone `ModuleTestingEnvironment` module (located in `modules/ModuleTestingEnvironment`).
    *   Compare `engine` implementation with `ModuleTestingEnvironment` implementation.
    *   Apply the `NetworkEventSystemDecorator` wrapping logic to the MTE module's initialization code.
*   **Verification**: Run tests within the `ModuleTestingEnvironment` module to ensure no regressions and that network events propagate correctly there as well.

## 2. Record & Replay System
*   **Context**: The user mentioned diving into the record & replay system after MTE work is stabilized.
*   **Task**: Investigate the current state of the Record & Replay system.
    *   Assess if the recent `NetworkEventSystemDecorator` changes impact recording/replaying of events.
    *   Create/Run tests specifically for Record & Replay scenarios.

## 3. Networking Coverage Expansion
*   **Context**: JaCoCo analysis identified gaps in `org.terasology.engine.network`.
*   **Task**: Implement tests for:
    *   `ServerInfoService`
    *   `PingService` / `PingComponent`
    *   `ServerPingSystem` / `ClientPingSystem`
