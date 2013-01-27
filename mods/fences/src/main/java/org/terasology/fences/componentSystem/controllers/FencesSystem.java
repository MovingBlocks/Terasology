package org.terasology.fences.componentSystem.controllers;


import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.*;
import org.terasology.events.ActivateEvent;
import org.terasology.events.NoHealthEvent;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockItemComponent;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.management.BlockManager;
import java.util.ArrayList;

@RegisterComponentSystem
public class FencesSystem implements EventHandlerSystem {

    @In
    private WorldRenderer worldRenderer;

    @In
    private WorldProvider worldProvider;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    private final int[][] viewMap = { /*{-2,0},*/ {-1,0}, {0,1}, /*{0,2},*/ {1,0}, /*{2,0},*/ {0,-1}, /*{0,-2}*/};

    @Override
    public void initialise() {
    }

    @ReceiveEvent(components = {BlockItemComponent.class, ItemComponent.class}, priority = EventPriority.PRIORITY_LOW)
    public void onPlaceBlock(ActivateEvent event, EntityRef item) {
        BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);

        BlockComponent targetBlockComp = event.getTarget().getComponent(BlockComponent.class);
        if (targetBlockComp == null) {
            return;
        }

        Side surfaceDir = Side.inDirection(event.getHitNormal());

        Vector3i placementPos = new Vector3i(event.getTarget().getComponent(BlockComponent.class).getPosition());
        placementPos.add(surfaceDir.getVector3i());

