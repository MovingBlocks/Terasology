package org.terasology.entitySystem.metadata;

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
     * @return The metadata builder used by this library.
     */
    TypeHandlerLibrary getTypeHandlerLibrary();

}
