package org.terasology.audio.events;

import org.terasology.audio.Sound;
import org.terasology.entitySystem.AbstractEvent;
import org.terasology.network.OwnerEvent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@OwnerEvent
public class PlaySoundForOwnerEvent extends AbstractPlaySoundEvent {

    protected PlaySoundForOwnerEvent() {}

    public PlaySoundForOwnerEvent(Sound sound, float volume) {
        super(sound, volume);
    }

}
