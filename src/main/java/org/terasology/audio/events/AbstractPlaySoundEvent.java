package org.terasology.audio.events;

import org.terasology.audio.Sound;
import org.terasology.entitySystem.AbstractEvent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.network.NetworkEvent;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
public abstract class AbstractPlaySoundEvent extends NetworkEvent {
    private Sound sound;
    private float volume;

    protected AbstractPlaySoundEvent() {}

    public AbstractPlaySoundEvent(Sound sound, float volume) {
        this.sound = sound;
        this.volume = volume;
    }

    public AbstractPlaySoundEvent(EntityRef instigator, Sound sound, float volume) {
        super(instigator);
        this.sound = sound;
        this.volume = volume;
    }

    public Sound getSound() {
        return sound;
    }

    public float getVolume() {
        return volume;
    }
}
