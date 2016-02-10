/*
 * Copyright 2015 MovingBlocks
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
package org.terasology.world.block.sounds;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.audio.StaticSound;
import org.terasology.module.sandbox.API;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@API
public class BlockSounds extends Asset<BlockSoundsData> {
    private final List<StaticSound> stepSounds = new ArrayList<>();
    private final List<StaticSound> destroySounds = new ArrayList<>();
    private final List<StaticSound> digSounds = new ArrayList<>();

    public BlockSounds(ResourceUrn urn, AssetType<?, BlockSoundsData> assetType, BlockSoundsData data) {
        super(urn, assetType);
        reload(data);
    }

    @Override
    protected void doReload(BlockSoundsData blockSoundsData) {
        stepSounds.clear();
        digSounds.clear();
        destroySounds.clear();
        stepSounds.addAll(blockSoundsData.getStepSounds());
        digSounds.addAll(blockSoundsData.getDigSounds());
        destroySounds.addAll(blockSoundsData.getDestroySounds());
    }

    public List<StaticSound> getStepSounds() {
        return Collections.unmodifiableList(stepSounds);
    }

    public List<StaticSound> getDigSounds() {
        return Collections.unmodifiableList(digSounds);
    }

    public List<StaticSound> getDestroySounds() {
        return Collections.unmodifiableList(destroySounds);
    }
}
