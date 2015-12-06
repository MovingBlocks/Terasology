/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.world.chunks.internal;

import com.google.common.base.Preconditions;
import com.google.protobuf.ByteString;
import gnu.trove.list.TByteList;
import gnu.trove.list.array.TByteArrayList;
import org.terasology.math.geom.Vector3i;
import org.terasology.protobuf.EntityData;
import org.terasology.world.biomes.BiomeManager;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.Chunk;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.blockdata.TeraArray;
import org.terasology.world.chunks.blockdata.TeraDenseArray16Bit;
import org.terasology.world.chunks.blockdata.TeraDenseArray8Bit;

/**
 */
public final class ChunkSerializer {

    private ChunkSerializer() {
    }

    public static EntityData.ChunkStore.Builder encode(Vector3i pos, TeraArray blockData, TeraArray liquidData, TeraArray biomeData) {
        final EntityData.ChunkStore.Builder b = EntityData.ChunkStore.newBuilder()
                .setX(pos.x).setY(pos.y).setZ(pos.z);
        b.setBlockData(runLengthEncode16(blockData));
        b.setLiquidData(runLengthEncode8(liquidData));
        b.setBiomeData(runLengthEncode16(biomeData));

        return b;
    }

    public static Chunk decode(EntityData.ChunkStore message, BlockManager blockManager, BiomeManager biomeManager) {
        Preconditions.checkNotNull(message, "The parameter 'message' must not be null");
        if (!message.hasX() || !message.hasY() || !message.hasZ()) {
            throw new IllegalArgumentException("Ill-formed protobuf message. Missing chunk position.");
        }
        Vector3i pos = new Vector3i(message.getX(), message.getY(), message.getZ());
        if (!message.hasBlockData()) {
            throw new IllegalArgumentException("Ill-formed protobuf message. Missing block data.");
        }
        if (!message.hasLiquidData()) {
            throw new IllegalArgumentException("Ill-formed protobuf message. Missing liquid data.");
        }

        final TeraArray blockData = runLengthDecode(message.getBlockData());
        final TeraArray liquidData = runLengthDecode(message.getLiquidData());
        final TeraArray biomeData = runLengthDecode(message.getBiomeData());
        return new ChunkImpl(pos, blockData, liquidData, biomeData, blockManager, biomeManager);
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

    private static EntityData.RunLengthEncoding8 runLengthEncode8(TeraArray array) {
        EntityData.RunLengthEncoding8.Builder builder = EntityData.RunLengthEncoding8.newBuilder();
        TByteList values = new TByteArrayList(16384);
        byte lastItem = (byte) array.get(0, 0, 0);
        int counter = 0;
        for (int y = 0; y < array.getSizeY(); ++y) {
            for (int z = 0; z < array.getSizeZ(); ++z) {
                for (int x = 0; x < array.getSizeX(); ++x) {
                    byte item = (byte) array.get(x, y, z);
                    if (lastItem != item) {
                        builder.addRunLengths(counter);
                        values.add(lastItem);
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
            values.add(lastItem);
        }
        builder.setValues(ByteString.copyFrom(values.toArray()));
        return builder.build();
    }

    private static TeraArray runLengthDecode(EntityData.RunLengthEncoding16 data) {
        Preconditions.checkState(data.getValuesCount() == data.getRunLengthsCount(), "Expected same number of values as runs");
        short[] decodedData = new short[ChunkConstants.SIZE_X * ChunkConstants.SIZE_Y * ChunkConstants.SIZE_Z];
        int index = 0;
        for (int pos = 0; pos < data.getValuesCount(); ++pos) {
            int length = data.getRunLengths(pos);
            short value = (short) data.getValues(pos);
            for (int i = 0; i < length; ++i) {
                decodedData[index++] = value;
            }
        }
        return new TeraDenseArray16Bit(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z, decodedData);
    }

    private static TeraArray runLengthDecode(EntityData.RunLengthEncoding8 data) {
        Preconditions.checkState(data.getValues().size() == data.getRunLengthsCount(), "Expected same number of values as runs");
        byte[] decodedData = new byte[ChunkConstants.SIZE_X * ChunkConstants.SIZE_Y * ChunkConstants.SIZE_Z];
        int index = 0;
        ByteString.ByteIterator valueSource = data.getValues().iterator();
        for (int pos = 0; pos < data.getRunLengthsCount(); ++pos) {
            int length = data.getRunLengths(pos);
            byte value = valueSource.nextByte();
            for (int i = 0; i < length; ++i) {
                decodedData[index++] = value;
            }
        }
        return new TeraDenseArray8Bit(ChunkConstants.SIZE_X, ChunkConstants.SIZE_Y, ChunkConstants.SIZE_Z, decodedData);
    }
}
