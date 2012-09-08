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
package org.terasology.componentSystem.action;

import javax.vecmath.Vector3f;

import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.events.HitEvent;
import org.terasology.math.Vector3fUtil;

/**
 * @author aherber 
 */
@RegisterComponentSystem
public class MeleeAttackAction implements EventHandlerSystem {

	@Override
	public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {ItemComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity){
    	EntityRef hitEntity =  event.getTarget();
	    Vector3f origin = event.getOrigin();
	    Vector3f target = event.getTargetLocation();
    	if(hitEntity != null && origin != null && target != null){
    		double distance = Vector3fUtil.calcdist(origin, target);
        	ItemComponent item = entity.getComponent(ItemComponent.class);
	        if(distance <= item.range){
	        	hitEntity.send(new HitEvent(event.getInstigator(),event.getDirection(),event.getHitPosition()));
	        }
	    }
    }
}
