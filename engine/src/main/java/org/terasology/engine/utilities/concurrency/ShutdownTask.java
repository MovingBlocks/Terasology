// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.utilities.concurrency;

/**
 */
public class ShutdownTask implements Task {
    @Override
    public String getName() {
        return "Shutdown";
    }

    @Override
    public void run() {
    }

    @Override
    public boolean isTerminateSignal() {
        return true;
    }
}
