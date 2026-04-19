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
| Integration (MTE, multiplayer) | `@IntegrationEnvironment(networkMode = LISTEN_SERVER)` | Very slow | `ClientConnectionTest` |

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
`ChatMessageEvent` (`@BroadcastEvent`) are already fully registered and
will replicate correctly:

```java
// This works — ChatMessageEvent is engine-registered with full metadata
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
register a `BaseComponentSystem` as a probe and also register it with the
`EventSystem` directly (needed for post-initialization registration):

```java
MyProbe probe = new MyProbe();
context.get(ComponentSystemManager.class).register(probe);
context.get(EventSystem.class).registerEventHandler(probe);
```

### What Does NOT Work

Defining `@BroadcastEvent`/`@OwnerEvent`/`@ServerEvent` inner classes in
test files and expecting them to replicate over the network. They register
locally but the `EventLibrary` doesn't pick up their network metadata,
so `NetworkEventSystemDecorator.networkReplicate()` silently skips them.

## Context and Registry Patterns

### CoreRegistry Isolation

`CoreRegistry` is a deprecated static singleton. Tests sharing a JVM must
save and restore it:

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
