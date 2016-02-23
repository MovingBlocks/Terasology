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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.audio.StaticSound;
import org.terasology.audio.events.PlaySoundEvent;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.FootstepEvent;
import org.terasology.logic.characters.events.JumpEvent;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.characters.events.SwimStrokeEvent;
import org.terasology.logic.characters.events.VerticalCollisionEvent;
import org.terasology.logic.health.DoDestroyEvent;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.players.event.OnPlayerSpawnedEvent;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

import java.util.List;

/**
 */
@RegisterSystem(RegisterMode.ALWAYS)
public class CharacterSoundSystem extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(CharacterSoundSystem.class);

    public static final long MIN_TIME = 10;
    private static final float LANDING_VOLUME_MODIFIER = 0.2f; //The sound volume is multiplied by this number
    private static final float LANDING_VELOCITY_THRESHOLD = 7; //How fast do you have to be falling for the sound to play
    private static final float LANDING_VOLUME_MAX = 2; //The maximum modifier value
    private Random random = new FastRandom();

    @In
    private Time time;

    @In
    private WorldProvider worldProvider;

    @ReceiveEvent
    public void onFootstep(FootstepEvent event, EntityRef entity, LocationComponent locationComponent, CharacterSoundComponent characterSounds) {

        List<StaticSound> footstepSounds = characterSounds.footstepSounds;

        // Check if the block the character is standing on has footstep sounds
        Vector3i blockPos = new Vector3i(locationComponent.getLocalPosition());
        blockPos.y--; // The block *below* the character's feet is interesting to us
        Block block = worldProvider.getBlock(blockPos);
        if (block != null) {
            if (block.getSounds() == null) {
                logger.error("Block '{}' has no sounds", block.getURI());
            } else if (!block.getSounds().getStepSounds().isEmpty()) {
                footstepSounds = block.getSounds().getStepSounds();
            }
        }

        if (footstepSounds.size() > 0 && characterSounds.lastSoundTime + MIN_TIME < time.getGameTimeInMs()) {
            StaticSound sound = random.nextItem(footstepSounds);
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
        Vector3f velocity = event.getVelocity();
        float soundVolumeModifier = (velocity.y * -1 - LANDING_VELOCITY_THRESHOLD) * LANDING_VOLUME_MODIFIER;

        if (soundVolumeModifier <= 0f) {
            return;
        }

        if (soundVolumeModifier > LANDING_VOLUME_MAX) {
            soundVolumeModifier = LANDING_VOLUME_MAX;
        }

        if (characterSounds.lastSoundTime + MIN_TIME < time.getGameTimeInMs()) {
            StaticSound sound = null;
            if (characterSounds.landingSounds.size() > 0) {
                sound = random.nextItem(characterSounds.landingSounds);
            } else if (characterSounds.footstepSounds.size() > 0) {
                sound = random.nextItem(characterSounds.footstepSounds);
            }
            if (sound != null) {
                entity.send(new PlaySoundEvent(entity, sound, characterSounds.landingVolume * soundVolumeModifier));
                characterSounds.lastSoundTime = time.getGameTimeInMs();
                entity.saveComponent(characterSounds);
            }
        }
    }

    @ReceiveEvent
    public void onDeath(DoDestroyEvent event, EntityRef entity, CharacterSoundComponent characterSounds) {
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
