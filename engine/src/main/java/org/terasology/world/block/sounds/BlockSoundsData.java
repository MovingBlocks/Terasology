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

import org.terasology.assets.AssetData;
import org.terasology.audio.StaticSound;
import org.terasology.module.sandbox.API;

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
