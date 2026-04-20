# Engine Testing Patterns

Patterns and gotchas for writing tests against the Terasology engine,
especially integration tests using the ModuleTestingEnvironment (MTE).

For module-level testing basics, see [Testing-Modules.md](Testing-Modules.md).

## Test Hierarchy

Tests form a natural progression from fast/isolated to slow/integrated:

| Level | Runner | Speed | Example |
|-------|--------|-------|---------|
| Unit (no engine) | Plain JUnit, mocks | Fast | `PojoEventSystemTests` |
| Unit (engine libs) | `ModuleManagerFactory`, manual Context | Medium | `ContextImplTest` |
| Integration (MTE, single player) | `@IntegrationEnvironment` | Slow | `ComponentSystemTest` |
| Integration (MTE, multiplayer) | `@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)` | Very slow | `ClientConnectionTest` |

Prefer higher levels only when the thing you're testing genuinely requires
the engine or network stack.

## MTE Basics

The `@IntegrationEnvironment` annotation starts a headless engine instance.
Use `@In` (not `@javax.inject.Inject`) for field injection in test classes —
the MTE harness uses `InjectionHelper` which looks for `@In`.

```java
@IntegrationEnvironment
public class MyTest {
    @In
    private EntityManager entityManager;  // injected by MTE

    @Test
    public void testSomething(MainLoop mainLoop) {
        // MainLoop injected via JUnit parameter resolution
    }
}
```

For multiplayer tests, use `NetworkMode.LISTEN_SERVER` and create clients
via `ModuleTestingHelper`:

```java
@IntegrationEnvironment(networkMode = NetworkMode.LISTEN_SERVER)
public class MyNetworkTest {
    @In
    private ModuleTestingHelper helper;

    @Test
    public void testWithClient() throws IOException {
        Context clientContext = helper.createClient();
        // client has its own EntityManager, NetworkSystem, etc.
    }
}
```

## Network Event Testing

### The Event Registration Problem

Network events (`@BroadcastEvent`, `@OwnerEvent`, `@ServerEvent`) require
two things to replicate over the network:

1. Registration in the `EventSystem` (for local dispatch)
2. Registration in the `EventLibrary` (for network metadata — direction, serialization)

The `NetworkEventSystemDecorator` handles both: its `registerEvent()` method
registers the event and, if the event has a network annotation, adds it to
the `EventLibrary`.

**Inner-class events defined in test files** get registered in the `EventSystem`
by the module classpath scan, but may NOT get their network metadata populated
in the `EventLibrary`. This means they fire locally but never replicate across
the network.

### Working Patterns

**Use existing engine events for network propagation tests.** Events like
`ChatMessageEvent` (`@OwnerEvent`) are already fully registered and
will replicate correctly:

```java
// This works — ChatMessageEvent is engine-registered with full metadata
// It's an @OwnerEvent, so it replicates to the entity's owner
clientEntity.send(new ChatMessageEvent("hello", senderInfo));
```

**Use `TestEventReceiver` for local event testing.** MTE provides this
helper to listen for events without defining a full ComponentSystem:

```java
try (TestEventReceiver<MyEvent> receiver = new TestEventReceiver<>(context, MyEvent.class)) {
    entity.send(new MyEvent());
    assertTrue(receiver.hasReceived());
}
```

**Register a probe system for multiplayer event verification.** When you
need to verify an event arrived on a specific context (host or client),
register a `BaseComponentSystem` as a probe via `ComponentSystemManager`:

```java
MyProbe probe = new MyProbe();
context.get(ComponentSystemManager.class).register(probe);
```

`ComponentSystemManager.register()` handles event handler registration
internally — do not also call `EventSystem.registerEventHandler()` as
this causes duplicate event delivery.

### What Does NOT Work

Defining `@BroadcastEvent`/`@OwnerEvent`/`@ServerEvent` inner classes in
test files and expecting them to replicate over the network. They register
locally but the `EventLibrary` doesn't pick up their network metadata,
so `NetworkEventSystemDecorator.networkReplicate()` silently skips them.

## Singleton State in Tests

Several engine classes are global singletons (`PathManager`, `CoreRegistry`)
that tests must mutate. This is inherently fragile — any test that changes
singleton state affects all subsequent tests in the same JVM.

### The save/restore pattern

Always save the original state in `@BeforeEach` and restore in `@AfterEach`:

