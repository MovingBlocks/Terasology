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
 * An event sent whenever a sound should be played.
 * The event can then either be sent to the client that owens the instigator or not
 */
public abstract class AbstractPlaySoundEvent extends NetworkEvent {
    private StaticSound sound;
    private float volume;

    protected AbstractPlaySoundEvent() {
    }

    /**
     * Event marking a sound should be played
     *
     * @param sound  The sound to play
     * @param volume The volume to play it at
     */
    public AbstractPlaySoundEvent(StaticSound sound, float volume) {
        this.sound = sound;
        this.volume = volume;
    }

    /**
     * Event marking the sound should be played but not for the owner client of the entity.
     *
     * @param instigator The entity to not sent the event to
     * @param sound       The sound to play
     * @param volume      THe volume to play iit at
     */
    public AbstractPlaySoundEvent(EntityRef instigator, StaticSound sound, float volume) {
        super(instigator);
        this.sound = sound;
        this.volume = volume;
    }

    /**
     * @return The sound to play
     */
    public StaticSound getSound() {
        return sound;
    }

    /**
     * @return The volume to play the sound at
     */
    public float getVolume() {
        return volume;
    }
}
