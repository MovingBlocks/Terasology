// Copyright 2022 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.core.subsystem;

public abstract class NonPlayerVisibleSubsystem implements EngineSubsystem {
    /**
     * An unambiguous but messy name.
     * <p>
     * Good enough for test subsystems.
     */
    @Override
    public String getName() {
        return getClass().getCanonicalName();
    }
}
