// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes;

/**
 * Used for load processes using varied amount of steps dependant on something else (e.g. waiting for external action)
 */
public abstract class VariableStepLoadProcess implements LoadProcess {
    @Override
    public float getProgress() {
        return 0;
    }
}
