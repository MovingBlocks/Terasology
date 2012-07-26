package org.terasology.componentSystem.action;

import java.awt.geom.CubicCurve2D;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.terasology.components.AABBCollisionComponent;
import org.terasology.components.CharacterMovementComponent;
import org.terasology.components.HealthComponent;
import org.terasology.components.PlayerComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.components.RadarComponent;
import org.terasology.components.actions.MeleeAttackActionComponent;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.ReceiveEvent;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.events.ActivateEvent;
import org.terasology.events.DamageEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.ComponentSystemManager;
import org.terasology.model.structures.AABB;
import org.terasology.model.structures.RayIntersection;

import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/**
 * @author Immortius <immortius@gmail.com>
 */
@RegisterComponentSystem
public class MeleeAttackAction implements EventHandlerSystem {
    private final static Logger logger = Logger.getLogger("MeleeAttackAction");

 
    public void initialise() {
   }
    
    @Override
    public void shutdown(){
    	
    }
//    @ReceiveEvent(components={MeleeAttackActionComponent.class})
//    public void onActivate(ActivateEvent event, EntityRef entity) {
//    	System.out.println("Melee-Attack By:"+entity.getId());
//    	EntityRef instigator = event.getInstigator();
//    	RadarComponent radar = instigator.getComponent(RadarComponent.class);
//        Vector3f dir = new Vector3f(event.getDirection());
//        Vector3f origin = new Vector3f(event.getInstigatorLocation());
//        if (origin == null || dir == null || radar == null) return;
//        for (EntityRef entityRef : radar.getDetected()) {
//        	System.out.println("Teste Collision mit Entity:"+entityRef.getId());
//            Logger.getLogger(MeleeAttackAction.class.getName()).log(Level.SEVERE, "UEBERPRUEFE ENTITY "+entityRef.getId());
//            LocationComponent location = entityRef.getComponent(LocationComponent.class);
//            AABBCollisionComponent collision = entityRef.getComponent(AABBCollisionComponent.class);
//            AABB aabb = new AABB(location.getWorldPosition(),collision.getExtents());
//            if(RayIntersection.intersections(aabb,new Vector3d(origin), new Vector3d(dir))){
//                Logger.getLogger(MeleeAttackAction.class.getName()).log(Level.SEVERE, "ENTITY HAS BEEN HIT");
//            	DamageEvent damageEvent = new DamageEvent(10, entityRef);
//            	entityRef.send(damageEvent);
//            }  	
//       }
//    }
    
    @ReceiveEvent(components={MeleeAttackActionComponent.class})
    public void onActivate(ActivateEvent event, EntityRef entity) {
      Logger.getLogger(MeleeAttackAction.class.getName()).log(Level.SEVERE, "UEBERPRUEFE ENTITY "+event.getInstigator().getId());
    	System.out.println("Melee-Attack:"+event.getInstigator());
    	RadarComponent radar  = event.getInstigator().getComponent(RadarComponent.class);
    	System.out.println("Melee-Attack:"+radar.getDetected().size());

    	Vector3f dir = new Vector3f(event.getDirection());
        Vector3f origin = new Vector3f(event.getOrigin());
        if (origin == null || dir == null) return;
        Set<EntityRef> detected = radar.getDetected();
        for(EntityRef entityRef : detected){
        	org.terasology.components.world.LocationComponent location = entityRef.getComponent(org.terasology.components.world.LocationComponent.class);
        	AABBCollisionComponent collision = entityRef.getComponent(AABBCollisionComponent.class);
            AABB aabb = new AABB(location.getWorldPosition(),collision.getExtents());
            if(RayIntersection.intersections(aabb,new Vector3d(origin), new Vector3d(dir))){
            	entityRef.send(new DamageEvent(10, event.getInstigator()));
            }  	
        }
    }
//    
//    @ReceiveEvent(components={MeleeAttackActionComponent.class})
//    public void onActivate(ActivateEvent event, EntityRef entity) {
//    	EntityRef entity = event.getInstigator();
//    	RadarComponent radar = entity.getComponent(RadarComponent.class);
//        Vector3f dir = new Vector3f(event.getDirection());
//        Vector3f origin = new Vector3f(event.getLocation());
//        if (origin == null || dir == null) return;
//        for (EntityRef entityRef : radar.getDetected()) {
//            Logger.getLogger(MeleeAttackAction.class.getName()).log(Level.SEVERE, "UEBERPRUEFE ENTITY "+entityRef.getId());
//            LocationComponent location = entityRef.getComponent(LocationComponent.class);
//            AABBCollisionComponent collision = entityRef.getComponent(AABBCollisionComponent.class);
//            AABB aabb = new AABB(location.getWorldPosition(),collision.getExtents());
//            if(RayBlockIntersection.intersections(aabb,new Vector3d(origin), new Vector3d(dir))){
//                Logger.getLogger(MeleeAttackAction.class.getName()).log(Level.SEVERE, "ENTITY HAS BEEN HIT");
//            	DamageEvent damageEvent = new DamageEvent(10, entity);
//            	entityRef.send(damageEvent);
//            }  	
//       }
//    }
}
