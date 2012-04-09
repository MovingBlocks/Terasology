package org.terasology.componentSystem.action;

import org.terasology.audio.Sound;
import org.terasology.components.actions.PlaySoundActionComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.events.ActivateEvent;
import org.terasology.logic.manager.AudioManager;
import org.terasology.logic.manager.SoundManager;
import org.terasology.utilities.FastRandom;

/**
 * @author Immortius <immortius@gmail.com>
 */
public class PlaySoundAction implements EventHandlerSystem {
    
    private FastRandom random = new FastRandom();
    
    public void initialise() {
        
    }
    
    @ReceiveEvent(components={PlaySoundActionComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        PlaySoundActionComponent playSound = entity.getComponent(PlaySoundActionComponent.class);
        if (playSound.sounds.size() > 0) {
            Sound sound = playSound.sounds.get(random.randomIntAbs(playSound.sounds.size()));
            AudioManager.play(sound, event.getLocation(), playSound.volume, SoundManager.PRIORITY_NORMAL);
        }
    }
}
