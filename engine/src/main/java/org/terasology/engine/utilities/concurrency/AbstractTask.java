// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.utilities.concurrency;

/**
 */
public abstract class AbstractTask implements Task {

    @Override
    public boolean isTerminateSignal() {
        return false;
    }
}
