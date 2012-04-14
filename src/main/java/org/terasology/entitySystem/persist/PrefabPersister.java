package org.terasology.entitySystem.persist;

import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public interface PrefabPersister {

    public Prefab loadPrefab(Reader reader, PrefabManager manager) throws IOException;

    public PrefabManager loadPrefabs(Reader reader) throws IOException;

    public void savePrefab(Writer writer, Prefab prefab) throws IOException;

    public void savePrefabs(Writer writer, PrefabManager manager) throws IOException;
}
