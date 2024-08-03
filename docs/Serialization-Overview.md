## Introduction
Terasology relies on [a custom system](https://github.com/MovingBlocks/Terasology/tree/develop/engine/src/main/java/org/terasology/persistence/typeHandling) to serialize Java objects of arbitrary types. It was initially written to serve as a medium to serialize objects of custom module types in components, but it has now been expanded to be a fully independent serialization library capable of serializing almost every type out-of-the-box.

## Design
Serialization functionality is based around 3 main types: `TypeHandler`, `TypeHandlerFactory` and `TypeHandlerLibrary`. More information can be found in the classes' JavaDoc.

### `TypeHandler`
A `TypeHandler` is responsible for serializing objects of a certain type. During the serialization process, it must convert an object to a `PersistedData` (the serialized form represented in memory) such that a similar object with the same data can be accurately reconstructed from that `PersistedData`.`TypeHandler` code should be designed to be as robust as possible; rather than throwing an exception or returning null when a field is not serializable, a `TypeHandler` should simply skip over the unserializable/unknown field and serialize the rest normally.

It is also important to note that `TypeHandler` methods should be considered "hot" code, and any information that can be cached (like field metadata for a type) should be cached. Since the `TypeHandler`s themselves are cached, this prevents unnecessary and potentially expensive information retrieval during the serialize and deserialize methods, which may be called multiple times during the serialization of a single object. This caching process is aided by `TypeHandlerFactory`, which can provide the common cached data to the `TypeHandler` during construction.

### `TypeHandlerFactory`
A `TypeHandlerFactory` is used to create a `TypeHandler` for the given type. The `TypeHandlerFactory` can obtain any information that the `TypeHandler` might need (like class or field metadata) and pass it along to the `TypeHandler` during construction.

Example of a `TypeHandler` that does not require a `TypeHandlerFactory`

### `TypeHandlerLibrary`
A `TypeHandlerLibrary` is responsible for creating `TypeHandler`s for various types. To obtain the `TypeHandler` for a specific type, it iterates through all registered `TypeHandlerFactory`s and asks each of them to create a `TypeHandler` for the given type; the first successfully generated `TypeHandler` is returned. The iteration is done so that factories that are registered later are given precedence over factories registered first. This allows consumers to override default type handling behaviour for specific types depending on their needs.

The `TypeHandlerLibrary` caches type handlers for various types. Since a single type's `TypeHandler` may be retrieved multiple times while serializing a single object, this cache helps avoid calling the expensive `TypeHandler` creation process (via `TypeHandlerFactory`) multiple times for efficiency. While the `TypeHandlerLibrary` only deals with type handlers, it is also possible to "register" a `TypeHandler` for a given type using `addTypeHandler` -- under the hood, this method just creates a `TypeHandlerFactory`.

The `@RegisterTypeHandler` and the `@RegisterTypeHandlerFactory` annotations can also be used to register type handlers and type handler factories to the `TypeHandlerLibrary` in the `Context`.

[Read more about the TypeHandlerLibrary](https://github.com/MovingBlocks/Terasology/tree/develop/subsystems/TypeHandlerLibrary)

### Examples

#### `TypeHandler` without a `TypeHandlerFactory`
- [`IntTypeHandler`](https://github.com/MovingBlocks/Terasology/blob/develop/engine/src/main/java/org/terasology/persistence/typeHandling/coreTypes/IntTypeHandler.java)

These are registered using the `TypeHandlerLibrary.addTypeHandler` method.

#### `TypeHandler` and `TypeHandlerFactory` pair
- [`CollectionTypeHandler`](https://github.com/MovingBlocks/Terasology/blob/develop/engine/src/main/java/org/terasology/persistence/typeHandling/coreTypes/CollectionTypeHandler.java) and [`CollectionTypeHandlerFactory`](https://github.com/MovingBlocks/Terasology/blob/develop/engine/src/main/java/org/terasology/persistence/typeHandling/coreTypes/factories/CollectionTypeHandlerFactory.java)

Only the `TypeHandlerFactory` has to be registered via the `TypeHandlerLibrary.addTypeHandlerFactory` method; the `TypeHandler` does not need to be registered separately.

## Workflow
The workflow for (de)serializing an object is as follows:
- Retrieve the `TypeHandler` for its type from the `TypeHandlerLibrary` (using `getTypeHandler`)
- (De)serialize the object using the retrieved `TypeHandler`.

Depending on the serialization format used, the `TypeHandler` must be provided with appropriate `PersistedData` and `PersistedDataSerializer` instances. To make the process easier, Terasology provides the `AbstractSerializer` utility class, implementations of which provide helper methods to easily serialize and deserialize objects to disk using the preferred format.

### Supported Formats
Terasology currently supports JSON (via Gson) and binary (via Protobuf) serialization. 

- To serialize objects in JSON, use the `GsonPersistedData` and `GsonPersistedDataSerializer` classes or the `GsonSerializer` utility class.
- To serialize objects in binary, use the `ProtobufPersistedData` and `ProtobufPersistedDataSerializer` classes or the `ProtobufSerializer` utility class.

## Features
- Format independent -- code that serializes an object remains the same regardless of the target format.
- Supports almost every possible type out-of-the-box.
- Supports recursive types (i.e. types that refer to themselves) but not cyclic object references.
- It is possible to override default serialization behavior by specifying a new `TypeHandler` that handles a type differently.
