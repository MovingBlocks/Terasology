package org.terasology.audio;

import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.audio.events.PlaySoundForOwnerEvent;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.logic.players.LocalPlayer;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkSystem;

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
