/*
 * Copyright 2018 MovingBlocks
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
package org.terasology.persistence.typeHandling.coreTypes;

import org.junit.jupiter.api.Test;
import org.terasology.persistence.typeHandling.PersistedDataSerializer;
import org.terasology.persistence.typeHandling.inMemory.PersistedInteger;
import org.terasology.persistence.typeHandling.inMemory.PersistedString;

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

        Optional<Character> deserializedInteger = typeHandler.deserialize(new PersistedInteger((int) '\n'));

        assertFalse(deserializedInteger.isPresent());
    }
}
