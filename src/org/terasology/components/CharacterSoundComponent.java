package org.terasology.components;

import com.google.common.collect.Lists;
import org.terasology.entitySystem.AbstractComponent;
import org.terasology.entitySystem.Component;
import org.terasology.logic.audio.Sound;
import org.terasology.persistence.interfaces.StorageReader;
import org.terasology.persistence.interfaces.StorageWriter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class CharacterSoundComponent extends AbstractComponent {

    public List<Sound> footstepSounds = Lists.newArrayList();
    public float footstepVolume = 1.0f;

}
