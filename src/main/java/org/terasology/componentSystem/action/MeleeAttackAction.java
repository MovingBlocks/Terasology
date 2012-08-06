package org.terasology.componentSystem.action;

import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.events.DamageEvent;
import org.terasology.game.CoreRegistry;

import org.terasology.physics.BulletPhysics;
import org.terasology.physics.HitResult;
import org.terasology.physics.StandardCollisionGroup;

import javax.vecmath.Vector3f;

/**
 * @author aherber
 */
@RegisterComponentSystem
public class MeleeAttackAction implements EventHandlerSystem {

   private BulletPhysics physicsRenderer;

    public void initialise() {
        physicsRenderer = CoreRegistry.get(BulletPhysics.class);
   }
	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
	}
    
    @ReceiveEvent(components={ItemComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
        Vector3f dir = new Vector3f(event.getDirection());
        Vector3f origin = new Vector3f(event.getInstigatorLocation());
        ItemComponent itemComponent = entity.getComponent(ItemComponent.class);
        if (origin == null || dir == null) return;
		HitResult hitResult = physicsRenderer.rayTrace(origin, dir, 3, StandardCollisionGroup.CHARACTER);
		EntityRef hitEntity = hitResult.getEntity(); 
		if(hitEntity != null) {
            DamageEvent damageEvent = new DamageEvent(itemComponent.baseDamage, entity);
            hitEntity.send(damageEvent); 
       }
    }
}
