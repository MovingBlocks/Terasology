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

package org.terasology.audio.system;

import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.terasology.utilities.Assets;
import org.terasology.audio.AudioManager;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.audio.events.PlaySoundForOwnerEvent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;

/**
 * This system handles receiving the PlaySound events and activating the AudioManager to play them
 */
@RegisterSystem
public class AudioSystem extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private NetworkSystem networkSystem;
    @In
    private LocalPlayer localPlayer;
    @In
    private AudioManager audioManager;

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
    public void playTestSound(@Sender EntityRef sender, @CommandParam("xOffset") float xOffset, @CommandParam("zOffset") float zOffset) {
Vector3f position = localPlayer.getPosition(new Vector3f());
        position.x += xOffset;
        position.z += zOffset;
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
            localPlayer.getPosition(new Vector3f()),
            localPlayer.getViewRotation(new Quaternionf()),
            localPlayer.getVelocity(new Vector3f()));
    }
}
