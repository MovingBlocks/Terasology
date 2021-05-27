// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.audio.events;

import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.BroadcastEvent;

/**
 * This event is sent whenever a sound is to be played.
 * The event is not sent to or played for the client of the entity that sent the event as they have simulated it already.
 */
@BroadcastEvent(skipInstigator = true)
public class PlaySoundEvent extends AbstractPlaySoundEvent {
    protected PlaySoundEvent() {
    }


    public PlaySoundEvent(StaticSound sound, float volume) {
        super(sound, volume);
    }


    public PlaySoundEvent(EntityRef exceptOwner, StaticSound sound, float volume) {
        super(exceptOwner, sound, volume);
    }

}
