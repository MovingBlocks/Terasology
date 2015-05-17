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

/*
 * Class that sends the PlaySound event and saves the SoundComponent to the entity
 */
@RegisterSystem(RegisterMode.ALWAYS)
public class CharacterSoundSystem extends BaseComponentSystem {

    private static final long MIN_TIME = 10;
    private static final float LANDING_VOLUME_MODIFIER = 0.2f; //The sound volume is multiplied by this number
    private static final float LANDING_VELOCITY_THRESHOLD = 7; //How fast do you have to be falling for the sound to play
    private static final float LANDING_VOLUME_MAX = 2; //The maximum modifier value
    private Random random = new FastRandom();

    @In
    private Time time;

    @In
    private AudioManager audioManager;

    @In
    private WorldProvider worldProvider;

    /**
     * @param event Instance of *FootstepEvent* event
     * @param entity Instance of EntityRef
     * @param locationComponent Component related to the location of the character
     * @param characterSounds Component related to the sound of the character
     * Checks the block below the character and set the proper *Step sound* into the game
     */
    @ReceiveEvent
    public void onFootstep(FootstepEvent event, EntityRef entity, LocationComponent locationComponent, CharacterSoundComponent characterSounds) {

        List<StaticSound> footstepSounds = characterSounds.footstepSounds;

        // Check if the block the character is standing on has footstep sounds
        Vector3i blockPos = new Vector3i(locationComponent.getLocalPosition());
        blockPos.y--; // The block *below* the character's feet is interesting to us
        Block block = worldProvider.getBlock(blockPos);
        handleFootstepSoundBlock(entity, characterSounds, footstepSounds, block);
    }

	/**
	 * @param entity Instance of EntityRef
	 * @param characterSounds Component related to the sound of the character
	 * @param footstepSounds List of *StaticSound*, containing footstepSounds
	 * @param block The block that's below the character's feet
	 * Helper method of *onFootstep*. Sends the new event and save the sound into the character
	 */
	private void handleFootstepSoundBlock(EntityRef entity,
			CharacterSoundComponent characterSounds,
			List<StaticSound> footstepSounds, Block block) {
		if (block != null && !block.getSounds().getStepSounds().isEmpty()) {
            footstepSounds = block.getSounds().getStepSounds();
        }

        if (footstepSounds.size() > 0 && characterSounds.lastSoundTime + MIN_TIME < time.getGameTimeInMs()) {
            StaticSound sound = random.nextItem(footstepSounds);
            entity.send(new PlaySoundEvent(entity, sound, characterSounds.footstepVolume));
            characterSounds.lastSoundTime = time.getGameTimeInMs();
            entity.saveComponent(characterSounds);
        }
	}

    /**
     * @param event Instance of the JumpEvent
     * @param entity Instance of EntityRef
     * @param characterSounds Component of the character sounds
     * Receives the jump event, plays the proper sound, and save it into the character sounds.
     */
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

    /**
     * @param event Instance of a vertical collision event
     * @param entity Instance of EntityRef
     * @param characterSounds Component of the character sounds
     * Receives the landed event (*VerticalCollisionEvent*), plays the proper sound, and save it into the character sounds.
     */
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

