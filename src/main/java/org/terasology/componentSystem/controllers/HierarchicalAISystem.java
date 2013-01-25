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
package org.terasology.componentSystem.controllers;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.HierarchicalAIComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.DamageEvent;
import org.terasology.events.HorizontalCollisionEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.LocalPlayer;
import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.utilities.FastRandom;
import org.terasology.world.WorldProvider;
import org.terasology.math.TeraMath;

/**
 * Hierarchical AI, idea from robotics
 * 
 * @author Esa-Petri Tirkkonen <esereja@yahoo.co.uk>
 */
@RegisterComponentSystem(authorativeOnly = true)
public class HierarchicalAISystem implements EventHandlerSystem,
		UpdateSubscriberSystem {

	private WorldProvider worldProvider;
	private EntityManager entityManager;
	private FastRandom random = new FastRandom();
	private Timer timer;
	private boolean idling;

	@Override
	public void initialise() {
		entityManager = CoreRegistry.get(EntityManager.class);
		timer = CoreRegistry.get(Timer.class);
		worldProvider = CoreRegistry.get(WorldProvider.class);
		idling = false;
	}

	@Override
	public void shutdown() {
	}

	// TODO add way to recognize if attacked

	@Override
	public void update(float delta) {
		for (EntityRef entity : entityManager.iteratorEntities(
				HierarchicalAIComponent.class, CharacterMovementComponent.class,
				LocationComponent.class)) {
			LocationComponent location = entity
					.getComponent(LocationComponent.class);
			Vector3f worldPos = location.getWorldPosition();

			// Skip this AI if not in a loaded chunk
			if (!worldProvider.isBlockActive(worldPos)) {
				continue;
			}

			// goto Hierarchical system
			loop(entity, location, worldPos);
		}
	}

	/**
	 * main loop of hierarchical system
	 * 
	 * @param entity
	 * @param location
	 * @param worldPos
	 */
	private void loop(EntityRef entity, LocationComponent location,
			Vector3f worldPos) {
		HierarchicalAIComponent ai = entity
				.getComponent(HierarchicalAIComponent.class);
		CharacterMovementComponent moveComp = entity
				.getComponent(CharacterMovementComponent.class);
		long tempTime = CoreRegistry.get(Timer.class).getTimeInMs();
		//TODO remove next
		long lastAttack=0;

		// skip update if set to skip them
		if (tempTime - ai.lastProgressedUpdateAt < ai.updateFrequency) {
			ai.lastProgressedUpdateAt = CoreRegistry.get(Timer.class)
					.getTimeInMs();
			return;
		}

		long directionChangeTime = ai.moveUpdateTime,
				moveChangeTime = ai.moveUpdateTime,
				idleChangeTime = ai.idlingUpdateTime,
				dangerChangeTime=ai.dangerUpdateTime;

		// get movement
		Vector3f drive = moveComp.getDrive();

		// stop movement
		drive.set(0, 0, 0);

		// find player position
		// TODO: shouldn't use local player, need some way to find nearest
		// player
		LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
		if (localPlayer != null) {
			Vector3f dist = new Vector3f(worldPos);
			dist.sub(localPlayer.getPosition());
			double distanceToPlayer = dist.lengthSquared();

			ai.inDanger = false;
			if (ai.dieIfPlayerFar)
				if (distanceToPlayer > ai.dieDistance)
					entity.destroy();

			//----------------danger behavior----------
			
			// if our AI is aggressive or hunter go and hunt player else run away
			// if wild
			if (ai.aggressive)
				// TODO fix this to proper attacking
				if (distanceToPlayer <= ai.attackDistance) {
					if (  tempTime-lastAttack > ai.damageFrequency){
						localPlayer.getEntity().send(
								new DamageEvent(ai.damage, entity));
						lastAttack=	CoreRegistry.get(Timer.class).getTimeInMs();
					}
				}
			
			//update 
			if(tempTime-ai.lastChangeOfDangerAt >dangerChangeTime){
				dangerChangeTime = (long) (TeraMath.fastAbs(ai.dangerUpdateTime
						* random.randomDouble() * ai.hectic));
				if (ai.hunter)
					if (distanceToPlayer > ai.playerdistance
							&& distanceToPlayer < ai.playerSense) {
						// Head to player
						Vector3f tempTarget = localPlayer.getPosition();
						if (ai.forgiving != 0)
							ai.movementTarget.set(new Vector3f(
									(tempTarget.x + random.randomFloat()
											* ai.forgiving),
									(tempTarget.y + random.randomFloat()
											* ai.forgiving),
									(tempTarget.z + random.randomFloat()
											* ai.forgiving)));
						else
							ai.movementTarget.set(tempTarget);
						ai.inDanger = true;
						entity.saveComponent(ai);

						// System.out.print("\nhunting palyer\n");
					}
				// run opposite direction
				if (ai.wild)
					if (distanceToPlayer > ai.panicDistance
							&& distanceToPlayer < ai.runDistance) {
						Vector3f tempTarget = localPlayer.getPosition();
						if (ai.forgiving != 0)
							ai.movementTarget.set(new Vector3f(
									(tempTarget.x * -1 + random.randomFloat()
											* ai.forgiving),
									(tempTarget.y * -1 + random.randomFloat()
											* ai.forgiving),
									(tempTarget.z * -1 + random.randomFloat()
											* ai.forgiving)));
						else
							ai.movementTarget
									.set(new Vector3f(tempTarget.x * -1,
											tempTarget.y * -1, tempTarget.z
													* -1));
						entity.saveComponent(ai);
						ai.inDanger = true;
					}
				ai.lastChangeOfDangerAt = CoreRegistry.get(Timer.class)
						.getTimeInMs();
			}
		}

		if (!ai.inDanger) {

			//----------------eat----------
			// if anything edible is in front
			if (foodInFront()) {
				return;
			}

			//----------------idle----------
			// Idling part
			// what AI does when nothing better to do
			if (idling) {
				// time to stop idling
				if (tempTime - ai.lastChangeOfidlingtAt > idleChangeTime) {
					idleChangeTime = (long) (TeraMath
							.fastAbs(ai.idlingUpdateTime
									* random.randomDouble() * ai.hectic));
					idling = false;
					// mark idling state changed
					ai.lastChangeOfidlingtAt = CoreRegistry.get(Timer.class)
							.getTimeInMs();
				}
				entity.saveComponent(location);
				ai.lastProgressedUpdateAt = CoreRegistry.get(Timer.class)
						.getTimeInMs();
				return;

			}

			// check if it is time to idle again
			if (tempTime - ai.lastChangeOfMovementAt > moveChangeTime) {
				// update time
				moveChangeTime = (long) (TeraMath.fastAbs(ai.moveUpdateTime
						* random.randomDouble() * ai.hectic));
				idling = true;
				entity.saveComponent(location);

				// mark start idling
				ai.lastChangeOfMovementAt = CoreRegistry.get(Timer.class)
						.getTimeInMs();
				ai.lastProgressedUpdateAt = CoreRegistry.get(Timer.class)
						.getTimeInMs();
				return;
			}

			// Random walk
			// check if time to change direction
			if (tempTime - ai.lastChangeOfDirectionAt > directionChangeTime) {
				directionChangeTime = (long) (TeraMath
						.fastAbs(ai.moveUpdateTime * random.randomDouble()
								* ai.straightLined));
				// if ai flies
				if (ai.flying) {
					float targetY = 0;
					do {
						targetY = worldPos.y + random.randomFloat() * 100;
					} while (targetY > ai.maxAltitude);
					ai.movementTarget.set(worldPos.x + random.randomFloat()
							* 500, targetY, worldPos.z + random.randomFloat()
							* 500);
				} else
					ai.movementTarget.set(worldPos.x + random.randomFloat()
							* 500, worldPos.y,
							worldPos.z + random.randomFloat() * 500);
				ai.lastChangeOfDirectionAt = timer.getTimeInMs();
				entity.saveComponent(ai);
				// System.out.print("direction changed\n");

			}
		}

		Vector3f targetDirection = new Vector3f();
		targetDirection.sub(ai.movementTarget, worldPos);
		targetDirection.normalize();
		moveComp.setDrive(targetDirection);

		float yaw = (float) Math.atan2(targetDirection.x, targetDirection.z);
		AxisAngle4f axisAngle = new AxisAngle4f(0, 1, 0, yaw);
		location.getLocalRotation().set(axisAngle);
		entity.saveComponent(moveComp);
		entity.saveComponent(location);
		// System.out.print("\Destination set: " + targetDirection.x + ":" +targetDirection.z + "\n");
		// System.out.print("\nI am: " + worldPos.x + ":" + worldPos.z + "\n");

		ai.lastProgressedUpdateAt = CoreRegistry.get(Timer.class).getTimeInMs();
	}

	private boolean foodInFront() {
		return false;
		// return true;
	}

	//TODO change eating thingy to use this
	@ReceiveEvent(components = { HierarchicalAIComponent.class })
	public void onBump(HorizontalCollisionEvent event, EntityRef entity) {
		CharacterMovementComponent moveComp = entity
				.getComponent(CharacterMovementComponent.class);
		if (moveComp != null && moveComp.isGrounded) {
			moveComp.jump = true;
			entity.saveComponent(moveComp);
		}
	}
	
	//Destroy AI on death
    @ReceiveEvent(components = {HierarchicalAIComponent.class})
    public void onDeath(NoHealthEvent event, EntityRef entity) {
    	entity.destroy();
    }
}
