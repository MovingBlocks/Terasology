// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.core.modes.loadProcesses;

import org.terasology.engine.context.Context;
import org.terasology.engine.core.modes.SingleStepLoadProcess;
import org.terasology.engine.persistence.StorageManager;

import java.io.IOException;

/**
 * Repairs the save game when it is in an inconsistent state after a crash.
 */
public class EnsureSaveGameConsistency extends SingleStepLoadProcess {
    private final Context context;

    public EnsureSaveGameConsistency(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "${engine:menu#ensuring-save-game-consistency}";
    }

    @Override
    public boolean step() {
        try {
            context.get(StorageManager.class).checkAndRepairSaveIfNecessary();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 0;
    }
}
