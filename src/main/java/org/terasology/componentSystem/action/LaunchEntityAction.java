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

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import org.terasology.components.LaunchEntityComponent;
import org.terasology.components.LocalPlayerComponent;
import org.terasology.components.ProjectileComponent;
import org.terasology.components.rendering.MeshComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.math.AABB;
import org.terasology.math.TeraMath;
import org.terasology.physics.ImpulseEvent;
import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.physics.shapes.BoxShapeComponent;
import org.terasology.physics.shapes.CapsuleShapeComponent;
import org.terasology.physics.shapes.CylinderShapeComponent;
import org.terasology.physics.shapes.HullShapeComponent;
import org.terasology.physics.shapes.SphereShapeComponent;

import com.bulletphysics.linearmath.QuaternionUtil;

/**
 * @author aherber 
 */
@RegisterComponentSystem
public class LaunchEntityAction implements EventHandlerSystem {

    private EntityManager entityManager;

    public void initialise() {
    	entityManager = CoreRegistry.get(EntityManager.class);
    }

    @Override
    public void shutdown() {
    }

    @ReceiveEvent(components = {LaunchEntityComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity){
    	EntityRef instigator = event.getInstigator();
    	Vector3f instigatorLocation = event.getInstigatorLocation();
    	LaunchEntityComponent launchEntityComponent = entity.getComponent(LaunchEntityComponent.class);
    	EntityRef projectile = entityManager.create(launchEntityComponent.entity);
    	LocationComponent projectileLocation;
    	//TODO define relative spawn position as a vector in the LauncherComponent
    	Vector3f origin = event.getOrigin();
    	Vector3f direction = event.getDirection();
    	
    	projectileLocation = projectile.getComponent(LocationComponent.class);
    	if(projectileLocation == null){
    		projectileLocation  = new LocationComponent(origin);
    	}

    	//Spawn Entity in Front of Player(-Camera)	
    	//float distanceToEntity = calcSpawnDistance(event, entity) + launchEntityComponent.spawnDistance + 0.2f;
    	float distanceToEntity = 0;
        Vector3f spawnPosition = new Vector3f(instigatorLocation);
        spawnPosition.x += direction.x * distanceToEntity;
        spawnPosition.y += direction.y * distanceToEntity;
        spawnPosition.z += direction.z * distanceToEntity;                
        projectileLocation.setLocalPosition(spawnPosition);
    	
    	// Set rotation based on mouselook 
    	if(launchEntityComponent.useMouseLookForRotation){
    	 	Quat4f lookRotation = new Quat4f();
    		//TODO this should use the CameraComponent when ready
        	LocalPlayerComponent localPlayerComponent = instigator.getComponent(LocalPlayerComponent.class);
    		QuaternionUtil.setEuler(lookRotation, TeraMath.DEG_TO_RAD * localPlayerComponent.viewYaw, TeraMath.DEG_TO_RAD * localPlayerComponent.viewPitch, 0);
    		QuaternionUtil.quatRotate(lookRotation, new Vector3f(0,0,1), direction);
    		projectileLocation.setLocalRotation(lookRotation);
    	}
    	
        ProjectileComponent projectileComponent = projectile.getComponent(ProjectileComponent.class);
    	projectileComponent.owner = instigator;
    	/*******************************************
         **** THIS IS FOR TESTING PURPOSES ONLY **** 
    	 *******************************************/ 
    	projectileLocation.setLocalScale(0.015f); //TODO use a better model that is correctly scaled
    	/*******************************************/
    	projectile.addComponent(projectileLocation);
    	projectile.saveComponent(projectileComponent);
    	//Fire Entity
    	direction.scale(launchEntityComponent.maxSpeed);
    	projectile.send(new ImpulseEvent(direction));
    }
    
    private float calcSpawnDistance(ActivateEvent event, EntityRef entity) {
    	float distance = 0;
    	EntityRef instigator = event.getInstigator();
    	float collisionVolumeDistanceZ = getCollisionVolumeDistanceZ(instigator);
    	float meshVolumeDisanceZ = getMeshVolumeDistanceZ(instigator);
        distance += collisionVolumeDistanceZ > meshVolumeDisanceZ ? collisionVolumeDistanceZ : meshVolumeDisanceZ;
    	collisionVolumeDistanceZ = getCollisionVolumeDistanceZ(entity);
    	meshVolumeDisanceZ = getMeshVolumeDistanceZ(entity);
        distance += collisionVolumeDistanceZ > meshVolumeDisanceZ ? collisionVolumeDistanceZ : meshVolumeDisanceZ;
        return distance;
    }
    
    private float getCollisionVolumeDistanceZ(EntityRef entity){
    	float distance = 0;
        BoxShapeComponent box = entity.getComponent(BoxShapeComponent.class);
        if (box != null) {
            Vector3f halfExtents = new Vector3f(box.extents);
            halfExtents.scale(0.5f);
            return halfExtents.z;
        }
        SphereShapeComponent sphere = entity.getComponent(SphereShapeComponent.class);
        if (sphere != null) 
        	return sphere.radius;
        CapsuleShapeComponent capsule = entity.getComponent(CapsuleShapeComponent.class);
        if (capsule != null) 
            return capsule.radius;
        CylinderShapeComponent cylinder = entity.getComponent(CylinderShapeComponent.class);
        if (cylinder != null) 
        	return cylinder.radius;
        HullShapeComponent hull = entity.getComponent(HullShapeComponent.class);
        if (hull != null) {
            AABB aabb = hull.sourceMesh.getAABB();
            Vector3f halfExtents = new Vector3f(aabb.getExtents());
            halfExtents.scale(0.5f);
            return halfExtents.z;
        }
        CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
        if (characterMovementComponent != null) 
            return characterMovementComponent.radius;
        return distance;    
    }
    
    private float getMeshVolumeDistanceZ(EntityRef entity){
    	float distance = 0;
    	MeshComponent meshComponent = entity.getComponent(MeshComponent.class);
    	if(meshComponent != null){
    		Vector3f halfExtents = new Vector3f(meshComponent.mesh.getAABB().getExtents());
    		halfExtents.scale(0.5f);
    		distance = halfExtents.z;
    	}
        return distance;    
    }
}
