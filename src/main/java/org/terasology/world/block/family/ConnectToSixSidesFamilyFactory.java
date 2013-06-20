package org.terasology.world.block.family;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.math.*;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockDefinition;

import javax.vecmath.Vector3f;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public abstract class ConnectToSixSidesFamilyFactory implements BlockFamilyFactory {
    public static final String NO_CONNECTIONS="no_connections";
    public static final String ONE_CONNECTION="one_connection";
    public static final String TWO_CONNECTIONS_LINE="line_connection";
    public static final String TWO_CONNECTIONS_CORNER="2d_corner";
    public static final String THREE_CONNECTIONS_CORNER = "3d_corner";
    public static final String THREE_CONNECTIONS_T="2d_t";
    public static final String FOUR_CONNECTIONS_CROSS="cross";
    public static final String FOUR_CONNECTIONS_SIDE ="3d_side";
    public static final String FIVE_CONNECTIONS="five_connections";
    public static final String SIX_CONNECTIONS="all";

    private ConnectionCondition connectionCondition;

    protected ConnectToSixSidesFamilyFactory(ConnectionCondition connectionCondition) {
        this.connectionCondition = connectionCondition;
    }

    @Override
    public BlockFamily createBlockFamily(BlockBuilderHelper blockBuilder, AssetUri blockDefUri, BlockDefinition blockDefinition, JsonObject blockDefJson) {
        Map<Byte, Block> blocksForConnections = Maps.newHashMap();

        // First add all the explicitly defined blocks
        putFixedBlockDefinition(NO_CONNECTIONS, (byte) 0,
                blockBuilder, blockDefUri, blockDefJson, blocksForConnections);
        putFixedBlockDefinition(ONE_CONNECTION, DirectionsUtil.getDirections(Direction.RIGHT),
                blockBuilder, blockDefUri, blockDefJson, blocksForConnections);
        putFixedBlockDefinition(TWO_CONNECTIONS_LINE, DirectionsUtil.getDirections(Direction.LEFT, Direction.RIGHT),
                blockBuilder, blockDefUri, blockDefJson, blocksForConnections);
        putFixedBlockDefinition(TWO_CONNECTIONS_CORNER, DirectionsUtil.getDirections(Direction.RIGHT, Direction.FORWARD),
                blockBuilder, blockDefUri, blockDefJson, blocksForConnections);
        putFixedBlockDefinition(THREE_CONNECTIONS_CORNER, DirectionsUtil.getDirections(Direction.RIGHT, Direction.FORWARD, Direction.UP),
                blockBuilder, blockDefUri, blockDefJson, blocksForConnections);
        putFixedBlockDefinition(THREE_CONNECTIONS_T, DirectionsUtil.getDirections(Direction.RIGHT, Direction.FORWARD, Direction.BACKWARD),
                blockBuilder, blockDefUri, blockDefJson, blocksForConnections);
        putFixedBlockDefinition(FOUR_CONNECTIONS_CROSS, DirectionsUtil.getDirections(Direction.LEFT, Direction.RIGHT, Direction.FORWARD, Direction.BACKWARD),
                blockBuilder, blockDefUri, blockDefJson, blocksForConnections);
        putFixedBlockDefinition(FOUR_CONNECTIONS_SIDE, DirectionsUtil.getDirections(Direction.RIGHT, Direction.FORWARD, Direction.BACKWARD, Direction.UP),
                blockBuilder, blockDefUri, blockDefJson, blocksForConnections);
        putFixedBlockDefinition(FIVE_CONNECTIONS, DirectionsUtil.getDirections(Direction.RIGHT, Direction.FORWARD, Direction.BACKWARD, Direction.UP, Direction.DOWN),
                blockBuilder, blockDefUri, blockDefJson, blocksForConnections);
        putFixedBlockDefinition(SIX_CONNECTIONS, (byte) 63,
                blockBuilder, blockDefUri, blockDefJson, blocksForConnections);

        // Now make sure we have all combinations based on the basic set (above) and rotations
        for (byte connections = 0; connections<64; connections++) {
            if (!blocksForConnections.containsKey(connections)) {
                for (Rotation rotation : Rotation.allPossibleRotations()) {
                    final Collection<Direction> connectedDirections = DirectionsUtil.getDirections(connections);
                    Set<Direction> rotatedDirections = Sets.newHashSet();
                    for (Direction connectedDirection : connectedDirections) {
                        final Vector3i rotatedSideVector = rotation.rotate(Side.inDirection(connectedDirection.getVector3f())).getVector3i();
                        rotatedDirections.add(Direction.inDirection(rotatedSideVector.x, rotatedSideVector.y, rotatedSideVector.z));
                    }
                    byte rotatedConnection = DirectionsUtil.getDirections(rotatedDirections);
                }
            }
        }

        final Block archetypeBlock = blocksForConnections.get(DirectionsUtil.getDirections(Direction.LEFT, Direction.RIGHT));
        return new ConnectToSixSidesFamily(connectionCondition, new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName()), blockDefinition.categories,
                archetypeBlock, blocksForConnections);
    }

    private void putFixedBlockDefinition(String definition, byte sides, BlockBuilderHelper blockBuilder, AssetUri blockDefUri, JsonObject blockDefJson, Map<Byte, Block> blocksForConnections) {
        BlockDefinition blockDefNoConnections = blockBuilder.getBlockDefinitionForSection(blockDefJson, definition);
        blocksForConnections.put(sides, blockBuilder.constructSimpleBlock(blockDefUri, blockDefNoConnections));
    }
}
