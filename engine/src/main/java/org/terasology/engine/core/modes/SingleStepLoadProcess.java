// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes;

public abstract class SingleStepLoadProcess implements LoadProcess {

    @Override
    public void begin() {
    }

    @Override
    public float getProgress() {
        return 0;
    }
}
