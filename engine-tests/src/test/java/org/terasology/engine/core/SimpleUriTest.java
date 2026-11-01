// Copyright 2026 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core;

import org.junit.jupiter.api.Test;
import org.terasology.gestalt.naming.Name;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleUriTest {

    @Test
    void parsesModuleAndObject() {
        SimpleUri uri = new SimpleUri("engine:something");

        assertTrue(uri.isValid());
        assertEquals(new Name("engine"), uri.getModuleName());
        assertEquals(new Name("something"), uri.getObjectName());
    }

    @Test
    void stringWithoutSeparatorIsInvalid() {
        assertFalse(new SimpleUri("engine").isValid());
    }

    @Test
    void nullStringIsInvalid() {
        // Regression: null reached this constructor when deserializing a stale reference and
        // threw on split(). It must land in the same empty/invalid state as the no-arg form.
        SimpleUri uri = new SimpleUri((String) null);

        assertFalse(uri.isValid());
        assertTrue(uri.getModuleName().isEmpty());
        assertTrue(uri.getObjectName().isEmpty());
    }
}
