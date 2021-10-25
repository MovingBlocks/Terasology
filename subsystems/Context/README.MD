# Context

>It is really not subsystem. but used by them

Context is simple DI implementation for TS

Provide interface for working with context. 

## How to use (from scratch, not Terasology)

1. Implement `Context` class
2. Create it
3. Provide it.
4. Use `InjectionHelper` with context to inject at `@In` fields

## How to use (with Terasology)

1. Create your system/manager/another injectable class type
2. Write field with `@In` - be sure that Field Type is injectable. then Field will be injected
with manager/system/ another injectable class type
3. Mark with `@Share` - be sure that your class is injectable type. then your class be provided to `Context`.