package org.terasology.componentSystem.characters;

import org.terasology.audio.Sound;
import org.terasology.components.CharacterSoundComponent;
import org.terasology.components.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.FootstepEvent;
import org.terasology.events.JumpEvent;
import org.terasology.events.VerticalCollisionEvent;
import org.terasology.logic.manager.AudioManager;
import org.terasology.logic.manager.SoundManager;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3d;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterComponentSystem
public class CharacterSoundSystem implements EventHandlerSystem {
    
    private FastRandom random = new FastRandom();

    public void initialise() {
    }
    
    @ReceiveEvent(components = {CharacterSoundComponent.class})
    public void footstep(FootstepEvent event, EntityRef entity) {
        if (random == null) return;

        LocationComponent location = entity.getComponent(LocationComponent.class);
        if (location != null) {
            CharacterSoundComponent characterSounds = entity.getComponent(CharacterSoundComponent.class);
            if (characterSounds.footstepSounds.size() > 0) {
                Sound sound = characterSounds.footstepSounds.get(random.randomIntAbs(characterSounds.footstepSounds.size()));
                AudioManager.play(sound, new Vector3d(location.getWorldPosition()), characterSounds.footstepVolume, SoundManager.PRIORITY_NORMAL);
            }
        }
    }

    @ReceiveEvent(components = {CharacterSoundComponent.class})
    public void jump(JumpEvent event, EntityRef entity) {
        if (random == null) return;

        LocationComponent location = entity.getComponent(LocationComponent.class);
        if (location != null) {
            CharacterSoundComponent characterSounds = entity.getComponent(CharacterSoundComponent.class);
            if (characterSounds.footstepSounds.size() > 0) {
                Sound sound = characterSounds.footstepSounds.get(random.randomIntAbs(characterSounds.footstepSounds.size()));
                AudioManager.play(sound, new Vector3d(location.getWorldPosition()), 0.8f, SoundManager.PRIORITY_NORMAL);
            }
        }
    }

    @ReceiveEvent(components = {CharacterSoundComponent.class})
    public void landed(VerticalCollisionEvent event, EntityRef entity) {
        if (random == null || event.getVelocity().y > 0f) return;


        CharacterSoundComponent characterSounds = entity.getComponent(CharacterSoundComponent.class);
        if (characterSounds.footstepSounds.size() > 0) {
            Sound sound = characterSounds.footstepSounds.get(random.randomIntAbs(characterSounds.footstepSounds.size()));
            AudioManager.play(sound, event.getLocation(), 1.0f, SoundManager.PRIORITY_NORMAL);
        }
    }

}
