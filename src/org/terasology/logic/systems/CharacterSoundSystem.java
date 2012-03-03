package org.terasology.logic.systems;

import org.terasology.components.CharacterSoundComponent;
import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandler;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.FootstepEvent;
import org.terasology.logic.audio.Sound;
import org.terasology.logic.manager.AudioManager;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3d;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class CharacterSoundSystem implements EventHandler {
    
    private FastRandom random;
    
    @ReceiveEvent(components = {CharacterSoundComponent.class})
    public void footstep(FootstepEvent event, EntityRef entity) {
        if (random == null) return;

        LocationComponent location = entity.getComponent(LocationComponent.class);
        if (location != null) {
            CharacterSoundComponent characterSounds = entity.getComponent(CharacterSoundComponent.class);
            if (characterSounds.footstepSounds.length > 0) {
                Sound sound = characterSounds.footstepSounds[random.randomIntAbs(characterSounds.footstepSounds.length)];
                AudioManager.play(sound, new Vector3d(LocationHelper.localToWorldPos(location)), characterSounds.footstepVolume, AudioManager.PRIORITY_NORMAL);
            }
        }
    }

    public FastRandom getRandom() {
        return random;
    }

    public void setRandom(FastRandom random) {
        this.random = random;
    }
}
