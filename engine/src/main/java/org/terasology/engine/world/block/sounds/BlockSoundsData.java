// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.block.sounds;

import org.terasology.gestalt.assets.AssetData;
import org.terasology.engine.audio.StaticSound;
import org.terasology.context.annotation.API;

import java.util.ArrayList;
import java.util.List;

@API
public class BlockSoundsData implements AssetData {
    private List<StaticSound> stepSounds = new ArrayList<>();
    private List<StaticSound> destroySounds = new ArrayList<>();
    private List<StaticSound> digSounds = new ArrayList<>();

    /**
     * Sounds that are played when a player steps on this block.
     */
    public List<StaticSound> getStepSounds() {
        return stepSounds;
    }

    public void setStepSounds(List<StaticSound> stepSounds) {
        this.stepSounds = stepSounds;
    }

    /**
     * Sounds that are played, when this block is destroyed (i.e. with a pickaxe)
     */
    public List<StaticSound> getDestroySounds() {
        return destroySounds;
    }

    public void setDestroySounds(List<StaticSound> destroySounds) {
        this.destroySounds = destroySounds;
    }

    /**
     * Sounds that are played, while this block is being dug up (i.e. with a shovel or or pickaxe)
     */
    public List<StaticSound> getDigSounds() {
        return digSounds;
    }

    public void setDigSounds(List<StaticSound> digSounds) {
        this.digSounds = digSounds;
    }
}
