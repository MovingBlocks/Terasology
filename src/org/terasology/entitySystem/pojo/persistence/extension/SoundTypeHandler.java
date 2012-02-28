package org.terasology.entitySystem.pojo.persistence.extension;

import com.google.common.collect.Lists;
import org.terasology.audio.Sound;
import org.terasology.entitySystem.pojo.persistence.TypeHandler;
import org.terasology.logic.manager.SoundManager;
import org.terasology.protobuf.EntityData;

import java.util.List;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class SoundTypeHandler implements TypeHandler<Sound> {

    private SoundManager soundManager;

    public SoundTypeHandler(SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    public EntityData.Value serialize(Sound value) {
        return EntityData.Value.newBuilder().addString(value.getName()).build();
    }

    public Sound deserialize(EntityData.Value value) {
        if (value.getStringCount() > 0) {
            return soundManager.getSound(value.getString(0));
        }
        return null;
    }

    public EntityData.Value serialize(Iterable<Sound> value) {
        EntityData.Value.Builder result = EntityData.Value.newBuilder();
        for (Sound item : value) {
            result.addString(item.getName());
        }
        return result.build();
    }

    public List<Sound> deserializeList(EntityData.Value value) {
        List<Sound> result = Lists.newArrayListWithCapacity(value.getStringCount());
        for (String item : value.getStringList()) {
            Sound sound = soundManager.getSound(item);
            if (sound != null)
                result.add(sound);
        }
        return result;
    }
}
