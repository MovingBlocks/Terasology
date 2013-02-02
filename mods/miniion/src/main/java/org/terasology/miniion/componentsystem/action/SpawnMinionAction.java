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

import javax.vecmath.Vector3f;

//import com.bulletphysics.collision.shapes.BoxShape;
//import com.bulletphysics.linearmath.QuaternionUtil;

import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.Prefab;
import org.terasology.entitySystem.PrefabManager;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.miniion.components.MinionComponent;
//import org.terasology.math.Side;
import org.terasology.miniion.components.actions.SpawnMinionActionComponent;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;
import org.terasology.physics.character.CharacterMovementComponent;

/**
 * @author Immortius
 */
@RegisterComponentSystem
public class SpawnMinionAction implements EventHandlerSystem {

	private EntityManager entityManager;

	@Override
	public void initialise() {
		entityManager = CoreRegistry.get(EntityManager.class);
	}

	@Override
	public void shutdown() {
	}

	@ReceiveEvent(components = SpawnMinionActionComponent.class)
	public void onActivate(ActivateEvent event, EntityRef entity) {
		SpawnMinionActionComponent spawnInfo = entity
				.getComponent(SpawnMinionActionComponent.class);
		if (spawnInfo.prefab != null) {
			Vector3f spawnPos = event.getTargetLocation();
			spawnPos.y += 2;
			Prefab prefab = CoreRegistry.get(PrefabManager.class).getPrefab(
					spawnInfo.prefab);
			if (prefab != null
					&& prefab.getComponent(LocationComponent.class) != null) {
				EntityRef minion = entityManager.create(prefab, spawnPos);
				if (minion != null) {
					CharacterMovementComponent movecomp = minion
							.getComponent(CharacterMovementComponent.class);
					movecomp.height = 0.31f;
					minion.saveComponent(movecomp);
					MinionComponent minioncomp = minion
							.getComponent(MinionComponent.class);
					String[] tempstring = MinionSystem.getName().split(":");
					if (tempstring.length == 2) {
						minioncomp.name = tempstring[0];
						minioncomp.flavortext = tempstring[1];
					}
				}
			}
		}
	}
}
