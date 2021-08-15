// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.audio.events;

import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.NetworkEvent;

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
