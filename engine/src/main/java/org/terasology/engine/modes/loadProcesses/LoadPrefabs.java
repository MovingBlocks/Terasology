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

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.prefab.Prefab;

import java.util.Iterator;

/**
 * @author Immortius
 */
public class LoadPrefabs extends StepBasedLoadProcess {
    private static final Logger logger = LoggerFactory.getLogger(LoadPrefabs.class);

    private Iterator<AssetUri> prefabs;

    @Override
    public String getMessage() {
        return "Loading Prefabs...";
    }

    @Override
    public boolean step() {
        if (prefabs.hasNext()) {
            Assets.get(prefabs.next(), Prefab.class);
            step();
        }
        return !prefabs.hasNext();
    }

    @Override
    public void begin() {
        prefabs = Assets.list(AssetType.PREFAB).iterator();
        setTotalSteps(Lists.newArrayList(Assets.list(AssetType.PREFAB)).size());
    }
}
