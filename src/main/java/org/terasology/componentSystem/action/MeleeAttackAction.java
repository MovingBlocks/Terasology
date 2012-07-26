package org.terasology.componentSystem.action;

import java.util.Set;
import java.util.logging.Logger;

import org.terasology.components.AABBCollisionComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.components.RadarComponent;
import org.terasology.components.actions.MeleeAttackActionComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.events.DamageEvent;
import org.terasology.model.structures.AABB;
import org.terasology.model.structures.RayEntityIntersection;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * @author Andre Herber <andre.herber@yahoo.de>
 * Basic Melee Attack using Raytracing against the AABBCollisonComponent to check for Collision. 
 * TODO Use the Mesh/Bounding Box of the Weapon/Entities to check the Collision. 
 */
@RegisterComponentSystem
public class MeleeAttackAction implements EventHandlerSystem {
    private final static Logger logger = Logger.getLogger("MeleeAttackAction");

 
    public void initialise() {
   }
    
    @Override
    public void shutdown(){
    	
    }
    
    @ReceiveEvent(components={MeleeAttackActionComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
    	RadarComponent radar  = event.getInstigator().getComponent(RadarComponent.class);
    	ItemComponent item = entity.getComponent(ItemComponent.class);
    	Vector3f dir = new Vector3f(event.getDirection());
        Vector3f origin = new Vector3f(event.getOrigin());
        if (origin == null || dir == null) return;
        Set<EntityRef> detected = radar.getDetected();
        for(EntityRef entityRef : detected){
        	LocationComponent location = entityRef.getComponent(LocationComponent.class);
        	AABBCollisionComponent collision = entityRef.getComponent(AABBCollisionComponent.class);
            AABB aabb = new AABB(location.getWorldPosition(),collision.getExtents());
            //TODO Use the Mesh/Bounding Box for Collision Detection rather the Raytracing
            if(RayEntityIntersection.intersections(aabb,new Vector3d(origin), new Vector3d(dir), 2)){//TODO Range should be Part of the Weapon/Item Definition
            	//TODO Delay Damage 
            	entityRef.send(new DamageEvent(item.baseDamage, event.getInstigator()));
            }  	
        }
    }
}
