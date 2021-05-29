// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.audio.events;

import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.BroadcastEvent;

/**
 * A <i>trigger event</i> to play a sound.
 * <p>
 * The event is not sent to or played for the client of the entity that sent the event as they have simulated it
 * already.
 * <p>
 * The target entities {@link org.terasology.engine.logic.location.LocationComponent LocationComponent} defines at which
 * location (world coordinates) the sound is played at. If the entity does not have a location, the sound is played as
 * global effect.
 * <p>
 * Note, that the volume is only adjusted for static sounds with <b>mono channel</b>.
 */
@BroadcastEvent(skipInstigator = true)
public class PlaySoundEvent extends AbstractPlaySoundEvent {
    /**
     * INTERNAL: Only required for internal replication of network events
     */
    protected PlaySoundEvent() {
    }

    public PlaySoundEvent(StaticSound sound, float volume) {
        super(sound, volume);
    }

    public PlaySoundEvent(EntityRef exceptOwner, StaticSound sound, float volume) {
        super(exceptOwner, sound, volume);
    }

}
