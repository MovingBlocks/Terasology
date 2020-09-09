// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.persistence.typeHandling.coreTypes;

import org.junit.jupiter.api.Test;
import org.terasology.engine.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.engine.persistence.typeHandling.inMemory.PersistedInteger;
import org.terasology.engine.persistence.typeHandling.inMemory.PersistedString;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CharacterTypeHandlerTest {
    CharacterTypeHandler typeHandler = new CharacterTypeHandler();

    @Test
    public void testSerialize() {
        PersistedDataSerializer serializer = mock(PersistedDataSerializer.class);

        char linefeedChar = '\n';

        typeHandler.serializeNonNull(linefeedChar, serializer);

        verify(serializer).serialize(eq("\n"));
    }

    @Test
    public void testDeserialize() {
        Optional<Character> deserializedLinefeed = typeHandler.deserialize(new PersistedString("\n"));

        assertTrue(deserializedLinefeed.isPresent());
        assertEquals('\n', (char) deserializedLinefeed.get());

        Optional<Character> deserializedInteger = typeHandler.deserialize(new PersistedInteger('\n'));

        assertFalse(deserializedInteger.isPresent());
    }
}
