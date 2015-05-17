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
import org.terasology.audio.StaticSound;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.ActivationPredicted;
import org.terasology.logic.common.ActivateEvent;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.math.geom.Vector3f;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

/*
 * Class that handle the sounds of the game, acoording to the events received.
 */
@RegisterSystem(RegisterMode.ALWAYS)
public class PlaySoundAction extends BaseComponentSystem {

    private Random random = new FastRandom();

    @In
    private AudioManager audioManager;

    @In
    private LocalPlayer localPlayer;

    /**
     * @param event An instance of the ActivationPredicted event
     * @param entity An instance of EntityRef
     * Deal with the predicted activation events and play a sound.
     */
    @ReceiveEvent(components = {PlaySoundActionComponent.class})
    public void onActivationPredicted(ActivationPredicted event, EntityRef entity) {
        PlaySoundActionComponent playSound = entity.getComponent(PlaySoundActionComponent.class);
        StaticSound sound = random.nextItem(playSound.sounds);
        if (sound != null) {
            Vector3f pos = null;
            switch (playSound.relativeTo) {
                case Target:
                    pos = event.getTargetLocation();
                    break;
                default:
                    pos = event.getInstigatorLocation();
                    break;
            }
            if (pos == null) {
                pos = event.getOrigin();
            }
            audioManager.playSound(sound, pos, playSound.volume, AudioManager.PRIORITY_NORMAL);
        }
    }

    /**
     * @param event An instance of the ActivateEvent event
     * @param entity An instance of EntityRef
     * Deal with the real activation event (No predicted) and play a sound.
     */
    @ReceiveEvent(components = {PlaySoundActionComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        if (event.getInstigator().equals(localPlayer.getCharacterEntity())) {
            return; // owner has heard sound from prediction
        }
        PlaySoundActionComponent playSound = entity.getComponent(PlaySoundActionComponent.class);
        StaticSound sound = random.nextItem(playSound.sounds);
        verifyAndPlaySound(event, playSound, sound);
    }

	/**
	 * @param event ActivateEvent from the onActivate method
	 * @param playSound ActionComponent for playing a sound
	 * @param sound StaticSound instance for a sound
	 * Helper method for the activation of a sound. Check for the current position and play a sound according to it.
	 */
	private void verifyAndPlaySound(ActivateEvent event,
			PlaySoundActionComponent playSound, StaticSound sound) {
		if (sound != null) {
            Vector3f pos = null;
            switch (playSound.relativeTo) {
                case Target:
                    pos = event.getTargetLocation();
                    break;
                default:
                    pos = event.getInstigatorLocation();
                    break;
            }
            if (pos == null) {
                pos = event.getOrigin();
            }
            audioManager.playSound(sound, pos, playSound.volume, AudioManager.PRIORITY_NORMAL);
        }
	}
}
