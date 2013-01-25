/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.componentSystem.characters;

import javax.vecmath.Vector3d;

import org.terasology.audio.Sound;
import org.terasology.components.CharacterSoundComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.In;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.DamageEvent;
import org.terasology.events.FootstepEvent;
import org.terasology.events.FromLiquidEvent;
import org.terasology.events.HorizontalCollisionEvent;
import org.terasology.events.IntoLiquidEvent;
import org.terasology.events.JumpEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.events.RespawnEvent;
import org.terasology.events.SwimEvent;
import org.terasology.events.VerticalCollisionEvent;
import org.terasology.logic.manager.AudioManager;
import org.terasology.logic.manager.SoundManager;
import org.terasology.math.TeraMath;
import org.terasology.utilities.FastRandom;
import org.terasology.world.WorldProvider;

/**
 * @author Immortius <immortius@gmail.com>
 * @author Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
 */
@RegisterComponentSystem
public class CharacterSoundSystem implements EventHandlerSystem {

    private FastRandom random = new FastRandom();
    final private long MINTIME= 100;

    @In
    private WorldProvider worldProvider;

    //TODO mintime times need fine tuning
    
    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {CharacterSoundComponent.class})
    public void onFootstep(FootstepEvent event, EntityRef entity) {
        if (random == null) return;
        
        //TODO different sounds before player sound on each surface
        LocationComponent location = entity.getComponent(LocationComponent.class);
        if (location != null) {
            CharacterSoundComponent characterSounds = entity.getComponent(CharacterSoundComponent.class);
            if(worldProvider.getTime()-characterSounds.lastSound < MINTIME) return;
            characterSounds.lastSound=worldProvider.getTime();
            if (characterSounds.footstepSounds.size() > 0) {
                Sound sound = characterSounds.footstepSounds.get(random.randomIntAbs(characterSounds.footstepSounds.size()));
                AudioManager.play(sound, new Vector3d(location.getWorldPosition()), characterSounds.footstepVolume, SoundManager.PRIORITY_NORMAL);
            }
        }
    }

    @ReceiveEvent(components = {CharacterSoundComponent.class})
    public void onJump(JumpEvent event, EntityRef entity) {
        if (random == null) return;

        LocationComponent location = entity.getComponent(LocationComponent.class);
        if (location != null) {
            CharacterSoundComponent characterSounds = entity.getComponent(CharacterSoundComponent.class);
            if(worldProvider.getTime()-characterSounds.lastSound < MINTIME) return;
            characterSounds.lastSound=worldProvider.getTime();
            if(characterSounds.jumpSounds.size() > 0){
            	Sound sound = characterSounds.jumpSounds.get(random.randomIntAbs(characterSounds.jumpSounds.size()));
                AudioManager.play(sound, new Vector3d(location.getWorldPosition()), characterSounds.landingVolume, SoundManager.PRIORITY_NORMAL);
            }else if (characterSounds.footstepSounds.size() > 0) {
                Sound sound = characterSounds.footstepSounds.get(random.randomIntAbs(characterSounds.footstepSounds.size()));
                AudioManager.play(sound, new Vector3d(location.getWorldPosition()), characterSounds.landingVolume, SoundManager.PRIORITY_NORMAL);
            }
        }
    }
    
    //TODO upward vertical crash missing
    @ReceiveEvent(components = {CharacterSoundComponent.class})
    public void onLanding(VerticalCollisionEvent event, EntityRef entity) {
        //System.out.print("vertical Velocity of "+entity.getId() +":"+event.getVelocity()+"\n");
    	if (random == null || event.getVelocity().y > 0f) return;

        //-11 is velocity of full jump
        CharacterSoundComponent characterSounds = entity.getComponent(CharacterSoundComponent.class);
        if(worldProvider.getTime()-characterSounds.lastSound < MINTIME) return;
        characterSounds.lastSound=worldProvider.getTime();
        if(characterSounds.landingSounds.size() >0 && event.getVelocity().y < -14f){
        	Sound sound = characterSounds.landingSounds.get(random.randomIntAbs(characterSounds.landingSounds.size()));
            AudioManager.play(sound, event.getLocation(), 1.0f, SoundManager.PRIORITY_NORMAL);
        }else if (characterSounds.footstepSounds.size() > 0) {
            Sound sound = characterSounds.footstepSounds.get(random.randomIntAbs(characterSounds.footstepSounds.size()));
            AudioManager.play(sound, event.getLocation(), characterSounds.landingVolume, SoundManager.PRIORITY_NORMAL);
        }
    }
    
    @ReceiveEvent(components = {CharacterSoundComponent.class})
    public void onCrash(HorizontalCollisionEvent event, EntityRef entity) {
    	//System.out.print("horisontal Velocity of "+entity.getId() +":"+event.getVelocity()+"\n");
    	float velocity = (TeraMath.fastAbs(event.getVelocity().x)+TeraMath.fastAbs(event.getVelocity().z))/2;
    	//System.out.print("calculated Velocity of "+entity.getId() +":"+velocity+"\n");
        if (random == null || velocity < 4.5f) return;

        //-5 strongest normal bump
        CharacterSoundComponent characterSounds = entity.getComponent(CharacterSoundComponent.class);
        if(worldProvider.getTime()-characterSounds.lastSound < 500) return;
        characterSounds.lastSound=worldProvider.getTime();
        if(characterSounds.landingSounds.size() >0 ){
        	Sound sound = characterSounds.landingSounds.get(random.randomIntAbs(characterSounds.landingSounds.size()));
            AudioManager.play(sound, event.getLocation(), characterSounds.landingVolume, SoundManager.PRIORITY_NORMAL);
        }
    }
    
    @ReceiveEvent(components = {CharacterSoundComponent.class})
    public void onDamage(DamageEvent event, EntityRef entity) {
    	if (random == null) return;
    	
        LocationComponent location = entity.getComponent(LocationComponent.class);
        if (location != null) {
            CharacterSoundComponent characterSounds = entity.getComponent(CharacterSoundComponent.class);
            if(worldProvider.getTime()-characterSounds.lastSound < 10) return;
            characterSounds.lastSound=worldProvider.getTime();
            if (characterSounds.damageSounds.size() > 0 ) {
                Sound sound = characterSounds.damageSounds.get(random.randomIntAbs(characterSounds.damageSounds.size()));
                AudioManager.play(sound, new Vector3d(location.getWorldPosition()), characterSounds.damageVolume, SoundManager.PRIORITY_LOW);
            }
        }
    }
    
    @ReceiveEvent(components = {CharacterSoundComponent.class})
    public void onDeath(NoHealthEvent event, EntityRef entity) {
    	if (random == null) return;
    	
    	LocationComponent location = entity.getComponent(LocationComponent.class);
        if (location != null) {
            CharacterSoundComponent characterSounds = entity.getComponent(CharacterSoundComponent.class);
            if (characterSounds.deathSounds.size() > 0) {
                Sound sound = characterSounds.deathSounds.get(random.randomIntAbs(characterSounds.deathSounds.size()));
                AudioManager.play(sound, new Vector3d(location.getWorldPosition()), characterSounds.deathVolume, SoundManager.PRIORITY_HIGH);
            }
        }
    }
    
    @ReceiveEvent(components = {CharacterSoundComponent.class})
    public void onRespawn(RespawnEvent event, EntityRef entity) {
    	if (random == null) return;
    	
    	LocationComponent location = entity.getComponent(LocationComponent.class);
        if (location != null) {
            CharacterSoundComponent characterSounds = entity.getComponent(CharacterSoundComponent.class);
            if (characterSounds.respawnSounds.size() > 0) {
                Sound sound = characterSounds.respawnSounds.get(random.randomIntAbs(characterSounds.respawnSounds.size()));
                AudioManager.play(sound, new Vector3d(location.getWorldPosition()), characterSounds.respawnVolume, SoundManager.PRIORITY_LOW);
            }
        }
    }
    
    
    @ReceiveEvent(components = {CharacterSoundComponent.class})
    public void onSwimming(SwimEvent event, EntityRef entity) {
    	//System.out.print("swinmming\n");
    	
        CharacterSoundComponent characterSounds = entity.getComponent(CharacterSoundComponent.class);
        if(worldProvider.getTime()-characterSounds.lastSound < 1000) return;
        characterSounds.lastSound=worldProvider.getTime();
        if (event.getLiquid().getDisplayName().compareTo("Water")==0) {
            AudioManager.play("engine:Slime4", new Vector3d(event.getPosition()), characterSounds.footstepVolume, SoundManager.PRIORITY_NORMAL);
        }else if (event.getLiquid().getDisplayName().compareTo("Lava")==0) {
            AudioManager.play("engine:FootGrass2", new Vector3d(event.getPosition()), characterSounds.footstepVolume, SoundManager.PRIORITY_NORMAL);
        }

    }
    
    @ReceiveEvent(components = {CharacterSoundComponent.class})
    public void onIntoLiquid(IntoLiquidEvent event, EntityRef entity) {
    	//System.out.print("in liquid\n");
        CharacterSoundComponent characterSounds = entity.getComponent(CharacterSoundComponent.class);
        if(worldProvider.getTime()-characterSounds.lastSound < MINTIME) return;
        characterSounds.lastSound=worldProvider.getTime();
        if (event.getLiquid().getDisplayName().compareTo("Water")==0) {
            AudioManager.play("engine:Slime1", new Vector3d(event.getPosition()), characterSounds.footstepVolume, SoundManager.PRIORITY_NORMAL);
        }else if (event.getLiquid().getDisplayName().compareTo("Lava")==0) {
            AudioManager.play("engine:FootGrass3", new Vector3d(event.getPosition()), characterSounds.footstepVolume, SoundManager.PRIORITY_NORMAL);
        }
    }
    
    @ReceiveEvent(components = {CharacterSoundComponent.class})
    public void onfromliquid(FromLiquidEvent event, EntityRef entity) {
    	//System.out.print("out liquid\n");
        CharacterSoundComponent characterSounds = entity.getComponent(CharacterSoundComponent.class);
        if(worldProvider.getTime()-characterSounds.lastSound < MINTIME) return;
        characterSounds.lastSound=worldProvider.getTime();
        if (event.getLiquid().getDisplayName().compareTo("Water")==0) {
            AudioManager.play("engine:Slime3", new Vector3d(event.getPosition()), characterSounds.footstepVolume, SoundManager.PRIORITY_NORMAL);
        }else if (event.getLiquid().getDisplayName().compareTo("Lava")==0) {
            AudioManager.play("engine:FootGrass4", new Vector3d(event.getPosition()), characterSounds.footstepVolume, SoundManager.PRIORITY_NORMAL);
        }
    }

}
