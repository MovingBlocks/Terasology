// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.audio.events;

import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.BroadcastEvent;

/**
 * A <i>trigger event</i> to play a sound.
 * <p>
 * The event is {@link BroadcastEvent broadcasted} to all connected systems.
 * If the {@code instigator}
 * A client sending this event should have already simulated it locally.
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

    /**
     * Create a new trigger event to play a sound on all connected clients.
     *
     * The instigator of this event is undefined ({@link EntityRef#NULL}).
     *
     * @param sound the sound to play; should be mono channel
     * @param volume the volume of the sound effect in {@code [0.0, 1.0]}
     */
    public PlaySoundEvent(StaticSound sound, float volume) {
        super(sound, volume);
    }

    /**
     * Create a new trigger event to play a sound on all connected systems except for the instigating client.
     *
     * The instigator client is derived from the {@code instigator} entity by resolving the ownership relation.
     * The <i>client entity</i> is defined by having the {@link org.terasology.engine.network.ClientComponent ClientComponent}.
     *
     * @param instigator an entity denoting the instigator system
     * @param sound the sound to play; should be mono channel
     * @param volume the volume of the sound effect in {@code [0.0, 1.0]}
     */
    public PlaySoundEvent(EntityRef instigator, StaticSound sound, float volume) {
        super(instigator, sound, volume);
    }

}
