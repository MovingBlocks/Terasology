// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.mesh;

import org.junit.jupiter.api.Test;
import org.terasology.engine.rendering.assets.mesh.resource.IndexResource;

import java.nio.ByteBuffer;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IndexResourceTest {
    @Test
    public void testPutIndexResource() {
        IndexResource in = new IndexResource();
        in.put(10);
        in.put(11);
        in.put(15);

        assertEquals(3, in.indices());
        in.writeBuffer(buffer -> {
            assertEquals(3 * Integer.BYTES, buffer.limit());

            assertEquals(10, buffer.getInt(0));
            assertEquals(11, buffer.getInt(1 * Integer.BYTES));
            assertEquals(15, buffer.getInt(2 * Integer.BYTES));
        });
    }

    @Test
    public void testRewind() {
        IndexResource in = new IndexResource();
        in.put(10);
        in.put(11);
        in.put(15);

        assertEquals(3, in.indices());
        in.writeBuffer(buffer -> {
            assertEquals(3 * Integer.BYTES, buffer.limit());

            assertEquals(10, buffer.getInt(0));
            assertEquals(11, buffer.getInt(1 * Integer.BYTES));
            assertEquals(15, buffer.getInt(2 * Integer.BYTES));
        });

        in.rewind();
        in.put(20);
        assertEquals(3, in.indices());
        in.writeBuffer(buffer -> {
            assertEquals(3 * Integer.BYTES, buffer.limit());

            assertEquals(20, buffer.getInt(0));
            assertEquals(11, buffer.getInt(1 * Integer.BYTES));
            assertEquals(15, buffer.getInt(2 * Integer.BYTES));
        });

    }

    @Test
    public void testAllocate() {

        IndexResource in = new IndexResource();
        in.allocateElements(20); // re-allocate to 20 indices

        in.put(100);
        in.put(14);
        in.put(10);

        assertEquals(20, in.indices());
        in.writeBuffer(buffer -> {
            assertEquals(20 * Integer.BYTES, buffer.limit());

            assertEquals(100, buffer.getInt(0));
            assertEquals(14, buffer.getInt(1 * Integer.BYTES));
            assertEquals(10, buffer.getInt(2 * Integer.BYTES));
        });

        in.allocateElements(4); // re-allocate to 4 indices
        assertEquals(4, in.indices());
        in.writeBuffer(buffer -> {
            assertEquals(4 * Integer.BYTES, buffer.limit());

            assertEquals(100, buffer.getInt(0));
            assertEquals(14, buffer.getInt(1 * Integer.BYTES));
            assertEquals(10, buffer.getInt(2 * Integer.BYTES));
        });
    }
}
