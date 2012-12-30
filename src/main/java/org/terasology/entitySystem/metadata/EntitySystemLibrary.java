package org.terasology.entitySystem.metadata;

import org.terasology.entitySystem.Component;

/**
 * @author Immortius
 */
public interface EntitySystemLibrary {

    /**
     *
     * @return The library of component metadata
     */
    ComponentLibrary getComponentLibrary();

    /**
     *
     * @return The library of event metadata
     */
    EventLibrary getEventLibrary();

    /**
     * Registers a handler for a specific type.
     * <p/>
     * Type handlers are used when analysing a component class, and provide the functionality for serializing,
     * deserializing and cloning supported types
     *
     * @param forClass The type to support
     * @param handler  The handler for forClass
     * @param <T>
     */
    <T> void registerTypeHandler(Class<? extends T> forClass, TypeHandler<T> handler);

    /**
     * Retrieves the handler for a specific type.
     * @param forClass
     * @param <T>
     * @return The handler for the specified type.
     */
    <T> TypeHandler<? super T> getTypeHandler(Class<T> forClass);
}
