// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.actions;

import org.joml.Vector3f;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.events.ActivationPredicted;
import org.terasology.engine.logic.common.ActivateEvent;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

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
    @ReceiveEvent(components = PlaySoundActionComponent.class)
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
    @ReceiveEvent(components = PlaySoundActionComponent.class)
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
