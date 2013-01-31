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
package org.terasology.miniion.componentsystem.action;

import java.util.Set;

import org.terasology.entitySystem.*;
import org.terasology.events.ActivateEvent;
import org.terasology.math.Vector3i;
import org.terasology.miniion.components.ZoneSelectionComponent;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;
import org.terasology.rendering.world.BlockGrid;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

@RegisterComponentSystem
public class SetSelectionAction implements EventHandlerSystem {

	@In
	private WorldProvider worldProvider;

	@Override
	public void initialise() {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@ReceiveEvent(components = ZoneSelectionComponent.class)
	public void onActivate(ActivateEvent event, EntityRef entity) {
		if(MinionSystem.getNewZone() == null){
			MinionSystem.startNewSelection(new Vector3i(event.getTargetLocation()));
		}else if(MinionSystem.getNewZone().getEndPosition() == null){
			MinionSystem.endNewSelection(new Vector3i(event.getTargetLocation()));
		}else{
			MinionSystem.resetNewSelection();
		}		
	}
}
