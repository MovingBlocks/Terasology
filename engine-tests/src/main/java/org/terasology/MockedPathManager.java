// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Interface for using in junit jupiter tests(Junit 5).
 * <p>
 * Just add this interface to your test class
 */
public interface MockedPathManager {

    @BeforeEach
    default void setupPathManager() {
        PathManagerMocker.mockPathManager();
    }
    
    @AfterEach
    default void clearPathManager() {
        PathManagerMocker.resetPathManager();
    }
}
