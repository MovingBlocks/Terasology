package org.terasology.components.actions;

import com.google.common.collect.Lists;
import org.terasology.audio.Sound;
import org.terasology.entitySystem.Component;

import java.util.Arrays;
import java.util.List;

/**
 * When activated, plays a random sound
 *
 * @author Immortius <immortius@gmail.com>
 */
public class PlaySoundActionComponent implements Component {
    public List<Sound> sounds = Lists.newArrayList();
    public float volume = 1.0f;
    public ActionTarget relativeTo = ActionTarget.Instigator;

    public PlaySoundActionComponent() {
    }

    public PlaySoundActionComponent(Sound... sounds) {
        this.sounds.addAll(Arrays.asList(sounds));
    }
}
