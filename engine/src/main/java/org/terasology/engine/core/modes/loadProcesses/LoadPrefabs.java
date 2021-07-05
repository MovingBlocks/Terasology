// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.StepBasedLoadProcess;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.assets.management.AssetManager;

import java.util.Iterator;

public class LoadPrefabs extends StepBasedLoadProcess {
    private final AssetManager assetManager;
    private Iterator<ResourceUrn> prefabs;

    public LoadPrefabs(Context context) {
        this.assetManager = context.get(AssetManager.class);
    }

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

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
