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

import org.terasology.components.HitDetectionComponent;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.entitySystem.event.RemovedComponentEvent;
import org.terasology.events.HitEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.physics.CollideEvent;
import org.terasology.physics.ExitCollisionEvent;

/**
 * @author aherber 
 */
@RegisterComponentSystem
public class HitDetectionSystem implements EventHandlerSystem {

	@Override
	public void initialise() {
	}

	@Override
	public void shutdown() {
	}

	//There is a race between these two methods therefore we need active waiting
	@ReceiveEvent(components = HitDetectionComponent.class)
	public void onExitCollsion(ExitCollisionEvent event, EntityRef entity) {
		HitDetectionComponent hitDetectionComponent = entity.getComponent(HitDetectionComponent.class);
		hitDetectionComponent.timeSinceLastHit = 0;
		while(!hitDetectionComponent.hit){} // i know this is ugly because its active waiting
		hitDetectionComponent.hit = false;
	}
	
	@ReceiveEvent(components = HitDetectionComponent.class)
	public void onCollision(CollideEvent event, EntityRef entity) {
		HitDetectionComponent hitDetectionComponent = entity.getComponent(HitDetectionComponent.class);
		hitDetectionComponent.timer.tick();
	    float delta = hitDetectionComponent.timer.getDelta();
	    hitDetectionComponent.timeSinceLastHit += delta;
		switch (hitDetectionComponent.hitDetection) {
			case ON_CONTACT:
		 		if(!hitDetectionComponent.hit){
		 			entity.send(new HitEvent(event.getOtherEntity(), event.getHitPoint(), event.getHitNormal()));
		 			hitDetectionComponent.hit = true;
		 		}
		 		break;
		 	case ON_COLLISION:
		 		entity.send(new HitEvent(event.getOtherEntity(), event.getHitPoint(), event.getHitNormal()));
		 		break;
		 	case ON_PERIOD:
		 		if(hitDetectionComponent.timeSinceLastHit >= hitDetectionComponent.detectionPeriod){
		 			entity.send(new HitEvent(event.getOtherEntity(), event.getHitPoint(), event.getHitNormal()));
					hitDetectionComponent.timeSinceLastHit -= hitDetectionComponent.detectionPeriod;
		 		}
		 		break;
		 	default:
		 		break;
		 }
	}
	
	@ReceiveEvent(components = {HitDetectionComponent.class})
	public void onHit(HitEvent event, EntityRef entity) {
	}
	
	@ReceiveEvent(components = {HitDetectionComponent.class})
    public void addHitDetection(AddComponentEvent event, EntityRef entity) {
    }
	
	@ReceiveEvent(components = {HitDetectionComponent.class})
    public void removeHitDetection(RemovedComponentEvent event, EntityRef entity) {
    }

    @ReceiveEvent(components = {HitDetectionComponent.class})
	public void onDeath(NoHealthEvent event, EntityRef entity) {
	}
}
