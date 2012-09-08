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


import javax.vecmath.Vector3f;


import org.terasology.components.HealthComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.ProjectileComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entityFactory.DeadEntityFactory;
import org.terasology.entitySystem.*;
import org.terasology.events.DamageEvent;
import org.terasology.events.HitEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.mods.miniions.components.SimpleMinionAIComponent;
import org.terasology.physics.ImpulseEvent;
import org.terasology.physics.RigidBodyComponent;
import org.terasology.physics.StandardCollisionGroup;
import org.terasology.physics.TriggerComponent;
import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.physics.shapes.BoxShapeComponent;
import org.terasology.physics.shapes.CapsuleShapeComponent;
import org.terasology.physics.shapes.CylinderShapeComponent;
import org.terasology.physics.shapes.HullShapeComponent;
import org.terasology.physics.shapes.SphereShapeComponent;

/**
 * @author aherber 
 */
@RegisterComponentSystem
public class CombatSystem implements EventHandlerSystem {

    private DeadEntityFactory deadEntityFactory;
    private final float pushBackVelocity = 0.5f;
    
    @Override  
    public void initialise() {
        deadEntityFactory = new DeadEntityFactory(); 
    }

    @Override
    public void shutdown() {
    }
    
    @ReceiveEvent(components =  ProjectileComponent.class)
    public void onProjectileHit(HitEvent event, EntityRef entity) {
    	System.out.println("Projectile Hit");
    	if(entity != null){
    		pierce(event, entity);
        }
    }
   
    private void pierce(HitEvent event, EntityRef entity){
    	ProjectileComponent projectile = entity.getComponent(ProjectileComponent.class);
    	RigidBodyComponent rigidBody = entity.getComponent(RigidBodyComponent.class);
		if(projectile.piercing){ 
	    	//rigidBody.collisionGroup = StandardCollisionGroup.KINEMATIC;
	    	//rigidBody.gravity = 0;
	    	//rigidBody.velocity = 0;
	    	System.out.println("pierce");
			//Remove Collsiondetection from Entity
			removeCollisionShape(entity);
			entity.removeComponent(RigidBodyComponent.class);
			entity.removeComponent(TriggerComponent.class);
			//calculate new Entity Location after piercing
			Vector3f position = event.getHitPosition();
			Vector3f hitNormal = event.getHitNormal();
			float piercingFactor = 0.001f;  //TODO This should be dynamic or based on a given value in the ProjectileComponent 
			position.x += hitNormal.x * piercingFactor;
			position.y += hitNormal.y * piercingFactor;
			position.z += hitNormal.z * piercingFactor;
			//update Projectileposition
			LocationComponent location = entity.getComponent(LocationComponent.class);
			location.setLocalPosition(position);
			entity.saveComponent(location);
	    	entity.saveComponent(rigidBody);
		}
    }
           
    @ReceiveEvent(components =  HealthComponent.class)
    public void onHit(HitEvent event, EntityRef entity) {
    	if(entity != null){
    		ItemComponent item = event.getInstigator().getComponent(ItemComponent.class);
    		entity.send(new DamageEvent(item.baseDamage, event.getInstigator()));
        }
    } 
    
    @ReceiveEvent(components = {SimpleMinionAIComponent.class})
    public void onDeath(NoHealthEvent event, EntityRef entity) {
       CharacterMovementComponent characterMovementComponent = entity.getComponent(CharacterMovementComponent.class);
       Vector3f drive = characterMovementComponent.getDrive();// Get last Movement of dead entity
       drive.z+= 2;
       EntityRef deadEntity = deadEntityFactory.newInstance(entity);//Create Dead Body (temporarily)
	   deadEntity.send(new ImpulseEvent(drive));
	   entity.destroy();
    }
  
    //Pushback für 
    public void pushBack(HitEvent event, EntityRef entity){
        CharacterMovementComponent comp = entity.getComponent(CharacterMovementComponent.class);
        comp.faceMovementDirection = false;//Dont Change facing on hit
        Vector3f drive = event.getHitNormal();
        comp.getDrive().add(drive);
        drive.scale(0.125f);//TODO Should be scaled to the force of the impact;
        comp.getVelocity().add(drive);
        comp.getVelocity().y += pushBackVelocity;
        entity.saveComponent(comp);
    }
    
//    public void freeze(RigidBodyComponent rigidBody ){
//        rigidBody.velocity = 0;
//	    rigidBody.gravity = 0;
//   	    rigidBody.collisionGroup = StandardCollisionGroup.KINEMATIC;
//   	    rigidBody.friction = 1;
//    }
//    
//    public void slowDown(RigidBodyComponent rigidBody , float factor){
//        rigidBody.velocity *= factor;
//    }
//    
//    public void bleed(HitEvent event){ 
//        effects.spawnEffectAt(event.getHitPosition(), "combat:blood");
//    }
    
    private void removeCollisionShape(EntityRef entity){
   	    entity.removeComponent(BoxShapeComponent.class);
        entity.removeComponent(SphereShapeComponent.class);
        entity.removeComponent(CapsuleShapeComponent.class);
        entity.removeComponent(CylinderShapeComponent.class);
        entity.removeComponent(HullShapeComponent.class);
   }
}
