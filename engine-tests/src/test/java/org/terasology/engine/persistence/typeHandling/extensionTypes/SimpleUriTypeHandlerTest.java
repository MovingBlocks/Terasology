// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.persistence.typeHandling.extensionTypes;

import org.junit.jupiter.api.Test;
import org.terasology.engine.core.SimpleUri;
import org.terasology.persistence.typeHandling.PersistedData;
import org.terasology.persistence.typeHandling.inMemory.PersistedString;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleUriTypeHandlerTest {

    private final SimpleUriTypeHandler handler = new SimpleUriTypeHandler();

    @Test
    public void testDeserializeValidUri() {
        PersistedData data = new PersistedString("foo:bar");
        Optional<SimpleUri> uri = handler.deserialize(data);
        assertTrue(uri.isPresent());
        assertEquals(new SimpleUri("foo", "bar"), uri.get());
    }

    @Test
    public void testDeserializeInvalidUri() {
        PersistedData data = new PersistedString("baz");
        Optional<SimpleUri> uri = handler.deserialize(data);
        assertTrue(uri.isEmpty());
    }
}
