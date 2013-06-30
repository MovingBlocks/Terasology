package org.terasology.entitySystem.prefab;

import org.terasology.asset.AssetLoader;
import org.terasology.asset.AssetUri;
import org.terasology.engine.CoreRegistry;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.persistence.EntityDataJSONFormat;
import org.terasology.entitySystem.persistence.PrefabSerializer;
import org.terasology.protobuf.EntityData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;

/**
 * @author Immortius
 */
public class PrefabLoader implements AssetLoader<PrefabData> {

    public PrefabLoader() {
    }

    @Override
    public PrefabData load(AssetUri uri, InputStream stream, List<URL> urls) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        EntityData.Prefab prefabData = EntityDataJSONFormat.readPrefab(reader);
        if (prefabData != null) {
            return new PrefabSerializer(CoreRegistry.get(ComponentLibrary.class)).deserialize(prefabData);
        }
        return null;
    }
}
