package org.terasology.entitySystem.persist;

public abstract class PersisterFactory {

    /**
     * Do nothing, just avoid factory from being instanced
     */

    public static PersisterFactory getFactory() {
        return null;
    }

    public static PrefabPersister createPrefabPersister() {
        return new JsonPersister();
    }

}
