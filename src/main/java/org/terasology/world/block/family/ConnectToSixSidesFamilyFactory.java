package org.terasology.world.block.family;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import org.terasology.asset.AssetUri;
import org.terasology.math.*;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockDefinition;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ConnectToSixSidesFamilyFactory implements BlockFamilyFactory {
    public static final String NO_CONNECTIONS = "no_connections";
    public static final String ONE_CONNECTION = "one_connection";
    public static final String TWO_CONNECTIONS_LINE = "line_connection";
    public static final String TWO_CONNECTIONS_CORNER = "2d_corner";
    public static final String THREE_CONNECTIONS_CORNER = "3d_corner";
    public static final String THREE_CONNECTIONS_T = "2d_t";
    public static final String FOUR_CONNECTIONS_CROSS = "cross";
    public static final String FOUR_CONNECTIONS_SIDE = "3d_side";
    public static final String FIVE_CONNECTIONS = "five_connections";
    public static final String SIX_CONNECTIONS = "all";

    private ConnectionCondition connectionCondition;
    private static Matrix3f[] possibleTransformations = new Matrix3f[64];

    static {
        Matrix3f[] xRot = new Matrix3f[4];
        Matrix3f[] yRot = new Matrix3f[4];
        Matrix3f[] zRot = new Matrix3f[4];
        for (int i = 0; i < 4; i++) {
            xRot[i] = new Matrix3f();
            xRot[i].setIdentity();
            xRot[i].rotX(i * ((float) (Math.PI / 2)));

            yRot[i] = new Matrix3f();
            yRot[i].setIdentity();
            yRot[i].rotY(i * ((float) (Math.PI / 2)));

            zRot[i] = new Matrix3f();
            zRot[i].setIdentity();
            zRot[i].rotZ(i * ((float) (Math.PI / 2)));
        }

        for (int i = 0; i < 64; i++) {
            possibleTransformations[i] = new Matrix3f(xRot[i / 16]);
            possibleTransformations[i].mul(yRot[(i & 15) / 4]);
            possibleTransformations[i].mul(zRot[i & 3]);
        }
    }

    protected ConnectToSixSidesFamilyFactory(ConnectionCondition connectionCondition) {
        this.connectionCondition = connectionCondition;
    }

    @Override
    public BlockFamily createBlockFamily(BlockBuilderHelper blockBuilder, AssetUri blockDefUri, BlockDefinition blockDefinition, JsonObject blockDefJson) {
        Map<Byte, BlockDefinition>[] basicBlocks = new Map[7];
        Map<Byte, Block> blocksForConnections = Maps.newHashMap();

        basicBlocks[0] = Maps.newHashMap();
        basicBlocks[0].put((byte) 0,
                getBlockDefinition(NO_CONNECTIONS, blockBuilder, blockDefJson));

        basicBlocks[1] = Maps.newHashMap();
        basicBlocks[1].put(DirectionsUtil.getDirections(Direction.FORWARD),
                getBlockDefinition(ONE_CONNECTION, blockBuilder, blockDefJson));

        basicBlocks[2] = Maps.newHashMap();
        basicBlocks[2].put(DirectionsUtil.getDirections(Direction.FORWARD, Direction.BACKWARD),
                getBlockDefinition(TWO_CONNECTIONS_LINE, blockBuilder, blockDefJson));
        basicBlocks[2].put(DirectionsUtil.getDirections(Direction.RIGHT, Direction.FORWARD),
                getBlockDefinition(TWO_CONNECTIONS_CORNER, blockBuilder, blockDefJson));

        basicBlocks[3] = Maps.newHashMap();
        basicBlocks[3].put(DirectionsUtil.getDirections(Direction.RIGHT, Direction.FORWARD, Direction.UP),
                getBlockDefinition(THREE_CONNECTIONS_CORNER, blockBuilder, blockDefJson));
        basicBlocks[3].put(DirectionsUtil.getDirections(Direction.RIGHT, Direction.FORWARD, Direction.BACKWARD),
                getBlockDefinition(THREE_CONNECTIONS_T, blockBuilder, blockDefJson));

        basicBlocks[4] = Maps.newHashMap();
        basicBlocks[4].put(DirectionsUtil.getDirections(Direction.LEFT, Direction.RIGHT, Direction.FORWARD, Direction.BACKWARD),
                getBlockDefinition(FOUR_CONNECTIONS_CROSS, blockBuilder, blockDefJson));
        basicBlocks[4].put(DirectionsUtil.getDirections(Direction.RIGHT, Direction.FORWARD, Direction.BACKWARD, Direction.UP),
                getBlockDefinition(FOUR_CONNECTIONS_SIDE, blockBuilder, blockDefJson));

        basicBlocks[5] = Maps.newHashMap();
        basicBlocks[5].put(DirectionsUtil.getDirections(Direction.RIGHT, Direction.FORWARD, Direction.BACKWARD, Direction.UP, Direction.DOWN),
                getBlockDefinition(FIVE_CONNECTIONS, blockBuilder, blockDefJson));

        basicBlocks[6] = Maps.newHashMap();
        basicBlocks[6].put((byte) 63,
                getBlockDefinition(SIX_CONNECTIONS, blockBuilder, blockDefJson));

        BlockUri blockUri = new BlockUri(blockDefUri.getPackage(), blockDefUri.getAssetName());

        // Now make sure we have all combinations based on the basic set (above) and rotations
        for (byte connections = 0; connections < 64; connections++) {
            Block block = constructBlockForConnections(connections, blockBuilder, blockDefUri, basicBlocks);
            if (block == null)
                throw new IllegalStateException("Unable to find correct block definition for connections: " + connections);
            block.setUri(new BlockUri(blockUri, String.valueOf(connections)));
            blocksForConnections.put(connections, block);
        }

        final Block archetypeBlock = blocksForConnections.get(DirectionsUtil.getDirections(Direction.LEFT, Direction.RIGHT));
        return new ConnectToSixSidesFamily(connectionCondition, blockUri, blockDefinition.categories,
                archetypeBlock, blocksForConnections);
    }

    private Block constructBlockForConnections(byte connections, BlockBuilderHelper blockBuilder, AssetUri blockDefUri, Map<Byte, BlockDefinition>[] basicBlocks) {
        int connectionCount = DirectionsUtil.getDirections(connections).size();
        Map<Byte, BlockDefinition> possibleBlockDefinitions = basicBlocks[connectionCount];
        for (Map.Entry<Byte, BlockDefinition> connectionBlockDefinition : possibleBlockDefinitions.entrySet()) {
            byte originalConnections = connectionBlockDefinition.getKey();

            Matrix3f transformation = getTransformationToAchieve(originalConnections, connections);
            if (transformation != null) {
                return blockBuilder.constructTransformedBlock(blockDefUri, connectionBlockDefinition.getValue(), Rotation.constructTempRotation(transformation));
            }
        }
        return null;
    }

    private Matrix3f getTransformationToAchieve(byte source, byte target) {
        Collection<Direction> originalDirections = DirectionsUtil.getDirections(source);

        for (Matrix3f possibleTransformation : possibleTransformations) {
            Set<Direction> transformedDirections = Sets.newHashSet();
            for (Direction originalDirection : originalDirections) {
                Vector3f result = new Vector3f();
                possibleTransformation.transform(originalDirection.getVector3f(), result);
                transformedDirections.add(Direction.inDirection(result));
            }

            byte transformedDirection = DirectionsUtil.getDirections(transformedDirections);
            if (transformedDirection == target) {
                return possibleTransformation;
            }
        }
        return null;
    }

    private BlockDefinition getBlockDefinition(String definition, BlockBuilderHelper blockBuilder, JsonObject blockDefJson) {
        return blockBuilder.getBlockDefinitionForSection(blockDefJson, definition);
    }
}
