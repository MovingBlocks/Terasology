// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.sounds;

import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.engine.audio.StaticSound;
import org.terasology.context.annotation.API;

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
