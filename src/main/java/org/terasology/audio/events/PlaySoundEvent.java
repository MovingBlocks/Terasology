package org.terasology.audio.events;

import org.terasology.audio.Sound;
import org.terasology.entitySystem.EntityRef;
import org.terasology.network.BroadcastEvent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@BroadcastEvent(skipInstigator = true)
public class PlaySoundEvent extends AbstractPlaySoundEvent {
    protected  PlaySoundEvent() {}

    public PlaySoundEvent(Sound sound, float volume) {
        super(sound, volume);
    }

    /**
     *
     * @param exceptOwner The sound is not sent to the owner of this entity.
     * @param sound
     * @param volume
     */
    public PlaySoundEvent(EntityRef exceptOwner, Sound sound, float volume) {
        super(exceptOwner, sound, volume);
    }

}