        handleOnLandedSound(entity, characterSounds, soundVolumeModifier);
    }

	/**
	 * @param entity Instance of EntityRef
	 * @param characterSounds Component of the sounds of the character
	 * @param soundVolumeModifier The current volume of the sound
	 * Helper method of *onLanded*. Sends the new event and saves the new sound into the character.
	 */
	private void handleOnLandedSound(EntityRef entity,
			CharacterSoundComponent characterSounds, float soundVolumeModifier) {
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

    /**
     * @param event Instance of a crash (*HorizontalCollisionEvent*) event
     * @param entity Instance of EntityRef
     * @param characterSounds Component that contains the character sound
     * @param healthComponent Component that contains the health of the character
     * Receives the crash event (*HorizontalCollisionEvent*), plays the proper sound, and save it into the character sounds.
     */
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

    /**
     * @param event Instance of Damage event
     * @param entity Instance of EntityRef
     * @param characterSounds Component that contains the character sound
     * Receives the damaged event, plays the proper sound, and save it into the character sounds.
     */
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

    /**
     * @param event Instance of Death event
     * @param entity Instance of EntityRef
     * @param characterSounds Component that contains the character sound
     * Receives the death event, plays the proper sound, and save it into the character sounds.
     */
    @ReceiveEvent
    public void onDeath(DestroyEvent event, EntityRef entity, CharacterSoundComponent characterSounds) {
        if (characterSounds.deathSounds.size() > 0) {
            StaticSound sound = random.nextItem(characterSounds.deathSounds);
            entity.send(new PlaySoundEvent(entity, sound, characterSounds.deathVolume));
        }
    }

    /**
     * @param event Instace of Respawn event
     * @param character Instance of EntityRef
     * @param characterSounds Component that contains the character sound
     * Receives the respawn event, plays the proper sound, and save it into the character sounds.
     */
    @ReceiveEvent
    public void onRespawn(OnPlayerSpawnedEvent event, EntityRef character, CharacterSoundComponent characterSounds) {
        if (characterSounds.respawnSounds.size() > 0) {
            StaticSound sound = random.nextItem(characterSounds.respawnSounds);
            character.send(new PlaySoundEvent(character, sound, characterSounds.respawnVolume));
        }
    }

    /**
     * @param event Instance of Swimming event
     * @param entity Instance of EntityRef
     * @param characterSounds Component that contains the character sound
     * Receives the swimming event, plays the proper sound, and save it into the character sounds.
     */
    @ReceiveEvent
    public void onSwimStroke(SwimStrokeEvent event, EntityRef entity, CharacterSoundComponent characterSounds) {
        if (characterSounds.swimSounds.size() > 0 && characterSounds.lastSoundTime + MIN_TIME < time.getGameTimeInMs()) {
            StaticSound sound = random.nextItem(characterSounds.swimSounds);
            entity.send(new PlaySoundEvent(entity, sound, characterSounds.swimmingVolume));
            characterSounds.lastSoundTime = time.getGameTimeInMs();
            entity.saveComponent(characterSounds);
        }
    }

    /**
     * @param event Instance of entering into a block
     * @param entity Instance of EntityRef
     * @param characterSounds Component that contains the character sound
     * Receives the event of entering into a water block, plays the proper sound, and save it into the character sounds.
     */
    @ReceiveEvent
    public void onEnterBlock(OnEnterBlockEvent event, EntityRef entity, CharacterSoundComponent characterSounds) {
        // only play this sound if the feet hit the water
        if (event.getCharacterRelativePosition().y == 0 && characterSounds.lastSoundTime + MIN_TIME < time.getGameTimeInMs()) {
            boolean oldBlockIsLiquid = event.getOldBlock().isLiquid();
            boolean newBlockIsLiquid = event.getNewBlock().isLiquid();
            StaticSound sound = null;
            handleOnEnterBlockSound(entity, characterSounds, oldBlockIsLiquid,
					newBlockIsLiquid, sound);
        }
    }

	/**
	 * @param entity Instance of EntityRef
	 * @param characterSounds Component that contains the character sound
	 * @param oldBlockIsLiquid Boolean that tells if the last block was a liquid one
	 * @param newBlockIsLiquid Boolean that tells if the new block is a liquid one
	 * @param sound Instance of StaticSound 
	 * Helper method of *onEnterBlock*. Check the block conditions, send the sound and save it into the character.
	 */
	private void handleOnEnterBlockSound(EntityRef entity,
			CharacterSoundComponent characterSounds, boolean oldBlockIsLiquid,
			boolean newBlockIsLiquid, StaticSound sound) {
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
