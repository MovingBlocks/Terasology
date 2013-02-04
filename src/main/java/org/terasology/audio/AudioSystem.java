package org.terasology.audio;

import org.terasology.audio.events.AbstractPlaySoundEvent;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.audio.events.PlaySoundForOwnerEvent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.ComponentSystem;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.logic.manager.SoundManager;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;

/**
 * @author Immortius
 */
@RegisterSystem
public class AudioSystem implements ComponentSystem {

    @In
    private NetworkSystem networkSystem;


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
            AudioManager.play(playSoundEvent.getSound(), location.getWorldPosition(), playSoundEvent.getVolume(), SoundManager.PRIORITY_NORMAL);
        } else {
            AudioManager.play(playSoundEvent.getSound(), playSoundEvent.getVolume());
        }
    }

    @ReceiveEvent(components = {})
    public void onPlaySound(PlaySoundForOwnerEvent playSoundEvent, EntityRef entity) {
        ClientComponent clientComponent = networkSystem.getOwnerEntity(playSoundEvent.getInstigator()).getComponent(ClientComponent.class);
        if (clientComponent != null && !clientComponent.local) {
            return;
        }
        AudioManager.play(playSoundEvent.getSound(), playSoundEvent.getVolume());
    }

}
