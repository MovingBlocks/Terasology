package org.terasology.componentSystem.action;

import org.terasology.audio.Sound;
import org.terasology.components.actions.PlaySoundActionComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.logic.manager.AudioManager;
import org.terasology.logic.manager.SoundManager;
import org.terasology.utilities.FastRandom;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterComponentSystem
public class PlaySoundAction implements EventHandlerSystem {
    
    private FastRandom random = new FastRandom();
    
    public void initialise() {
        
    }
    
    @ReceiveEvent(components={PlaySoundActionComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        PlaySoundActionComponent playSound = entity.getComponent(PlaySoundActionComponent.class);
        if (playSound.sounds.size() > 0) {
            Sound sound = playSound.sounds.get(random.randomIntAbs(playSound.sounds.size()));
            Vector3f pos = null;
            switch (playSound.relativeTo) {
                case Instigator:
                    pos = event.getInstigatorLocation();
                    break;
                case Target:
                    pos = event.getTargetLocation();
                    break;
            }
            if (pos == null) {
                pos = event.getOrigin();
            }
            AudioManager.play(sound, pos, playSound.volume, SoundManager.PRIORITY_NORMAL);
        }
    }
}
