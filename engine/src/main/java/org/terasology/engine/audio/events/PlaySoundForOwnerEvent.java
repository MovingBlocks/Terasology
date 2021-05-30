// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.audio.events;

import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.network.OwnerEvent;

/**
 * This event is sent whenever a sound is played.
 * It will also be sent to the client that owns the entity sending the event.
 */
@OwnerEvent
public class PlaySoundForOwnerEvent extends AbstractPlaySoundEvent {

    protected PlaySoundForOwnerEvent() {
    }

    public PlaySoundForOwnerEvent(StaticSound sound, float volume) {
        super(sound, volume);
    }

}
