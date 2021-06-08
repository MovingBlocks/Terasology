// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.audio.system;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.audio.AudioManager;
import org.terasology.engine.audio.events.PlaySoundEvent;
import org.terasology.engine.audio.events.PlaySoundForOwnerEvent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.permission.PermissionManager;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.Assets;

/**
 * This system handles receiving the PlaySound events and activating the AudioManager to play them
 */
@RegisterSystem
public class AudioSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(AudioSystem.class);

    @In
    private NetworkSystem networkSystem;
    @In
    private LocalPlayer localPlayer;
    @In
    private AudioManager audioManager;

    // reuse same vector and quaternion instances when updating listeners during delta update.
    private final Vector3f playerPosition = new Vector3f();
    private final Vector3f playerVelocity = new Vector3f();
    private final Quaternionf playerViewRotation = new Quaternionf();

    /**
     * Toggles the muting of sounds.
     *
     * @param sender The entity requesting the mute
     * @return A human readable string of the mute status
     */
    @Command(shortDescription = "Toggle muting all sound", requiredPermission = PermissionManager.NO_PERMISSION)
    public String mute(@Sender EntityRef sender) {
        audioManager.setMute(!audioManager.isMute());
        return "All sound is now " + ((audioManager.isMute()) ? "muted." : "unmuted.");
    }

    /**
     * Plays a test dig sound at an offset from the player in the x and z axis.
     *
     * @param sender The entity sending the sound request
     * @param xOffset The x axis offset from the player to play the sound at.
     * @param zOffset The z axis offset from the player to play the sound at.
     */
    @Command(shortDescription = "Plays a test sound")
    public void playTestSound(@Sender EntityRef sender,
                              @CommandParam("xOffset") float xOffset,
                              @CommandParam("zOffset") float zOffset) {
        Vector3f position = localPlayer.getPosition(new Vector3f());
        position.add(xOffset, 0, zOffset);
        audioManager.playSound(Assets.getSound("engine:dig").get(), position);
    }

    /**
     * Receives the sound played event and calls on the AudioManager to play it.
     *
     * @param playSoundEvent The sound event.
     * @param entity The entity that instigated the event.
     */
    @ReceiveEvent
    public void onPlaySound(PlaySoundEvent playSoundEvent, EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        if (location != null) {
            Vector3f pos = location.getWorldPosition(new Vector3f());
            if (pos.isFinite()) {
                audioManager.playSound(playSoundEvent.getSound(), pos, playSoundEvent.getVolume(), AudioManager.PRIORITY_NORMAL);
                return;
            } else {
                logger.warn("Can't play sound with non-finite position/rotation?! Entity: {}", entity);
            }
        }
        audioManager.playSound(playSoundEvent.getSound(), playSoundEvent.getVolume());
    }

    /**
     * Receives an event send when a sound should be played for the entity owner as well. Calls on the AudioManager to
     * play it.
     *
     * @param playSoundEvent The sound event.
     * @param entity The entity that instigated the event.
     */
    @ReceiveEvent
    public void onPlaySound(PlaySoundForOwnerEvent playSoundEvent, EntityRef entity) {
        ClientComponent clientComponent = networkSystem.getOwnerEntity(entity).getComponent(ClientComponent.class);
        if (clientComponent != null && !clientComponent.local) {
            return;
        }
        audioManager.playSound(playSoundEvent.getSound(), playSoundEvent.getVolume(), AudioManager.PRIORITY_HIGH);
    }

    @Override
    public void update(float delta) {
        audioManager.updateListener(
            localPlayer.getPosition(playerPosition),
            localPlayer.getViewRotation(playerViewRotation),
            localPlayer.getVelocity(playerVelocity));
    }
}
