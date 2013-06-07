/*
 * Copyright 2013 Moving Blocks
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

package org.terasology.audio;

import org.terasology.asset.Assets;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.audio.events.PlaySoundForOwnerEvent;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.console.Command;
import org.terasology.logic.console.CommandParam;
import org.terasology.logic.location.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.systems.In;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;

import javax.vecmath.Vector3f;

/**
 * @author Immortius
 */
@RegisterSystem
public class AudioSystem implements UpdateSubscriberSystem {

    @In
    private NetworkSystem networkSystem;
    @In
    private LocalPlayer localPlayer;
    @In
    private AudioManager audioManager;


    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Command(shortDescription = "Toggle muting all sound")
    public String mute() {
        audioManager.setMute(!audioManager.isMute());
        return "All sound is now " + ((audioManager.isMute()) ? "muted." : "unmuted.");
    }

    @Command(shortDescription = "Plays a test sound")
    public void playTestSound(@CommandParam("xOffset") float xOffset, @CommandParam("zOffset") float zOffset) {
        Vector3f position = localPlayer.getPosition();
        position.x += xOffset;
        position.z += zOffset;
        audioManager.playSound(Assets.getSound("engine:dig"), position);
    }

    @ReceiveEvent(components = {})
    public void onPlaySound(PlaySoundEvent playSoundEvent, EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        if (location != null) {
            audioManager.playSound(playSoundEvent.getSound(), location.getWorldPosition(), playSoundEvent.getVolume(), AudioManager.PRIORITY_NORMAL);
        } else {
            audioManager.playSound(playSoundEvent.getSound(), playSoundEvent.getVolume());
        }
    }

    @ReceiveEvent(components = {})
    public void onPlaySound(PlaySoundForOwnerEvent playSoundEvent, EntityRef entity) {
        ClientComponent clientComponent = networkSystem.getOwnerEntity(entity).getComponent(ClientComponent.class);
        if (clientComponent != null && !clientComponent.local) {
            return;
        }
        audioManager.playSound(playSoundEvent.getSound(), playSoundEvent.getVolume(), AudioManager.PRIORITY_HIGH);
    }

    @Override
    public void update(float delta) {
        audioManager.updateListener(localPlayer.getPosition(), localPlayer.getViewRotation(), localPlayer.getVelocity());
    }
}
