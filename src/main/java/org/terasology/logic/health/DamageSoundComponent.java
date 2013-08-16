package org.terasology.logic.health;

import com.google.common.collect.Lists;
import org.terasology.audio.Sound;
import org.terasology.entitySystem.Component;

import java.util.List;

/**
 * @author Immortius
 */
public class DamageSoundComponent implements Component {
    public List<Sound> sounds = Lists.newArrayList();
}
