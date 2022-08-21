// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.extensionTypes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.BlockUriParseException;
import org.terasology.gestalt.assets.ResourceUrn;
import org.terasology.gestalt.naming.Name;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.inMemory.PersistedString;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockUriTypeHandlerTest {

    private final BlockUriTypeHandler handler = new BlockUriTypeHandler();

    @Test
    public void testDeserializeValidShortUri() {
        PersistedData data = new PersistedString("foo:bar");
        Optional<BlockUri> uri = handler.deserialize(data);
        assertTrue(uri.isPresent());
        assertEquals(new BlockUri(new ResourceUrn("foo", "bar")), uri.get());
    }

    @Test
    public void testDeserializeValidFullUri() {
        PersistedData data = new PersistedString("package:family:shapePackage:shapeName.identifier");
        Optional<BlockUri> uri = handler.deserialize(data);
        assertTrue(uri.isPresent());
        assertEquals(new BlockUri(
                new ResourceUrn("package", "family"), new ResourceUrn("shapePackage", "shapeName"), new Name("identifier")), uri.get());
    }

    @Test
    public void testDeserializeInvalidUri() {
        PersistedData data = new PersistedString("baz");
        BlockUriParseException thrown = Assertions.assertThrows(BlockUriParseException.class, () -> {
            handler.deserialize(data);
        });

        Assertions.assertEquals("Could not parse block uri: 'baz'", thrown.getMessage());
    }
}
