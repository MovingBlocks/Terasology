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

import org.joml.Vector3f;
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
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

/**
 * This systems handles the playing of sounds in response to events, on both server and client,
 * allowing the prediction system a chance to play expected sounds on time even if the server is lagging
 */
@RegisterSystem(RegisterMode.ALWAYS)
public class PlaySoundAction extends BaseComponentSystem {

    private Random random = new FastRandom();

    @In
    private AudioManager audioManager;

    @In
    private LocalPlayer localPlayer;

    /**
     * This method plays sound in prediction for preventing not playing song because of server lags
     *
     * @param event contains the details for the predicted event, used here for location purposes
     * @param entity is source of the playsound
     */
    @ReceiveEvent(components = {PlaySoundActionComponent.class})
    public void onActivationPredicted(ActivationPredicted event, EntityRef entity) {
        PlaySoundActionComponent playSound = entity.getComponent(PlaySoundActionComponent.class);
        StaticSound sound = random.nextItem(playSound.sounds);
        if (sound != null) {
            Vector3f pos;
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
     * This method plays sound if it wasn't played by prediction system
     *
     * @param event contains the details for the active event, used here for location purposes
     * @param entity is source of the playsound
     */
    @ReceiveEvent(components = {PlaySoundActionComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        if (event.getInstigator().equals(localPlayer.getCharacterEntity())) {
            return; // owner has heard sound from prediction
        }
        PlaySoundActionComponent playSound = entity.getComponent(PlaySoundActionComponent.class);
        StaticSound sound = random.nextItem(playSound.sounds);
        if (sound != null) {
            Vector3f pos;
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
