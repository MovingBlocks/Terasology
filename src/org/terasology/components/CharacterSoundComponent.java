package org.terasology.components;

import org.terasology.entitySystem.Component;
import org.terasology.logic.audio.Sound;
import org.terasology.persistence.interfaces.StorageReader;
import org.terasology.persistence.interfaces.StorageWriter;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class CharacterSoundComponent implements Component {
    public Sound[] footstepSounds = new Sound[0];
    public float footstepVolume = 1.0f;

    public void store(StorageWriter writer) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void retrieve(StorageReader reader) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
