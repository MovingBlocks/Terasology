/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.componentSystem.characters;

import org.terasology.audio.Sound;
import org.terasology.audio.AudioManager;
import org.terasology.components.CharacterSoundComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.events.FootstepEvent;
import org.terasology.events.JumpEvent;
import org.terasology.events.VerticalCollisionEvent;
import org.terasology.utilities.FastRandom;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterSystem(RegisterMode.ALWAYS)
public class CharacterSoundSystem implements ComponentSystem {

    private FastRandom random = new FastRandom();

    @In
    private AudioManager audioManager;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CharacterSoundComponent.class})
    public void footstep(FootstepEvent event, EntityRef entity) {
        if (random == null) return;

        LocationComponent location = entity.getComponent(LocationComponent.class);
        if (location != null) {
            CharacterSoundComponent characterSounds = entity.getComponent(CharacterSoundComponent.class);
            if (characterSounds.footstepSounds.size() > 0) {
                Sound sound = characterSounds.footstepSounds.get(random.randomIntAbs(characterSounds.footstepSounds.size()));
                audioManager.playSound(sound, location.getWorldPosition(), characterSounds.footstepVolume, AudioManager.PRIORITY_NORMAL);
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
                audioManager.playSound(sound, location.getWorldPosition(), 0.8f, AudioManager.PRIORITY_NORMAL);
            }
        }
    }

    @ReceiveEvent(components = {CharacterSoundComponent.class})
    public void landed(VerticalCollisionEvent event, EntityRef entity) {
        if (random == null || event.getVelocity().y > 0f) return;


        CharacterSoundComponent characterSounds = entity.getComponent(CharacterSoundComponent.class);
        if (characterSounds.footstepSounds.size() > 0) {
            Sound sound = characterSounds.footstepSounds.get(random.randomIntAbs(characterSounds.footstepSounds.size()));
            audioManager.playSound(sound, event.getLocation(), 1.0f, AudioManager.PRIORITY_NORMAL);
        }
    }

}
