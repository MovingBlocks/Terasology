/*
 * Copyright 2012 Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
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
package org.terasology.hunger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.HealthComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterSystem;
import org.terasology.entitySystem.RegisterMode;
import org.terasology.events.DamageEvent;
import org.terasology.events.HealthChangedEvent;
import org.terasology.hunger.events.*;
import org.terasology.events.RespawnEvent;
import org.terasology.game.CoreRegistry;

/**
 * @author Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
 * 
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class HungerSystem implements UpdateSubscriberSystem {
	private static final Logger logger = LoggerFactory.getLogger(HungerSystem.class);
    private EntityManager entityManager;

    public void initialise() {
        entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void shutdown() {
    }

    public void update(float delta) {
        for (EntityRef entity : entityManager.iteratorEntities(HungerComponent.class, HealthComponent.class)) {
        	HungerComponent hunger = entity.getComponent(HungerComponent.class);
        	HealthComponent health = entity.getComponent(HealthComponent.class);
            if(hunger.currentContentment < 0 && hunger.timeSinceLastDamage >= hunger.waitBeetweenDamage ){
            	hunger.timeSinceLastDamage=0;
            	logger.info("Starvating " +entity.getId()+" for "+health.maxHealth/((11+hunger.currentContentment)*10) +"\n");
            	onStarvation(new StarvationEvent(health.maxHealth/((11+hunger.currentContentment)*10) ),entity);
            }else{
            	hunger.timeSinceLastDamage += delta;
            }
 
            if (hunger.deregenRate == 0)
                continue;
            
        	if (hunger.currentContentment <= -10) {
        		hunger.currentContentment=-10;
            	continue;
            }
        	
            hunger.timeSinceLastEat += delta;
            if (hunger.timeSinceLastEat >= hunger.waitBeforeDeregen) {
            	hunger.partialDeregen += delta * hunger.deregenRate;
                if (hunger.partialDeregen >= 1) {
                	hunger.currentContentment = Math.min(hunger.maxContentment, hunger.currentContentment - (int) hunger.partialDeregen);
                	hunger.partialDeregen %= 1f;
                    if (hunger.currentContentment == hunger.maxContentment) {
                        entity.send(new FullContentmentEvent(entity, hunger.maxContentment));
                    } else {
                    	if (hunger.currentContentment == 0) {
                    		entity.send(new NoContentmentEvent(entity, hunger.maxContentment));
                    	} else {
                    		entity.send(new ContentmentChangedEvent(entity, hunger.currentContentment, hunger.maxContentment));
                    	}
                    }
                }
            }
            entity.saveComponent(hunger);
        }
    }

    @ReceiveEvent(components = {HealthComponent.class})
    public void onStarvation(StarvationEvent event, EntityRef entity) {
        HealthComponent health = entity.getComponent(HealthComponent.class);
        applyDamage(entity, health, event.getAmount(), event.getInstigator());
    }
    
    @ReceiveEvent(components = {HungerComponent.class})
    public void onEat(EatEvent event, EntityRef entity) {
    	HungerComponent hunger = entity.getComponent(HungerComponent.class);
    	hunger.timeSinceLastEat=0;
    	hunger.currentContentment+=event.getAmount();
    	if(hunger.currentContentment<0){
    		hunger.currentContentment=0;
    		entity.send(new NoContentmentEvent(entity, hunger.maxContentment));
    		}else{
    			if(hunger.currentContentment >= hunger.maxContentment){
    				//over eating is unhealthy
    				//TODO add fancy overEating sound here
    				entity.send(new StarvationEvent((hunger.currentContentment-hunger.maxContentment)/10));
    				entity.send(new FullContentmentEvent(entity, hunger.maxContentment));
    			}else entity.send(new ContentmentChangedEvent(entity, hunger.currentContentment, hunger.maxContentment));
    		}
    	entity.saveComponent(hunger);
    }
    
    @ReceiveEvent(components = {HungerComponent.class})
    public void onRespawn(RespawnEvent event, EntityRef entity) {
        HungerComponent hungerComponent = entity.getComponent(HungerComponent.class);
        if (hungerComponent != null) {
        	hungerComponent.currentContentment = hungerComponent.maxContentment;
            entity.send(new HealthChangedEvent(entity, hungerComponent.currentContentment, hungerComponent.maxContentment));
            entity.saveComponent(hungerComponent);
        }
    }
    
    private void applyDamage(EntityRef entity, HealthComponent health, int damageAmount, EntityRef instigator) {
        entity.send(new DamageEvent(damageAmount, instigator));
    }
}
