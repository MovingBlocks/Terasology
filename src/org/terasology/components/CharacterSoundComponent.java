package org.terasology.components;

import org.terasology.entitySystem.AbstractComponent;
import org.terasology.entitySystem.Component;
import org.terasology.logic.audio.Sound;
import org.terasology.persistence.interfaces.StorageReader;
import org.terasology.persistence.interfaces.StorageWriter;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class CharacterSoundComponent extends AbstractComponent {
    public Sound[] footstepSounds = new Sound[0];
    public float footstepVolume = 1.0f;



}
