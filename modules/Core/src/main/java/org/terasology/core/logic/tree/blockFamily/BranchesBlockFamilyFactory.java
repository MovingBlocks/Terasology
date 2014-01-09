package org.terasology.core.logic.tree.blockFamily;

import org.terasology.core.logic.tree.PartOfTreeComponent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.family.ConnectToSixSidesFamilyFactory;
import org.terasology.world.block.family.ConnectionCondition;
import org.terasology.world.block.family.RegisterBlockFamilyFactory;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterBlockFamilyFactory("Core:branch")
public class BranchesBlockFamilyFactory extends ConnectToSixSidesFamilyFactory {
    public BranchesBlockFamilyFactory() {
        super(new BranchConnectionCondition(), (byte) 63);
    }

    private static class BranchConnectionCondition implements ConnectionCondition {
        @Override
        public boolean isConnectingTo(Vector3i blockLocation, Side connectSide, WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry) {
            Vector3i neighborLocation = new Vector3i(blockLocation);
            neighborLocation.add(connectSide.getVector3i());

            EntityRef neighborEntity = blockEntityRegistry.getBlockEntityAt(neighborLocation);
            return neighborEntity != null && connectsToNeighbor(neighborEntity);
        }

        private boolean connectsToNeighbor(EntityRef neighborEntity) {
            return neighborEntity.getComponent(PartOfTreeComponent.class) != null;
        }
    }
}
