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

import org.terasology.persistence.StorageManager;
import org.terasology.registry.CoreRegistry;

import java.io.IOException;

/**
 * Loads necessary data for the entity manager to work proprly from the global store.
 *
 * This step needs to run before the systems get initialized.
 *
 * @author Florian <florian@fkoeberle.de>
 */
public class LoadDataForEntitySystem extends SingleStepLoadProcess{

    @Override
    public String getMessage() {
        return "Loading data for entity system";
    }

    @Override
    public boolean step() {
        try {
            CoreRegistry.get(StorageManager.class).loadManagerDataFromGlobalStore();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
