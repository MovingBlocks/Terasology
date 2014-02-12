/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.logic.actions;

import org.terasology.audio.AudioManager;
import org.terasology.audio.Sound;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterSystem(RegisterMode.ALWAYS)
public class PlaySoundAction extends BaseComponentSystem {

    private Random random = new FastRandom();

    @In
    private AudioManager audioManager;

    @ReceiveEvent(components = {PlaySoundActionComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        PlaySoundActionComponent playSound = entity.getComponent(PlaySoundActionComponent.class);
        Sound sound = random.nextItem(playSound.sounds);
        if (sound != null) {
            Vector3f pos = null;
            switch (playSound.relativeTo) {
                case Instigator:
                    pos = event.getInstigatorLocation();
                    break;
                case Target:
                    pos = event.getTargetLocation();
                    break;
            }
            if (pos == null) {
                pos = event.getOrigin();
            }
            audioManager.playSound(sound, pos, playSound.volume, AudioManager.PRIORITY_NORMAL);
        }
    }
}
