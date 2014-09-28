/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.world.block.loader;

import org.terasology.module.sandbox.API;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines the sound scape of a Block. Can be reused by multiple block types.
 */
@API
public class BlockSoundsDefinition {

    /**
     * Sounds that are played when a player steps on this block.
     */
    private List<String> stepSounds = new ArrayList<>();

    /**
     * Sounds that are played, when this block is destroyed (i.e. with a pickaxe)
     */
    private List<String> destroySounds = new ArrayList<>();

    /**
     * Sounds that are played, while this block is being dug up (i.e. with a shovel or or pickaxe)
     */
    private List<String> digSounds = new ArrayList<>();

    public List<String> getStepSounds() {
        return stepSounds;
    }

    public void setStepSounds(List<String> stepSounds) {
        this.stepSounds = stepSounds;
    }

    public List<String> getDestroySounds() {
        return destroySounds;
    }

    public void setDestroySounds(List<String> destroySounds) {
        this.destroySounds = destroySounds;
    }

    public List<String> getDigSounds() {
        return digSounds;
    }

    public void setDigSounds(List<String> digSounds) {
        this.digSounds = digSounds;
    }

}
