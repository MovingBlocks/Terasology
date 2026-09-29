// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.logic.behavior.core;

import com.google.gson.JsonParseException;
import org.junit.jupiter.api.Test;
import org.terasology.engine.logic.behavior.actions.InvertAction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression coverage for https://github.com/MovingBlocks/Terasology/issues/5099 - a null child (stray comma,
 * or missing decorator child) must fail fast at load time, not crash later with a stray NPE.
 */
public class BehaviorTreeBuilderTest {

    private final BehaviorTreeBuilder builder = new BehaviorTreeBuilder();

    @Test
    public void validTreeStillParses() {
        BehaviorNode node = builder.fromJson("{ selector: [success, failure, success] }");

        assertEquals(3, node.getChildrenCount());
    }

    @Test
    public void nullChildInCompositeArrayFailsLoudlyInsteadOfLater() {
        JsonParseException exception = assertThrows(JsonParseException.class,
                () -> builder.fromJson("{ selector: [success, null, success] }"));

        assertTrue(exception.getMessage().contains("selector"));
        assertTrue(exception.getMessage().contains("index 1"));
    }

    @Test
    public void nullChildInDecoratorFailsLoudlyInsteadOfLater() {
        builder.registerDecorator("invert", InvertAction.class);

        JsonParseException exception = assertThrows(JsonParseException.class,
                () -> builder.fromJson("{ invert: { child: null } }"));

        assertTrue(exception.getMessage().contains("invert"));
    }

    @Test
    public void missingChildInDecoratorFailsLoudlyInsteadOfLater() {
        builder.registerDecorator("invert", InvertAction.class);

        JsonParseException exception = assertThrows(JsonParseException.class,
                () -> builder.fromJson("{ invert: {} }"));

        assertTrue(exception.getMessage().contains("invert"));
    }
}
