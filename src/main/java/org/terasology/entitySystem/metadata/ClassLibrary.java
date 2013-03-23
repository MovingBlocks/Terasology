package org.terasology.entitySystem.metadata;

/**
 * @author Immortius
 */
public interface ClassLibrary<T> extends Iterable<ClassMetadata<? extends T>> {

    /**
     * Registers a class with this library
     *
     * @param clazz
     */
    void register(Class<? extends T> clazz);

    /**
     * Registers a class with this library
     *
     * @param clazz
     * @param names The names to use to find this class
     */
    void register(Class<? extends T> clazz, String... names);

    /**
     * @param clazz
     * @return The metadata for the given clazz, or null if not registered.
     */
    <U extends T> ClassMetadata<U> getMetadata(Class<U> clazz);

    <U extends T> ClassMetadata<U> getMetadata(U object);

    /**
     * Copies registered class
     *
     * @param object
     * @return A copy of the class, or null if not registered
     */
    <TYPE extends T> TYPE copy(TYPE object);

    /**
     * @param className The simple name of the class - no packages, and may exclude any common suffix. Case doesn't matter.
     * @return The metadata for the given class, or null if not registered.
     */
    ClassMetadata<? extends T> getMetadata(String className);

}
