// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.modes.loadProcesses;

import org.terasology.engine.modes.ExpectedCost;
import org.terasology.engine.modes.SingleStepLoadProcess;
import org.terasology.persistence.StorageManager;
import org.terasology.registry.In;

import java.io.IOException;

/**
 * Repairs the save game when it is in an inconsistent state after a crash.
 */
@ExpectedCost(0)
public class EnsureSaveGameConsistency extends SingleStepLoadProcess {

    @In
    private StorageManager storageManager;

    @Override
    public String getMessage() {
        return "${engine:menu#ensuring-save-game-consistency}";
    }

    @Override
    public boolean step() {
        try {
            storageManager.checkAndRepairSaveIfNecessary();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }
}
