// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.persistence.typeHandling;

import org.junit.jupiter.api.Test;
import org.terasology.persistence.typeHandling.inMemory.PersistedInteger;
import org.terasology.persistence.typeHandling.inMemory.PersistedString;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringRepresentationTypeHandlerTest {

    private final RecordingHandler typeHandler = new RecordingHandler();

    @Test
    void deserializesStringContent() {
        Optional<String> result = typeHandler.deserialize(new PersistedString("engine:something"));

        assertTrue(result.isPresent());
        assertEquals("engine:something", result.get());
        assertTrue(typeHandler.getFromStringCalled);
    }

    @Test
    void treatsNullStringContentAsAbsent() {
        // PersistedString returns true from isString() unconditionally, so a null payload reaches
        // deserialize() with isString() == true. That is the shape that produced the NPE: null was
        // forwarded to getFromString(), and ComponentClassTypeHandler built a SimpleUri from it.
        Optional<String> result = typeHandler.deserialize(new PersistedString(null));

        assertFalse(result.isPresent());
        assertFalse(typeHandler.getFromStringCalled, "null content must not reach getFromString()");
    }

    @Test
    void ignoresNonStringData() {
        Optional<String> result = typeHandler.deserialize(new PersistedInteger(42));

        assertFalse(result.isPresent());
        assertFalse(typeHandler.getFromStringCalled);
    }

    /**
     * Records whether {@link #getFromString} was reached. The contract under test is not just
     * "returns empty" but "never forwards null to the subclass" — asserting only on the returned
     * Optional would still pass if a subclass happened to tolerate null and return null itself.
     */
    private static final class RecordingHandler extends StringRepresentationTypeHandler<String> {
        private boolean getFromStringCalled;

        @Override
        public String getAsString(String item) {
            return item;
        }

        @Override
        public String getFromString(String representation) {
            getFromStringCalled = true;
            return representation;
        }
    }
}
