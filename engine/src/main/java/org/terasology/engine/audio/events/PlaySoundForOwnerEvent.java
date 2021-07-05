// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.audio.events;

import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.network.OwnerEvent;

/**
 * A <i>trigger event</i> to play a sound for the owner of the targeted entity.
 * <p>
 * It will also be sent to the client that owns the entity the event is sent against.
 * <p>
 * Note, that the volume is only adjusted for static sounds with <b>mono channel</b>.
 */
@OwnerEvent
public class PlaySoundForOwnerEvent extends AbstractPlaySoundEvent {

    protected PlaySoundForOwnerEvent() {
    }

    public PlaySoundForOwnerEvent(StaticSound sound, float volume) {
        super(sound, volume);
    }

}
