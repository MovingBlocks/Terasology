// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology;

/**
 * Junit5's lock keys for {@link org.junit.jupiter.api.parallel.ResourceLock}.
 * You should use {@link org.junit.jupiter.api.parallel.ResourceLock} for correct test parallelization.
 */
public final class TestResourceLocks {
    
    /**
     * Locks test/test-class for using {@link org.terasology.registry.CoreRegistry}
     */
    public static final String CORE_REGISTRY = "CoreRegistry";

    private TestResourceLocks() {
        // Util class not creates.
    }
}
