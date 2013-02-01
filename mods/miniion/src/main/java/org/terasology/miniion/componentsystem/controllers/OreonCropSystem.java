package org.terasology.miniion.componentsystem.controllers;

import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.components.world.LocationComponent;
import org.terasology.entityFactory.BlockItemFactory;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.entitySystem.EventHandlerSystem;
import org.terasology.entitySystem.RegisterComponentSystem;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.math.Vector3i;
import org.terasology.miniion.components.OreonCropComponent;

import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.management.BlockManager;

@RegisterComponentSystem
public class OreonCropSystem implements EventHandlerSystem, UpdateSubscriberSystem{
	
	private EntityManager entityManager;
	private Timer timer;
	private WorldProvider worldprovider;

	@Override
	public void initialise() {
		entityManager = CoreRegistry.get(EntityManager.class);
		timer = CoreRegistry.get(Timer.class);
		worldprovider = CoreRegistry.get(WorldProvider.class);
	}

	@Override
	public void shutdown() {
		
	}

	@Override
	public void update(float delta) {
		for (EntityRef entity : entityManager.iteratorEntities(OreonCropComponent.class)){
			if(entity.hasComponent(BlockComponent.class)){
				OreonCropComponent crop = entity.getComponent(OreonCropComponent.class);
				if(crop.fullgrown){
					return;
				}
				if(crop.lastgrowthcheck == -1){
					crop.lastgrowthcheck = timer.getTimeInMs();
					return;
				}
				if(timer.getTimeInMs() - crop.lastgrowthcheck > 54000000){
					crop.lastgrowthcheck = timer.getTimeInMs();
					if(entity.hasComponent(LocationComponent.class)){
						LocationComponent locComponent = entity.getComponent(LocationComponent.class);
						Block oldblock = worldprovider.getBlock(locComponent.getWorldPosition());
						String oldUri = oldblock.getURI().getFamily();
						byte currentstage = Byte.parseByte(oldUri.substring(oldUri.length() - 1, oldUri.length()));
						if(crop.stages -1 > currentstage){
							currentstage++;
							if(currentstage == crop.stages -1){
								crop.fullgrown = true;
							}
							oldUri = oldUri.substring(0, oldUri.length()-1) + currentstage;
							Block newBlock = BlockManager.getInstance().getBlock(oldblock.getURI().getPackage() + ":" + oldUri);
							worldprovider.setBlock(new Vector3i(locComponent.getWorldPosition()), newBlock, oldblock);
						}
						entity.saveComponent(crop);
					}					
				}
			}
		}
		
	}

}
