package org.terasology.componentSystem.action;


import javax.vecmath.Vector3f;

import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.events.DamageEvent;
import org.terasology.events.HitEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.physics.BulletPhysics;
import org.terasology.physics.HitResult;
import org.terasology.physics.StandardCollisionGroup;


/**
 * @author Andre Herber <andre.herber@yahoo.de>
 * 
 */
@RegisterComponentSystem
public class MeleeAttackAction implements EventHandlerSystem {

	private BulletPhysics physicsRenderer;
 
    public void initialise() {
    	  physicsRenderer = CoreRegistry.get(BulletPhysics.class);
   }
    
    @Override
    public void shutdown(){
    	
    }
    
    @ReceiveEvent(components={ItemComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
    	Vector3f dir = new Vector3f(event.getDirection());
        Vector3f origin = new Vector3f(event.getOrigin());
    	HitResult hitResultWorld = physicsRenderer.rayTrace(origin, dir, 1.5f, StandardCollisionGroup.WORLD);
    	HitResult hitResultCharacter = physicsRenderer.rayTrace(origin, dir, 1.5f, StandardCollisionGroup.CHARACTER);
    	EntityRef hitEntityCharacter = hitResultCharacter.getEntity();
    	EntityRef hitEntityWorld = hitResultWorld.getEntity();
	    ItemComponent item = entity.getComponent(ItemComponent.class);
    	if(hitEntityWorld != null && hitEntityCharacter != null){
    	    Vector3f hitPointWorld = hitResultWorld.getHitPoint();
    		Vector3f hitPointCharacter = hitResultCharacter.getHitPoint();
    		if(hitPointCharacter != null && hitPointWorld != null){
    			float hitLentghWorld =  hitResultWorld.getHitPoint().length();
    			float hitLengthCharacter = hitResultCharacter.getHitPoint().length();
    			if( hitLentghWorld > hitLengthCharacter){
    				hitEntityCharacter.send(new DamageEvent(item.baseDamage, entity));	
    				hitEntityCharacter.send(new HitEvent(entity, hitResultCharacter));
    			}else{
    				hitEntityWorld.send(new DamageEvent(item.baseDamage, entity));	
    			}
    		}else if(hitPointWorld != null){
        	    hitEntityWorld.send(new DamageEvent(item.baseDamage, entity));	
    		}else if(hitPointCharacter != null){
    			hitEntityCharacter.send(new DamageEvent(item.baseDamage, entity));
				hitEntityCharacter.send(new HitEvent(entity, hitResultCharacter));
    		}
    	}
    }
}
