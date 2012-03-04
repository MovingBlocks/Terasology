package org.terasology.entitySystem.persist;

import com.google.gson.Gson;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.pojo.PojoPrefab;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

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
