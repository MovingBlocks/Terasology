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
package org.terasology.logic.characters;

import org.terasology.audio.AudioManager;
import org.terasology.audio.StaticSound;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.FootstepEvent;
import org.terasology.logic.characters.events.HorizontalCollisionEvent;
import org.terasology.logic.characters.events.JumpEvent;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.characters.events.SwimStrokeEvent;
import org.terasology.logic.characters.events.VerticalCollisionEvent;
import org.terasology.logic.health.DamageSoundComponent;
import org.terasology.logic.health.DestroyEvent;
import org.terasology.logic.health.HealthComponent;
import org.terasology.logic.health.OnDamagedEvent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;

import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterSystem(RegisterMode.ALWAYS)
public class CharacterSoundSystem extends BaseComponentSystem {

    private static final long MIN_TIME = 10;
    private Random random = new FastRandom();

    @In
    private Time time;

    @In
    private AudioManager audioManager;

    @ReceiveEvent
    public void onFootstep(FootstepEvent event, EntityRef entity, CharacterSoundComponent characterSounds) {
        if (characterSounds.footstepSounds.size() > 0 && characterSounds.lastSoundTime + MIN_TIME < time.getGameTimeInMs()) {
            StaticSound sound = random.nextItem(characterSounds.footstepSounds);
            entity.send(new PlaySoundEvent(entity, sound, characterSounds.footstepVolume));
            characterSounds.lastSoundTime = time.getGameTimeInMs();
            entity.saveComponent(characterSounds);
        }
    }

    @ReceiveEvent
    public void onJump(JumpEvent event, EntityRef entity, CharacterSoundComponent characterSounds) {
        if (characterSounds.lastSoundTime + MIN_TIME < time.getGameTimeInMs()) {
            StaticSound sound = null;
            if (characterSounds.jumpSounds.size() > 0) {
                sound = random.nextItem(characterSounds.jumpSounds);
            } else if (characterSounds.footstepSounds.size() > 0) {
                sound = random.nextItem(characterSounds.footstepSounds);
            }
            if (sound != null) {
                entity.send(new PlaySoundEvent(entity, sound, characterSounds.jumpVolume));
                characterSounds.lastSoundTime = time.getGameTimeInMs();
                entity.saveComponent(characterSounds);
            }
        }
    }

    @ReceiveEvent
    public void onLanded(VerticalCollisionEvent event, EntityRef entity, CharacterSoundComponent characterSounds) {
        if (event.getVelocity().y > 0f) {
            return;
        }

        if (characterSounds.lastSoundTime + MIN_TIME < time.getGameTimeInMs()) {
            StaticSound sound = null;
            if (characterSounds.landingSounds.size() > 0) {
                sound = random.nextItem(characterSounds.landingSounds);
            } else if (characterSounds.footstepSounds.size() > 0) {
                sound = random.nextItem(characterSounds.footstepSounds);
            }
            if (sound != null) {
                entity.send(new PlaySoundEvent(entity, sound, characterSounds.landingVolume));
                characterSounds.lastSoundTime = time.getGameTimeInMs();
                entity.saveComponent(characterSounds);
            }
        }
    }

    @ReceiveEvent
    public void onCrash(HorizontalCollisionEvent event, EntityRef entity, CharacterSoundComponent characterSounds, HealthComponent healthComponent) {
        Vector3f horizVelocity = new Vector3f(event.getVelocity());
        horizVelocity.y = 0;
        float velocity = horizVelocity.length();

        if (velocity > healthComponent.horizontalDamageSpeedThreshold) {
            if (characterSounds.lastSoundTime + MIN_TIME < time.getGameTimeInMs()) {
                StaticSound sound = random.nextItem(characterSounds.landingSounds);
                if (sound != null) {
                    entity.send(new PlaySoundEvent(entity, sound, characterSounds.landingVolume));
                    characterSounds.lastSoundTime = time.getGameTimeInMs();
                    entity.saveComponent(characterSounds);
                }
            }
        }
    }

    @ReceiveEvent
    public void onDamaged(OnDamagedEvent event, EntityRef entity, CharacterSoundComponent characterSounds) {
        if (characterSounds.lastSoundTime + MIN_TIME < time.getGameTimeInMs()) {
            DamageSoundComponent damageSounds = event.getType().getComponent(DamageSoundComponent.class);
            StaticSound sound = null;
            if (damageSounds != null && !damageSounds.sounds.isEmpty()) {
                sound = random.nextItem(damageSounds.sounds);
            } else if (!characterSounds.damageSounds.isEmpty()) {
                sound = random.nextItem(characterSounds.damageSounds);
            }

            if (sound != null) {
                entity.send(new PlaySoundEvent(entity, sound, characterSounds.damageVolume));
                characterSounds.lastSoundTime = time.getGameTimeInMs();
                entity.saveComponent(characterSounds);
            }
        }
    }

    @ReceiveEvent
    public void onDeath(DestroyEvent event, EntityRef entity, CharacterSoundComponent characterSounds) {
        if (characterSounds.deathSounds.size() > 0) {
            StaticSound sound = random.nextItem(characterSounds.deathSounds);
            entity.send(new PlaySoundEvent(entity, sound, characterSounds.deathVolume));
        }
    }

    @ReceiveEvent
    public void onRespawn(OnPlayerSpawnedEvent event, EntityRef character, CharacterSoundComponent characterSounds) {
        if (characterSounds.respawnSounds.size() > 0) {
            StaticSound sound = random.nextItem(characterSounds.respawnSounds);
            character.send(new PlaySoundEvent(character, sound, characterSounds.respawnVolume));
        }
    }

    @ReceiveEvent
    public void onSwimStroke(SwimStrokeEvent event, EntityRef entity, CharacterSoundComponent characterSounds) {
        if (characterSounds.swimSounds.size() > 0 && characterSounds.lastSoundTime + MIN_TIME < time.getGameTimeInMs()) {
            StaticSound sound = random.nextItem(characterSounds.swimSounds);
            entity.send(new PlaySoundEvent(entity, sound, characterSounds.swimmingVolume));
            characterSounds.lastSoundTime = time.getGameTimeInMs();
            entity.saveComponent(characterSounds);
        }
    }

    @ReceiveEvent
    public void onEnterBlock(OnEnterBlockEvent event, EntityRef entity, CharacterSoundComponent characterSounds) {
        // only play this sound if the feet hit the water
        if (event.getCharacterRelativePosition().y == 0 && characterSounds.lastSoundTime + MIN_TIME < time.getGameTimeInMs()) {
            boolean oldBlockIsLiquid = event.getOldBlock().isLiquid();
            boolean newBlockIsLiquid = event.getNewBlock().isLiquid();
            StaticSound sound = null;
            if (!oldBlockIsLiquid && newBlockIsLiquid) {
                sound = random.nextItem(characterSounds.enterWaterSounds);
            } else if (oldBlockIsLiquid && !newBlockIsLiquid) {
                sound = random.nextItem(characterSounds.leaveWaterSounds);
            }
            if (sound != null) {
                entity.send(new PlaySoundEvent(entity, sound, characterSounds.diveVolume));
                characterSounds.lastSoundTime = time.getGameTimeInMs();
                entity.saveComponent(characterSounds);
            }
        }
    }


}
