/*
 * Copyright 2013  Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.world.block.family;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockAdjacentType;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;

import java.util.*;

public class ConnectToAdjacentBlockFamily extends AbstractBlockFamily  {

    public static final int[][] viewMap = { {-1,0}, {0,1},  {1,0}, {0,-1} };

    public static final int[][] mixedMap = { {-1,-1,0}, {-1,0,0}, {0,-1,1}, {0, 0,1},  {1,-1,0}, {1, 0,0}, {0,-1,-1}, {0, 0,-1} };

    public static final int[][] viewFullMapWithoutDiagonal = {
            {-1,-1,0}, {0,-1,1},  {1,-1,0}, {0,-1,-1},
            {-1,0,0},  {0, 0,1},  {1, 0,0}, {0, 0,-1},
            {-1, 1,0}, {0, 1,1},  {1, 1,0}, {0, 1,-1}
    };
    public static final int[][] viewFullMap = {
            {-1,0,0},  {0, 0,1},  {1, 0,0}, {0, 0,-1}, {-1, 0,1}, {1, 0,1},  {1, 0,-1}, {-1, 0,-1},
            {-1,-1,0}, {0,-1,1},  {1,-1,0}, {0,-1,-1}, {-1,-1,1}, {1,-1,1},  {1,-1,-1}, {-1,-1,-1},
            {-1, 1,0}, {0, 1,1},  {1, 1,0}, {0, 1,-1}, {-1, 1,1}, {1, 1,1},  {1, 1,-1}, {-1, 1,-1},
    };
    private Map<BlockAdjacentType,EnumMap<Side,Block>> blockVariations = Maps.newHashMap();

    public ConnectToAdjacentBlockFamily(BlockUri uri, Map<BlockAdjacentType,EnumMap<Side,Block>> blockVariations, String[] categories) {
        super(uri, Arrays.asList(categories));

        for (BlockAdjacentType blockType : BlockAdjacentType.values()) {

            if ( !blockVariations.containsKey(blockType) ) {
                continue;
            }

            if ( ! this.blockVariations.containsKey( blockType ) ){
                this.blockVariations.put( blockType, Maps.<Side, Block>newEnumMap(Side.class));
            }

            for (Side side : Side.horizontalSides()) {
                Block block = blockVariations.get(blockType).get(side);
                if (block == null) {
                    continue;
                }
                this.blockVariations.get(blockType).put(side, block);
                block.setBlockFamily(this);
                block.setUri(new BlockUri(uri, blockType.toString() + "." + side.name()));
            }
        }
    }

    @Override
    public Block getBlockFor(Side attachmentSide, Side direction) {
        if (attachmentSide.isHorizontal()) {
            return blockVariations.get(BlockAdjacentType.ARCHETYPE).get(attachmentSide);
        }

        return blockVariations.get(BlockAdjacentType.ARCHETYPE).get(direction);
    }

    public Block getBlockFor(Vector3i position, WorldProvider world) {
        AroundBlocksInfo info = checkAroundBlocks(position,  world);
        return getBlockByAttachedSides(info);
    }

    @Override
    public Block getArchetypeBlock() {
        return blockVariations.get(BlockAdjacentType.ARCHETYPE).get(Side.FRONT);
    }

    @Override
    public Block getBlockFor(BlockUri blockUri) {
        if (getURI().equals(blockUri.getFamilyUri())) {
            try {
                return blockVariations.get(BlockAdjacentType.ARCHETYPE).get(Side.FRONT);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Iterable<Block> getBlocks() {
        ArrayList<Block> allBlocks = new ArrayList<Block>();
        for ( EnumMap<Side,Block> blocks : blockVariations.values() ){
            allBlocks.addAll(blocks.values());
        }
        return allBlocks;
    }

    public boolean hasType(BlockAdjacentType type){
        return blockVariations.containsKey(type);
    }

    private AroundBlocksInfo checkAroundBlocks(Vector3i primePos, WorldProvider worldProvider){

        ArrayList<Side> aroundBlocks = new ArrayList<Side>();
        ArrayList<Side> upSide       = new ArrayList<Side>();

        Block mainBlock = getArchetypeBlock();

        boolean lookAtDown = false;

        if ( mainBlock.getBlockFamily() instanceof ConnectToAdjacentBlockFamily && ( (ConnectToAdjacentBlockFamily)mainBlock.getBlockFamily()).hasType(BlockAdjacentType.SLOPE) ){
            lookAtDown = true;
        }

        int x = 0, z = 0, firstX = 0, firstZ = 0;
        int size = aroundBlocks.size();

        for (int i=0; i<8; i++){

            if( !lookAtDown && mixedMap[i][1] < 0){
                continue;
            }

            lookAtBlock( primePos, new Vector3i(mixedMap[i][0], mixedMap[i][1], mixedMap[i][2]), aroundBlocks, worldProvider);

            if ( mixedMap[i][1] == 0 && size != aroundBlocks.size()){
                firstX = mixedMap[i][0];
                firstZ = mixedMap[i][2];
                size   =  aroundBlocks.size();
            }

        }

        if ( mainBlock.isCheckHeightDiff() && lookAtDown && aroundBlocks.size() < 2 ){
            switch ( aroundBlocks.size() ){
                case 1:
                    if ( firstX!=0 ){
                        firstX = firstX - ( firstX * 2);
                    }

                    if ( firstZ!=0 ){
                        firstZ = firstZ - ( firstZ * 2);
                    }
                    lookAtBlock( primePos, new Vector3i(firstX, 1, firstZ), upSide, worldProvider);
                    break;
                case 0:
                    for (int i=0; i<4; i++){
                        lookAtBlock( primePos, new Vector3i(viewMap[i][0], 1, viewMap[i][1]), upSide, worldProvider);
                    }

                    break;
            }
        }

        AroundBlocksInfo result  = new AroundBlocksInfo();

        if ( upSide.size() > 0 ){
            result.upSide = upSide.get(0);
            aroundBlocks.add(upSide.get(0));
        }

        result.aroundBlocks = aroundBlocks;

        return result;
    }

    private void lookAtBlock(Vector3i mainBlockPos, Vector3i diffCord, ArrayList<Side> aroundBlocks, WorldProvider worldProvider){


        Block mainBlock = getArchetypeBlock();

        Vector3i currentPos = new Vector3i(mainBlockPos.x + diffCord.x, mainBlockPos.y + diffCord.y, mainBlockPos.z + diffCord.z);

        Block block = worldProvider.getBlock(currentPos);

        if ( !mainBlock.isCanConnectToAllBlocks() &&
                !mainBlock.getBlockFamily().getArchetypeBlock().equals(block.getBlockFamily().getArchetypeBlock()) ){

            if ( mainBlock.getAcceptedToConnectBlocks().size() ==0 ||
                    ( mainBlock.getAcceptedToConnectBlocks().size() > 0 &&
                            !mainBlock.getAcceptedToConnectBlocks().contains( block.getBlockFamily().getURI().toString() ) )
                    ){
                return;
            }
        }

        if ( ( mainBlock.isCanConnectToAllBlocks() && (  block.isInvisible() ||  block.isLiquid() || block.isPenetrable() || block.isReplacementAllowed() || block.isTranslucent() ) ) ){
            if ( mainBlock.getAcceptedToConnectBlocks().size() ==0 ||
                    ( mainBlock.getAcceptedToConnectBlocks().size() > 0 &&
                            !mainBlock.getAcceptedToConnectBlocks().contains( block.getBlockFamily().getURI().toString() ) )
                    ){
                return;
            }
        }

        Side addNewSide =  Side.inDirection(diffCord.x, 0, diffCord.z);

        if ( aroundBlocks.contains(addNewSide) ){
            return;
        }

        aroundBlocks.add(addNewSide);
    }

    private Block getBlockByAttachedSides(AroundBlocksInfo aroundInfo){

        ArrayList<Side> attachedSides = aroundInfo.aroundBlocks;

        BlockAdjacentType variationBlock = BlockAdjacentType.SINGLE;

        Side side = Side.LEFT;

        switch ( attachedSides.size() ){
            case 1:
                if ( aroundInfo.upSide != null ){
                    variationBlock = BlockAdjacentType.SLOPE;
                    side = attachedSides.get(0).rotateClockwise( 2 );
                }else{
                    variationBlock = BlockAdjacentType.END;
                    side = attachedSides.get(0).rotateClockwise( -1 );
                }

                break;
            case 2:

                if ( ( (attachedSides.get(0) == Side.LEFT || attachedSides.get(0) == Side.RIGHT) && (attachedSides.get(1) == Side.LEFT ||  attachedSides.get(1) == Side.RIGHT ) ) ||
                        ( ( attachedSides.get(0) == Side.FRONT || attachedSides.get(0) == Side.BACK ) && ( attachedSides.get(1) == Side.BACK || attachedSides.get(1) == Side.FRONT ) )
                        ){

                    if ( aroundInfo.upSide != null ){
                        variationBlock = BlockAdjacentType.SLOPE;
                        side = attachedSides.get(0);
                    }else{
                        variationBlock = BlockAdjacentType.PLAIN;
                        side = attachedSides.get(0).rotateClockwise(-1);
                    }

                } else {
                    variationBlock = BlockAdjacentType.CORNER;
                    if ( (attachedSides.get(0) == Side.LEFT && attachedSides.get(1) == Side.FRONT) ||
                            (attachedSides.get(0) == Side.FRONT && attachedSides.get(1) == Side.LEFT)
                            )
                    {
                        side = attachedSides.get(0).rotateClockwise(1);
                    }else {
                        side = attachedSides.get(0);
                    }
                }
                break;
            case 3:
                variationBlock = BlockAdjacentType.TEE;
                side = attachedSides.get( getDiffSideForTree(attachedSides) );
                break;
            case 4:
                variationBlock = BlockAdjacentType.CROSS;
                break;
        }

        if ( blockVariations.containsKey(variationBlock) ){
            return blockVariations.get(variationBlock).get(side);
        }

        return getArchetypeBlock();
    }

    private int getDiffSideForTree( ArrayList<Side> attachedSides ){
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

        return diffIndex;
    }

    private Side getDiffSideForTreeSide( ArrayList<Side> attachedSides ){
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

        return attachedSides.get(diffIndex);
    }

    private class AroundBlocksInfo {
        public Side upSide = null;
        public ArrayList<Side> aroundBlocks = Lists.newArrayList();

    }

}