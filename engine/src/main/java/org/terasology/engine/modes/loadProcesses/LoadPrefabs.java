// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.modes.loadProcesses;

import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.StepBasedLoadProcess;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.registry.In;

import java.util.Iterator;

@ExpectedCost(1)
public class LoadPrefabs extends StepBasedLoadProcess {
    @In
    private AssetManager assetManager;
    private Iterator<ResourceUrn> prefabs;

    @Override
    public String getMessage() {
        return "${engine:menu#loading-prefabs}";
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
}
