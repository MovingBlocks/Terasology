package org.terasology.components.actions;

import com.google.common.collect.Lists;
import org.terasology.audio.Sound;
import org.terasology.entitySystem.AbstractComponent;

import java.util.Arrays;
import java.util.List;

/**
 * When activated, plays a random sound
 * @author Immortius <immortius@gmail.com>
 */
public class PlaySoundActionComponent extends AbstractComponent {
    public List<Sound> sounds = Lists.newArrayList();
    public float volume = 1.0f;
    
    public PlaySoundActionComponent() {}
    
    public PlaySoundActionComponent(Sound ... sounds)
    {
        this.sounds.addAll(Arrays.asList(sounds));
    }
}
