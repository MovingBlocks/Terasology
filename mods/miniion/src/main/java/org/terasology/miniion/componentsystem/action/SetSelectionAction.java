package org.terasology.miniion.componentsystem.action;

import java.util.Set;

import org.terasology.entitySystem.*;
import org.terasology.events.ActivateEvent;
import org.terasology.math.Vector3i;
import org.terasology.miniion.components.ZoneSelectionComponent;
import org.terasology.rendering.world.BlockGrid;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

@RegisterComponentSystem
public class SetSelectionAction implements EventHandlerSystem{

	@In
    private WorldProvider worldProvider;
	
	@Override
	public void initialise() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
	
	@ReceiveEvent(components = ZoneSelectionComponent.class)
	public void onActivate(ActivateEvent event, EntityRef entity) {
		 ZoneSelectionComponent comp = entity.getComponent(ZoneSelectionComponent.class);
		 BlockGrid grid = comp.blockGrid;
		
		 Set<BlockGrid.GridPosition> gridPositions = grid.getGridPositions();

		 if (gridPositions.size() == 0) {
		  	 comp.startpos = new Vector3i(event.getTargetLocation());
		  	 comp.blockGrid.addGridPosition(comp.startpos, worldProvider.getBlock(comp.startpos).getId());
		 }
		 else if (gridPositions.size() == 1) {
		  	comp.endpos = new Vector3i(event.getTargetLocation());
		  	comp.blockGrid.addGridPosition(comp.endpos, worldProvider.getBlock(comp.endpos).getId());
		  	Vector3i minbounds = comp.blockGrid.getMinBounds();
		  	Vector3i maxbounds = comp.blockGrid.getMaxBounds();
		  	maxbounds.add(1, 1, 1);
			 for(int x = minbounds.x; x < maxbounds.x; x++){
				 for(int y = minbounds.y; y < maxbounds.y; y++){
					 for(int z = minbounds.z; z < maxbounds.z; z++){
						 Block tmpblock = worldProvider.getBlock(x, y, z);
						 if(!tmpblock.isInvisible()){
							 grid.addGridPosition(new Vector3i(x, y, z), tmpblock.getId());
						 }
					 }
				 }
			 }
		 }	
		 else {
			 comp.blockGrid.clear();
		 }	        			 			
		 entity.saveComponent(comp);		 
	 }
}
