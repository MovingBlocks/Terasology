package org.terasology.components;

import com.google.common.collect.Lists;
import org.terasology.audio.Sound;
import org.terasology.entitySystem.Component;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class CharacterSoundComponent implements Component {

    public List<Sound> footstepSounds = Lists.newArrayList();
    public float footstepVolume = 1.0f;

}
