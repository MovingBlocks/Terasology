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
package org.terasology.world.block;

import com.google.common.collect.ImmutableList;
import org.terasology.audio.StaticSound;

import java.util.Collections;
import java.util.List;

/**
 * Defines the sound scape of a Block. Can be reused by multiple block types.
 */
public class BlockSounds {

    /**
     * The ID of the default block sounds.
     */
    public static final String DEFAULT_ID = "engine:default";

    /**
     * Default block sounds. Used instead of a null value to avoid additional null-checks.
     */
    public static final BlockSounds NULL = new BlockSounds(
            "engine:null",
            Collections.<StaticSound>emptyList(),
            Collections.<StaticSound>emptyList(),
            Collections.<StaticSound>emptyList()
    );

    private final String uri;

    private final List<StaticSound> stepSounds;

    private final List<StaticSound> destroySounds;

    private final List<StaticSound> digSounds;

    public BlockSounds(String uri, List<StaticSound> stepSounds, List<StaticSound> destroySounds, List<StaticSound> digSounds) {
        this.uri = uri;
        this.stepSounds = ImmutableList.copyOf(stepSounds);
        this.destroySounds = ImmutableList.copyOf(destroySounds);
        this.digSounds = ImmutableList.copyOf(digSounds);
    }

    public String getUri() {
        return uri;
    }

    public List<StaticSound> getStepSounds() {
        return stepSounds;
    }

    public List<StaticSound> getDestroySounds() {
        return destroySounds;
    }

    public List<StaticSound> getDigSounds() {
        return digSounds;
    }

}
