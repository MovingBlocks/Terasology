// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.world.chunks.internal;

import com.google.common.base.Preconditions;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.blockdata.ExtraBlockDataManager;
import org.terasology.engine.world.chunks.blockdata.TeraArray;
import org.terasology.engine.world.chunks.blockdata.TeraDenseArray16Bit;
import org.terasology.protobuf.EntityData;

public final class ChunkSerializer {

    private ChunkSerializer() {
    }

    public static EntityData.ChunkStore.Builder encode(Vector3ic pos, TeraArray blockData, TeraArray[] extraData) {
        final EntityData.ChunkStore.Builder b = EntityData.ChunkStore.newBuilder()
            .setX(pos.x()).setY(pos.y()).setZ(pos.z());
        b.setBlockData(runLengthEncode16(blockData));
        for (TeraArray extraDatum : extraData) {
            b.addExtraData(runLengthEncode16(extraDatum));
        }
        return b;
    }

    public static Chunk decode(EntityData.ChunkStore message, BlockManager blockManager, ExtraBlockDataManager extraDataManager) {
        Preconditions.checkNotNull(message, "The parameter 'message' must not be null");
        if (!message.hasX() || !message.hasY() || !message.hasZ()) {
            throw new IllegalArgumentException("Ill-formed protobuf message. Missing chunk position.");
        }
        Vector3i pos = new Vector3i(message.getX(), message.getY(), message.getZ());
        if (!message.hasBlockData()) {
            throw new IllegalArgumentException("Ill-formed protobuf message. Missing block data.");
        }

        final TeraArray blockData = runLengthDecode(message.getBlockData());
        final TeraArray[] extraData = extraDataManager.makeDataArrays(Chunks.SIZE_X, Chunks.SIZE_Y, Chunks.SIZE_Z);
        for (int i = 0; i < extraData.length; i++) {
            runLengthDecode(message.getExtraData(i), extraData[i]);
        }
        return new ChunkImpl(pos, blockData, extraData, blockManager);
    }

    private static EntityData.RunLengthEncoding16 runLengthEncode16(TeraArray array) {
        EntityData.RunLengthEncoding16.Builder builder = EntityData.RunLengthEncoding16.newBuilder();
        short lastItem = (short) array.get(0, 0, 0);
        int counter = 0;
        for (int y = 0; y < array.getSizeY(); ++y) {
            for (int z = 0; z < array.getSizeZ(); ++z) {
                for (int x = 0; x < array.getSizeX(); ++x) {
                    short item = (short) array.get(x, y, z);
                    if (lastItem != item) {
                        builder.addRunLengths(counter);
                        builder.addValues(lastItem & 0xFFFF);
                        lastItem = item;
                        counter = 1;
                    } else {
                        counter++;
                    }
                }
            }
        }
        if (lastItem != 0) {
            builder.addRunLengths(counter);
            builder.addValues(lastItem & 0xFFFF);
        }
        return builder.build();
    }

    private static TeraArray runLengthDecode(EntityData.RunLengthEncoding16 data) {
        Preconditions.checkState(data.getValuesCount() == data.getRunLengthsCount(), "Expected same number of values as runs");
        short[] decodedData = new short[Chunks.SIZE_X * Chunks.SIZE_Y * Chunks.SIZE_Z];
        int index = 0;
        for (int pos = 0; pos < data.getValuesCount(); ++pos) {
            int length = data.getRunLengths(pos);
            short value = (short) data.getValues(pos);
            for (int i = 0; i < length; ++i) {
                decodedData[index++] = value;
            }
        }
        return new TeraDenseArray16Bit(Chunks.SIZE_X, Chunks.SIZE_Y, Chunks.SIZE_Z, decodedData);
    }

    /**
     * Decode compressed data into an existing TeraArray.
     * Generic w.r.t. TeraArray subclasses, allowing the data to be used for any type of TeraArray.
     */
    private static void runLengthDecode(EntityData.RunLengthEncoding16 data, TeraArray array) {
        int index = 0;
        int count = 0;
        int value = 0;
        outer:
        for (int y = 0; y < array.getSizeY(); ++y) {
            for (int z = 0; z < array.getSizeZ(); ++z) {
                for (int x = 0; x < array.getSizeX(); ++x) {
                    if (count == 0) {
                        if (index >= data.getRunLengthsCount()) {
                            break outer;
                        }
                        count = data.getRunLengths(index);
                        value = data.getValues(index);
                        index++;
                    }
                    count--;
                    array.set(x, y, z, value);
                }
            }
        }
    }
}
