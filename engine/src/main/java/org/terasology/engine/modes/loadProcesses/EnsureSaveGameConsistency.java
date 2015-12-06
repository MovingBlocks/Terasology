/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.engine.modes.loadProcesses;

import org.terasology.context.Context;
import org.terasology.engine.modes.LoadProcess;
import org.terasology.persistence.StorageManager;

import java.io.IOException;

/**
 * Repairs the save game when it is in an inconsistent state after a crash.
 *
 */
public class EnsureSaveGameConsistency implements LoadProcess {
    private final Context context;

    public EnsureSaveGameConsistency(Context context) {
        this.context = context;
    }

    @Override
    public String getMessage() {
        return "Ensuring save game consistency";
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
    public void begin() {
    }

    @Override
    public float getProgress() {
        return 0;
    }

    @Override
    public int getExpectedCost() {
        return 0;
    }
}
