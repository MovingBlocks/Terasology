/*
 * Copyright 2013 Moving Blocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.game.modes.loadProcesses;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.metadata.ComponentLibrary;
import org.terasology.entitySystem.persistence.EntityDataJSONFormat;
import org.terasology.entitySystem.persistence.PrefabSerializer;
import org.terasology.game.CoreRegistry;
import org.terasology.game.modes.LoadProcess;
import org.terasology.protobuf.EntityData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;

/**
 * @author Immortius
 */
public class LoadPrefabs implements LoadProcess {
    private static final Logger logger = LoggerFactory.getLogger(LoadPrefabs.class);

    private Iterator<AssetUri> prefabs;
    private PrefabSerializer prefabSerializer;

    @Override
    public String getMessage() {
        return "Loading Prefabs...";
    }

    @Override
    public boolean step() {
        AssetUri prefabURI = prefabs.next();
        logger.debug("Loading prefab " + prefabURI);
        try {
            InputStream stream = AssetManager.assetStream(prefabURI);
            if (stream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                EntityData.Prefab prefabData = EntityDataJSONFormat.readPrefab(reader);
                stream.close();
                if (prefabData != null) {
                    prefabSerializer.deserialize(prefabData, prefabURI);
                }
            } else {
                logger.warn("Failed to load prefab '{}'", prefabURI);
            }
        } catch (IOException e) {
            logger.error("Failed to load prefab '{}'", prefabURI, e);
        }
        return !prefabs.hasNext();
    }

    @Override
    public int begin() {
        prefabSerializer = new PrefabSerializer(CoreRegistry.get(PrefabManager.class), CoreRegistry.get(ComponentLibrary.class));
        prefabs = Assets.list(AssetType.PREFAB).iterator();
        return Lists.newArrayList(Assets.list(AssetType.PREFAB)).size();
    }
}
