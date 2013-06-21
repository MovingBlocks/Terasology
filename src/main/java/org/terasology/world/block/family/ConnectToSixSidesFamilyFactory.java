package org.terasology.world.block.family;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import gnu.trove.iterator.TByteObjectIterator;
import gnu.trove.map.TByteObjectMap;
import gnu.trove.map.hash.TByteObjectHashMap;
import gnu.trove.procedure.TByteObjectProcedure;
import org.terasology.asset.AssetUri;
import org.terasology.math.*;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.loader.BlockDefinition;

import javax.vecmath.Matrix3f;
import javax.vecmath.Vector3f;
import java.util.Collection;
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
        TByteObjectMap<BlockDefinition>[] basicBlocks = new TByteObjectMap[7];
        TByteObjectMap<Block> blocksForConnections = new TByteObjectHashMap<Block>();

        basicBlocks[0] = new TByteObjectHashMap<BlockDefinition>();
        basicBlocks[0].put((byte) 0,
                getBlockDefinition(NO_CONNECTIONS, blockBuilder, blockDefJson));

        basicBlocks[1] = new TByteObjectHashMap<BlockDefinition>();
        basicBlocks[1].put(Sides.getSides(Side.BACK),
                getBlockDefinition(ONE_CONNECTION, blockBuilder, blockDefJson));

        basicBlocks[2] = new TByteObjectHashMap<BlockDefinition>();
        basicBlocks[2].put(Sides.getSides(Side.BACK, Side.FRONT),
                getBlockDefinition(TWO_CONNECTIONS_LINE, blockBuilder, blockDefJson));
        basicBlocks[2].put(Sides.getSides(Side.LEFT, Side.BACK),
                getBlockDefinition(TWO_CONNECTIONS_CORNER, blockBuilder, blockDefJson));

        basicBlocks[3] = new TByteObjectHashMap<BlockDefinition>();
        basicBlocks[3].put(Sides.getSides(Side.LEFT, Side.BACK, Side.TOP),
                getBlockDefinition(THREE_CONNECTIONS_CORNER, blockBuilder, blockDefJson));
        basicBlocks[3].put(Sides.getSides(Side.LEFT, Side.BACK, Side.FRONT),
                getBlockDefinition(THREE_CONNECTIONS_T, blockBuilder, blockDefJson));

        basicBlocks[4] = new TByteObjectHashMap<BlockDefinition>();
        basicBlocks[4].put(Sides.getSides(Side.RIGHT, Side.LEFT, Side.BACK, Side.FRONT),
                getBlockDefinition(FOUR_CONNECTIONS_CROSS, blockBuilder, blockDefJson));
        basicBlocks[4].put(Sides.getSides(Side.LEFT, Side.BACK, Side.FRONT, Side.TOP),
                getBlockDefinition(FOUR_CONNECTIONS_SIDE, blockBuilder, blockDefJson));

        basicBlocks[5] = new TByteObjectHashMap<BlockDefinition>();
        basicBlocks[5].put(Sides.getSides(Side.LEFT, Side.BACK, Side.FRONT, Side.TOP, Side.BOTTOM),
                getBlockDefinition(FIVE_CONNECTIONS, blockBuilder, blockDefJson));

        basicBlocks[6] = new TByteObjectHashMap<BlockDefinition>();
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

        final Block archetypeBlock = blocksForConnections.get(Sides.getSides(Side.RIGHT, Side.LEFT));
        return new ConnectToSixSidesFamily(connectionCondition, blockUri, blockDefinition.categories,
                archetypeBlock, blocksForConnections);
    }

    private Block constructBlockForConnections(final byte connections, final BlockBuilderHelper blockBuilder, final AssetUri blockDefUri, TByteObjectMap<BlockDefinition>[] basicBlocks) {
        int connectionCount = Sides.getSides(connections).size();
        TByteObjectMap<BlockDefinition> possibleBlockDefinitions = basicBlocks[connectionCount];
        final TByteObjectIterator<BlockDefinition> blockDefinitionIterator = possibleBlockDefinitions.iterator();
        while (blockDefinitionIterator.hasNext()) {
            final byte originalConnections = blockDefinitionIterator.key();
            final BlockDefinition blockDefinition = blockDefinitionIterator.value();
            Matrix3f transformation = getTransformationToAchieve(originalConnections, connections);
            if (transformation != null) {
                return blockBuilder.constructTransformedBlock(blockDefUri, blockDefinition, Rotation.constructTempRotation(transformation));
            }
        }
        return null;
    }

    private Matrix3f getTransformationToAchieve(byte source, byte target) {
        Collection<Side> originalSides = Sides.getSides(source);

        for (Matrix3f possibleTransformation : possibleTransformations) {
            Set<Side> transformedSides = Sets.newHashSet();
            for (Side originalSide : originalSides) {
                Vector3f result = new Vector3f();
                possibleTransformation.transform(originalSide.getVector3i().toVector3f(), result);
                transformedSides.add(Side.inDirection(result));
            }

            byte transformedSide = Sides.getSides(transformedSides);
            if (transformedSide == target) {
                return possibleTransformation;
            }
        }
        return null;
    }

    private BlockDefinition getBlockDefinition(String definition, BlockBuilderHelper blockBuilder, JsonObject blockDefJson) {
        return blockBuilder.getBlockDefinitionForSection(blockDefJson, definition);
    }
}