```java
private Path originalHomePath;

@BeforeEach
void setUp(@TempDir Path tempHome) throws IOException {
    originalHomePath = PathManager.getInstance().getHomePath();
    PathManager.getInstance().useOverrideHomePath(tempHome);
}

@AfterEach
void tearDown() throws IOException {
    // Guard: @TempDir cleanup may have already deleted the original path
    if (originalHomePath != null && Files.isDirectory(originalHomePath)) {
        PathManager.getInstance().useOverrideHomePath(originalHomePath);
    }
}
```

The `Files.isDirectory` guard is important — `@TempDir` cleanup runs before
`@AfterEach` in some JUnit configurations, so the path you saved in setup
may already be deleted. Without the guard, the restore itself throws
`NoSuchFileException`.

### Why this matters

If a test class mutates `PathManager` without restoring, the next test class
in the same JVM sees a home path pointing at a deleted temp directory. This
causes `NoSuchFileException` failures that are:
- **Non-deterministic**: they depend on test execution order
- **Environment-specific**: may pass locally but fail in CI (different JVM
  reuse, cleanup timing, OS)
- **Hard to diagnose**: the failing test is correct — the bug is in a
  *different* test class that ran earlier

Gradle's default is one JVM per test worker, so parallel test *classes*
are usually isolated. Parallel *methods* within a class share state —
avoid `@Execution(CONCURRENT)` on tests that mutate singletons.

### CoreRegistry Isolation

`CoreRegistry` is the same pattern — a deprecated static singleton:

```java
@BeforeEach
void setUp() {
    originalContext = CoreRegistry.get(Context.class);
    CoreRegistry.setContext(new ContextImpl());
}

@AfterEach
void tearDown() {
    CoreRegistry.setContext(originalContext);
}
```

MTE tests handle this automatically — don't manually set `CoreRegistry`
in `@IntegrationEnvironment` tests.

### Service vs System

Distinguish between:
- **Services** (in `Context`/`CoreRegistry`): always available via `@In`,
  e.g. `EntityManager`, `NetworkSystem`
- **ComponentSystems** (in `ComponentSystemManager`): event-driven, process
  events via `@ReceiveEvent`, e.g. `ChatSystem`

Some functionality (like chat) requires an ECS system to be registered and
processing events — just having the service in the context isn't enough.

## Test Timing Diagnostics

MTE integration tests — especially those creating clients — are prone to
timeout-related flakiness in CI. Use JUnit 5's `TestReporter` to publish
structured timing data that appears in JUnit XML reports:

```java
@Test
@Tag("flaky")
public void testWithClient(TestReporter reporter) throws IOException {
    long start = System.currentTimeMillis();

    Context clientContext = helper.createClient();
    reporter.publishEntry("client_connect_ms",
            String.valueOf(System.currentTimeMillis() - start));

    // ... test logic ...

    reporter.publishEntry("total_ms",
            String.valueOf(System.currentTimeMillis() - start));
}
```

The published entries appear in JUnit XML output and are accessible via
the Jenkins test report API (`/testReport/api/json`), making them
queryable by both humans and automation without console log scraping.

**When to add timings:** Any test tagged `@Tag("flaky")`, and especially
any test that calls `helper.createClient()` — the client connection
handshake is the most common source of timeout failures.

**Recommended entry names:** `client_connect_ms`, `client2_connect_ms`,
`both_registered_ms`, `messages_sent_ms`, `total_ms`, `ticks_received`.

## Gradle Test Execution

```bash
# Run all tests
./gradlew test

# Run a specific test class (subproject is required)
./gradlew :engine-tests:test --tests "*.ClientNetworkStateTest"

# Force fresh run (clear cached results)
./gradlew :engine-tests:cleanTest :engine-tests:test --tests "*.MyTest"

# Via ws CLI (auto-discovers subproject and clears cache)
ws test terasology ClientNetworkStateTest
```

Always use `cleanTest` for targeted test runs — Gradle's UP-TO-DATE cache
can serve stale results from a previous failure.

## See Also

- [Testing-Modules.md](Testing-Modules.md) — module-level testing basics
- `engine-tests/src/main/java/org/terasology/engine/integrationenvironment/` — MTE source
- `engine-tests/src/test/java/org/terasology/engine/integrationenvironment/` — existing MTE tests
