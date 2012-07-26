package org.terasology.componentSystem.common;

import java.util.ArrayList;
import java.util.Set;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.RadarComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.components.AABBCollisionComponent;
import org.terasology.model.structures.AABB;
import org.terasology.events.LocationChangeEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.entitySystem.*;
import org.terasology.game.CoreRegistry;

/**
 * @author aherber andre.herber@yahoo.de
 *
 * Checks if The Collisionvolume of an Entity, that has changed its Location, overlaps any detectionarea.
 * If the volumes overlap, the entity is registered as a detected Entity in the RadarComponent.
 * Otherwise if the Collisionvolume of an Entity lays outside of the detectionarea the Entity 
 * gets removed from the Set of detected Entities.     
 * @param LocationChangeEvent Event send by Entity who has change its location 
 * @param EntityRef entity    Entity who has Changed its location 
 */ 
@RegisterComponentSystem
public class RadarSystem implements EventHandlerSystem, UpdateSubscriberSystem{

    private EntityManager entityManager;
	
    @Override
    public void initialise() {
	  entityManager = CoreRegistry.get(EntityManager.class);
    }
    
    @Override
    public void update(float delta) {  
    }
     

    @Override
    public void shutdown(  ) {
    }
  
  /**
   * Checks if The Collisionvolume of an Entity, that has changed its Location, overlaps any detectionarea.     
   * @param LocationChangeEvent Event send by an Entity who has changed its location.
   * @param EntityRef entity    Entity who has changed its location. 
   */  
  @ReceiveEvent(components = {AABBCollisionComponent.class, LocationComponent.class })
  public void onMove(LocationChangeEvent event, EntityRef entity) {
  	EntityRef instigator = event.getInstigator();
	AABB entityAABB = new AABB(event.getInstigatorLocation(),instigator.getComponent(AABBCollisionComponent.class).getExtents());
  	if(entity != null ){
  		checkDetectionAreas(entityAABB, entity);
  	 }
  }
  
  /*
   * Removes an dead Entity from All Sets of detected Entities 
   * @param NoHealthEvent event send by dead Entity 
   * @param EntityRef entity    Entity who has died
   */
  @ReceiveEvent(components = {AABBCollisionComponent.class})
  public void onDeath(NoHealthEvent event, EntityRef entity) {
	for(EntityRef curEntity : entityManager.iteratorEntities(RadarComponent.class, LocationComponent.class)){
		if(curEntity != null && !curEntity.equals(entity)){
			RadarComponent radar = curEntity.getComponent(RadarComponent.class);
			if(radar != null){
				radar.remove(entity);
				curEntity.saveComponent(radar);
			}
		}
	}
    entity.destroy();
  }
  
  /**
   * Helpermethod that checks if the Collisionvolume of an Entity overlaps any detectionarea,    
   * If the volumes overlap, the entity is registered as a detected Entity in the RadarComponent.
   * Otherwise if the Collisionvolume of an Entity lays outside of the detectionarea the Entity gets removed.  
   * @param EntityRef entity =  Entity who has changed its location
   * @param AABB entityAABB  =  CollisionVolume of the Entity that is checked against the detectionarea 
   */ 
  public void checkDetectionAreas(AABB entityAABB, EntityRef entity){
	for(EntityRef curEntity : entityManager.iteratorEntities(RadarComponent.class, LocationComponent.class)){
		if(curEntity != null && !curEntity.equals(entity)){
			RadarComponent radar = curEntity.getComponent(RadarComponent.class);
			LocationComponent location = curEntity.getComponent(LocationComponent.class);
			if(radar != null && location != null){
				AABB detectionArea = new AABB(location.getWorldPosition(),radar.getDetectionArea());
				if(entityAABB.overlaps(detectionArea)){
					radar.add(entity);
				}else{
					radar.remove(entity);
				}
				curEntity.saveComponent(radar);
			}
		}
	 }
  }
  
//TODO change detectionArea when viewing distance is changed
//  @ReceiveEvent(components = {RadarComponent.class})
//    public void onChangeViewingDistance(ChangeViewingDistanceEvent event, EntityRef entity) {
//    	int activeViewingDistance = org.terasology.logic.manager.Config.getInstance().getActiveViewingDistance();
//      RadarComponent radar = entity.getComponent(RadarComponent.class);
//      AABB detectionArea = radar.getDetectionArea();
//      detectionArea.getDimensions().x = activeViewingDistance*Chunk.SIZE_X;
//      detectionArea.getDimensions().z = activeViewingDistance*Chunk.SIZE_Z;
//      detectionArea.getDimensions().y = Chunk.SIZE_Y;
//    	entity.saveComponent(radar);
//      updateDetected(entity);
//   }
  
  /**
   *.Updates the detectionstatus of all detected Entities of an Entity  
   * @param EntityRef entity =  Entity whose detection gets updated
   */ 
  public void updateDetected(EntityRef entity){
	  RadarComponent radar = entity.getComponent(RadarComponent.class);
	  LocationComponent location = entity.getComponent(LocationComponent.class);
	  if(radar != null && location != null){
		  AABB detectionArea = new AABB(location.getWorldPosition(),radar.getDetectionArea());
		  for(EntityRef detected : radar.getDetected()){
			  LocationComponent detectedLocation = entity.getComponent(LocationComponent.class);
			  AABB entityAABB = new AABB(detectedLocation.getWorldPosition(),detected.getComponent(AABBCollisionComponent.class).getExtents());
			  if(entityAABB.overlaps(detectionArea)){
				  radar.add(detected);
			  }else{
				  radar.remove(detected);
			  }
			  entity.saveComponent(radar);
		 }
	  }
  }
  
  
  //TODO Maybe a seperate  Filter Class
  /**
   *.filters the given Entities by the given filterCriterea  
   * @param EntityRef entity =  Entity whose detection gets updated
   */ 
  public void filterByType(Set<EntityRef> entities, Set<Class<? extends org.terasology.entitySystem.Component>> filter){
	  
  }
  
  /**
   *.filters the given Entities by the given filterCriterea  
   * @param EntityRef entity =  Entity whose detection gets updated
   */ 
  public void filterByExample(Set<EntityRef> entities, Set<? extends org.terasology.entitySystem.Component> filter){
	  
  }
}
 
