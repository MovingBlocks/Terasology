/*
 * Copyright 2013 Benjamin Glatzel <benjamin.glatzel@me.com>
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

import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.logic.LocalPlayer;

/**
 * @author Immortius
 */
@RegisterComponentSystem
public class AudioSystem implements UpdateSubscriberSystem, EventHandlerSystem {

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

    @ReceiveEvent(components = {})
    public void onPlaySound(PlaySoundEvent playSoundEvent, EntityRef entity) {
        LocationComponent location = entity.getComponent(LocationComponent.class);
        if (location != null) {
            audioManager.playSound(playSoundEvent.getSound(), location.getWorldPosition(), playSoundEvent.getVolume(), AudioManager.PRIORITY_NORMAL);
        } else {
            audioManager.playSound(playSoundEvent.getSound(), playSoundEvent.getVolume());
        }
    }

    @Override
    public void update(float delta) {
        audioManager.updateListener(localPlayer.getPosition(), localPlayer.getViewRotation(), localPlayer.getVelocity());
    }
}
