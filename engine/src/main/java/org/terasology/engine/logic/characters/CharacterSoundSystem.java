// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.characters;

import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.audio.StaticSound;
import org.terasology.engine.audio.events.PlaySoundEvent;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.events.FootstepEvent;
import org.terasology.engine.logic.characters.events.JumpEvent;
import org.terasology.engine.logic.characters.events.OnEnterBlockEvent;
import org.terasology.engine.logic.characters.events.PlayerDeathEvent;
import org.terasology.engine.logic.characters.events.SwimStrokeEvent;
import org.terasology.engine.logic.characters.events.VerticalCollisionEvent;
import org.terasology.engine.logic.health.DoDestroyEvent;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.players.event.OnPlayerRespawnedEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

import java.util.List;

@RegisterSystem(RegisterMode.ALWAYS)
public class CharacterSoundSystem extends BaseComponentSystem {

    public static final long MIN_TIME = 10;

    private static final Logger logger = LoggerFactory.getLogger(CharacterSoundSystem.class);
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
        Vector3i blockPos = new Vector3i(locationComponent.getLocalPosition(), RoundingMode.FLOOR);
        blockPos.y--; // The block *below* the character's feet is interesting to us
        Block block = worldProvider.getBlock(blockPos);
        if (block != null) {
            if (block.getSounds() == null) {
                logger.error("Block '{}' has no sounds", block.getURI()); //NOPMD
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
    public void onPlayerDeath(PlayerDeathEvent event, EntityRef character) {
        CharacterSoundComponent characterSounds = character.getComponent(CharacterSoundComponent.class);
        if (characterSounds.deathSounds.size() > 0) {
            StaticSound sound = random.nextItem(characterSounds.deathSounds);
            character.send(new PlaySoundEvent(character, sound, characterSounds.deathVolume));
        }
    }

    @ReceiveEvent
    public void onRespawn(OnPlayerRespawnedEvent event, EntityRef character, CharacterSoundComponent characterSounds) {
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
