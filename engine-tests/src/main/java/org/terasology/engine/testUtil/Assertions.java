// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.testUtil;

import com.google.common.collect.Iterables;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.fail;

public class Assertions {
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static void assertNotEmpty(Optional<?> obj) {
        if (!obj.isPresent()) {
            fail("Empty result.");
        }
    }

    public static void assertNotEmpty(Iterable<?> obj) {
        if (Iterables.isEmpty(obj)) {
            fail("Empty result.");
        }
    }
}
