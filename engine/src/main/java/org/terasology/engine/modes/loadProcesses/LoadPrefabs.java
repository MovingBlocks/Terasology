/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.engine.modes.loadProcesses;

import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.context.Context;
import org.terasology.entitySystem.prefab.Prefab;

import java.util.Iterator;

/**
 */
public class LoadPrefabs extends StepBasedLoadProcess {
    private final AssetManager assetManager;
    private Iterator<ResourceUrn> prefabs;

    public LoadPrefabs(Context context) {
        this.assetManager = context.get(AssetManager.class);
    }

    @Override
    public String getMessage() {
        return "Loading Prefabs...";
    }

    @Override
    public boolean step() {
        if (prefabs.hasNext()) {
            assetManager.getAsset(prefabs.next(), Prefab.class);
            stepDone();
        }
        return !prefabs.hasNext();
    }

    @Override
    public void begin() {
        prefabs = assetManager.getAvailableAssets(Prefab.class).iterator();
        setTotalSteps(assetManager.getAvailableAssets(Prefab.class).size());
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