        ArrayList<Side> attachedSides = checkAroundBlocks(placementPos, true, null);
        if ( blockItem.blockFamily.getURI().getPackage().equals("fences") ){
            updateBlock(placementPos, attachedSides);
        }
    }

        @ReceiveEvent(components = {BlockComponent.class}, priority = EventPriority.PRIORITY_HIGH)
    public void onDestroyed(NoHealthEvent event, EntityRef entity) {
        BlockComponent blockComp = entity.getComponent(BlockComponent.class);
        Block oldBlock = worldProvider.getBlock(blockComp.getPosition());
        checkAroundBlocks(blockComp.getPosition(), true, blockComp.getPosition());

        if ( oldBlock.getBlockFamily().getURI().getPackage().equals("fences") ){
            BlockFamily newBlock =  BlockManager.getInstance().getBlockFamily("fences:fenceend");

            if ( newBlock != null ){
                worldProvider.setBlock(blockComp.getPosition(), newBlock.getBlockFor(Side.LEFT, Side.RIGHT), oldBlock);
            }
        }
    }
    
    private ArrayList<Side> checkAroundBlocks(Vector3i primePos, boolean checkAround, Vector3i ignorePos){

        ArrayList<Side> aroundFences = new ArrayList<Side>();

        for (int i=0; i<4; i++){

            int x = viewMap[i][0];
            int z = viewMap[i][1];

            Vector3i currentPos = new Vector3i(primePos.x + x, primePos.y, primePos.z + z);

            if ( ignorePos != null && ignorePos.equals(currentPos) ){
                continue;
            }
            Block block = worldProvider.getBlock(currentPos);

            if ( block.isInvisible() ||  block.isLiquid() || block.isPenetrable() || block.isReplacementAllowed() ){
                continue;
            }

            Side addNewSide = null;
            if ( x == 0 && z > 0 ){
                addNewSide = Side.FRONT;
            } else if ( x > 0 && z == 0) {
                addNewSide = Side.RIGHT;
            } else if ( x == 0 && z < 0 ) {
                addNewSide = Side.BACK;
            } else if ( x < 0 && z == 0 ){
                addNewSide = Side.LEFT;
            }

            if ( addNewSide == null || aroundFences.contains(addNewSide) ){
                continue;
            }

            aroundFences.add(addNewSide);
           // Side side = addNewSide.reverse();

            if( checkAround &&  block.getBlockFamily().getURI().getPackage().equals("fences") ){
                ArrayList<Side> around = checkAroundBlocks(currentPos, false, ignorePos);
                updateBlock(currentPos, around);
            }

        }

        return aroundFences;
    }

    private void updateBlock(Vector3i blockPosition, ArrayList<Side> attachedSides){
        Block oldBlock = worldProvider.getBlock(blockPosition);
        String blockFamily = "fences:fencesinglepost";
        
        Side from = Side.LEFT;
        Side to   = Side.RIGHT;
        
        switch ( attachedSides.size() ){
            case 1:
                blockFamily = "fences:fenceend";
                
                if( attachedSides.get(0) == Side.FRONT || attachedSides.get(0) == Side.BACK ){
                    from = attachedSides.get(0).rotateClockwise(1);
                    to   = attachedSides.get(0);
                }else{
                    from = attachedSides.get(0).rotateClockwise(-1);
                    to   = attachedSides.get(0);
                }

                break;
            case 2:

                if ( ( (attachedSides.get(0) == Side.LEFT || attachedSides.get(0) == Side.RIGHT) && (attachedSides.get(1) == Side.LEFT ||  attachedSides.get(1) == Side.RIGHT ) ) ||
                     ( ( attachedSides.get(0) == Side.FRONT || attachedSides.get(0) == Side.BACK ) && ( attachedSides.get(1) == Side.BACK || attachedSides.get(1) == Side.FRONT ) )
                   ){
                    blockFamily = "fences:fence";
                    from = attachedSides.get(0).rotateClockwise(1);
                    to   = attachedSides.get(1);
                } else {
                    blockFamily = "fences:fencecorner";
                    if ( (attachedSides.get(0) == Side.LEFT && attachedSides.get(1) == Side.BACK) ||
                         (attachedSides.get(0) == Side.BACK && attachedSides.get(1) == Side.LEFT)
                      ){
                        from = attachedSides.get(0).rotateClockwise(1);
                        to   = attachedSides.get(1);
                    }else if ( (attachedSides.get(0) == Side.RIGHT && attachedSides.get(1) == Side.FRONT) ||
                               (attachedSides.get(0) == Side.FRONT && attachedSides.get(1) == Side.RIGHT)
                             )
                    {
                        from = attachedSides.get(0).rotateClockwise(2);
                        to   = attachedSides.get(1);
                    }else {
                        from = attachedSides.get(0);
                        to   = attachedSides.get(1);
                    }
                }
                break;
            case 3:
                blockFamily = "fences:fencetee";

                boolean left  = false;
                boolean right = false;
                int diffIndex = 0 ;

                for ( int i=0; i<3; i++  ){
                    if ( attachedSides.get(i) == Side.LEFT ) {
                        left = true;
                    }else if ( attachedSides.get(i) == Side.RIGHT ) {
                        right = true;
                    }
                }

                for ( int i=0; i<3; i++  ){
                    if ( left && right && ( attachedSides.get(i) != Side.RIGHT && attachedSides.get(i) != Side.LEFT) ){
                        diffIndex = i;
                        break;
                    } else if ( (!left || !right) && ( attachedSides.get(i) != Side.FRONT && attachedSides.get(i) != Side.BACK) ){
                        diffIndex = i;
                        break;
                    }
                }

                if ( attachedSides.get(diffIndex) == Side.FRONT || attachedSides.get(diffIndex) == Side.BACK ){
                    from = attachedSides.get(diffIndex).rotateClockwise(2);
                    to   = attachedSides.get(diffIndex);
                }else{
                    from = attachedSides.get(diffIndex);
                    to   = attachedSides.get(diffIndex);
                }
                break;
            case 4:
                blockFamily = "fences:fencecross";
                break;
        }

        BlockFamily newBlock =  BlockManager.getInstance().getBlockFamily(blockFamily);

        if ( newBlock != null ){
            worldProvider.setBlock(blockPosition, newBlock.getBlockFor(from, to), oldBlock);
        }



    }

    @Override
    public void shutdown() {
    }
}
