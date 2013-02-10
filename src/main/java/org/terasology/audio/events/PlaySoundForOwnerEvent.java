package org.terasology.audio.events;

import org.terasology.audio.Sound;
import org.terasology.network.OwnerEvent;

/**
 * @author Immortius
 */
@OwnerEvent
public class PlaySoundForOwnerEvent extends AbstractPlaySoundEvent {

    protected PlaySoundForOwnerEvent() {
    }

    public PlaySoundForOwnerEvent(Sound sound, float volume) {
        super(sound, volume);
    }

}
