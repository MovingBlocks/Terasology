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
package org.terasology.componentSystem.common;


import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.terasology.components.HealthComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.ProjectileComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.*;
import org.terasology.events.ActivateEvent;
import org.terasology.events.DamageEvent;
import org.terasology.events.HitEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.physics.CollideEvent;

/**
 * @author aherber 
 */
@RegisterComponentSystem
public class EffectSystem implements EventHandlerSystem {

    private EntityManager entityManager;
    
    @Override
    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class); 
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {HealthComponent.class})
    public void onDamage(DamageEvent event, EntityRef entity) {
    }    
    
    @ReceiveEvent(components = {ItemComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity){
    }
    
    //TODO Ranged Attack
    @ReceiveEvent(components = {ItemComponent.class,ProjectileComponent.class})
    public void onShot(ActivateEvent event, EntityRef entity){
    	
    }
    
    @ReceiveEvent(components =  {ProjectileComponent.class})
    public void onProjectileCollision(CollideEvent event, EntityRef entity) {
    }
    
    @ReceiveEvent(components = {HealthComponent.class})
    public void onHit(HitEvent event, EntityRef entity){
    	
    }

    
    
   @ReceiveEvent(components = {HealthComponent.class})
    public void onCollision(CollideEvent event, EntityRef entity) {
    }
    
  
    public void spawnEffectAt(Vector3f location, String effectName){
    	EntityRef effect =  entityManager.create(effectName);
  	    LocationComponent loc = new LocationComponent(location);      
  	    effect.addComponent(loc);
        effect.saveComponent(loc);
    }
    
    public void spawnEffectAt(Vector3f location,Quat4f rotation,float scale, String effectName){
    	EntityRef effect =  entityManager.create(effectName);
  	    LocationComponent loc = new LocationComponent(location);  
  	    loc.setLocalScale(scale);
  	    loc.setWorldRotation(rotation);
  	    effect.addComponent(loc);
        effect.saveComponent(loc);
    }
    
    public void spawnEffectAt(LocationComponent loc, String effectName){
    	EntityRef effect =  entityManager.create(effectName);
  	    effect.addComponent(loc);
        effect.saveComponent(loc);
    }
    
    public void spawnEffectAt(EntityRef entity, String effectName){
    	LocationComponent loc = entity.getComponent(LocationComponent.class);
    	EntityRef effect =  entityManager.create(effectName);
  	    effect.addComponent(loc);
    }
    
    public void attachEffect(EntityRef entity, String effectName){
    	LocationComponent loc = entity.getComponent(LocationComponent.class);
    	//scale Effect to size of Entity ?
    	EntityRef effect =  entityManager.create(effectName);
  	    effect.addComponent(loc);
    }
      
    @ReceiveEvent(components = {HealthComponent.class})
    public void onDeath(NoHealthEvent event, EntityRef entity) {
    	//TODO Remove all attached Effects from Entity
    }
}
