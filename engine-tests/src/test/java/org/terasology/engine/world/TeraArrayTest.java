// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.world;

import com.google.common.collect.Streams;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.terasology.engine.world.chunks.blockdata.TeraArray;
import org.terasology.engine.world.chunks.blockdata.TeraDenseArray16Bit;
import org.terasology.engine.world.chunks.blockdata.TeraDenseArray4Bit;
import org.terasology.engine.world.chunks.blockdata.TeraDenseArray8Bit;
import org.terasology.engine.world.chunks.blockdata.TeraOcTree;
import org.terasology.engine.world.chunks.blockdata.TeraSparseArray16Bit;
import org.terasology.engine.world.chunks.blockdata.TeraSparseArray4Bit;
import org.terasology.engine.world.chunks.blockdata.TeraSparseArray8Bit;

import java.nio.ByteBuffer;
import java.util.Random;
import java.util.stream.Stream;

public class TeraArrayTest {

    public static Stream<Arguments> arrays() {
        return Stream.of(
                new TeraOcTree((byte) 32),
                new TeraDenseArray4Bit(32, 32, 32),
                new TeraDenseArray8Bit(32, 32, 32),
                new TeraDenseArray16Bit(32, 32, 32),
                new TeraSparseArray4Bit(32, 32, 32),
                new TeraSparseArray8Bit(32, 32, 32),
                new TeraSparseArray16Bit(32, 32, 32)
        ).map(Arguments::of);
    }

    public static Stream<Arguments> serializers() {
        return Stream.of(
                new TeraOcTree.SerializationHandler(),
                new TeraDenseArray4Bit.SerializationHandler(),
                new TeraDenseArray8Bit.SerializationHandler(),
                new TeraDenseArray16Bit.SerializationHandler(),
                new TeraSparseArray4Bit.SerializationHandler(),
                new TeraSparseArray8Bit.SerializationHandler(),
                new TeraSparseArray16Bit.SerializationHandler()
        ).map(Arguments::of);
    }

    public static Stream<Arguments> arrayAndSerializers() {
        return Streams.zip(arrays(), serializers(), (a, s) -> Arguments.arguments(a.get()[0], s.get()[0]));
    }

    @MethodSource("arrays")
    @ParameterizedTest
    public void writeRead(TeraArray array) {

        Assertions.assertEquals(0, array.set(5, 31, 1, 1));
        int i = array.get(5, 31, 1);
        Assertions.assertEquals(1, i);
    }

    @MethodSource("arrayAndSerializers")
    @ParameterizedTest
    public void buffer(TeraArray array, TeraArray.SerializationHandler serializationHandler) {
        Random random = new Random();
        for (int i = 0; i < random.nextInt(40); i++) {
            array.set(random.nextInt(32),
                    random.nextInt(32),
                    random.nextInt(32),
                    random.nextInt(32)
            );
        }
        ByteBuffer buffer = serializationHandler.serialize(array);
        buffer.rewind();
        TeraArray deserializedArray = serializationHandler.deserialize(buffer);
        // At least this code is not fall.
    }

}
