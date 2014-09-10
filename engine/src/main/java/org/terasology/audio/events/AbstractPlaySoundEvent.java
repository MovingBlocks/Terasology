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

package org.terasology.audio.events;

import org.terasology.audio.StaticSound;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.network.NetworkEvent;

/**
 * @author Immortius
 */
public abstract class AbstractPlaySoundEvent extends NetworkEvent {
    private StaticSound sound;
    private float volume;

    protected AbstractPlaySoundEvent() {
    }

    public AbstractPlaySoundEvent(StaticSound sound, float volume) {
        this.sound = sound;
        this.volume = volume;
    }

    public AbstractPlaySoundEvent(EntityRef instigator, StaticSound sound, float volume) {
        super(instigator);
        this.sound = sound;
        this.volume = volume;
    }

    public StaticSound getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }
}
