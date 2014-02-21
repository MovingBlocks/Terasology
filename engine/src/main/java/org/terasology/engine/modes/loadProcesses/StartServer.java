/*
 * Copyright 2013 MovingBlocks
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

import org.terasology.engine.TerasologyConstants;
import org.terasology.network.NetworkSystem;
import org.terasology.network.exceptions.HostingFailedException;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.layers.mainMenu.ErrorMessagePopup;

/**
 * @author Immortius
 */
public class StartServer extends SingleStepLoadProcess {
    @Override
    public String getMessage() {
        return "Starting Server";
    }

    @Override
    public boolean step() {
        try {
            CoreRegistry.get(NetworkSystem.class).host(TerasologyConstants.DEFAULT_PORT);
        } catch (HostingFailedException e) {
            CoreRegistry.get(NUIManager.class).pushScreen("engine:errorMessagePopup", ErrorMessagePopup.class).setError("Failed to Host",
                    e.getMessage() + " - Reverting to single player");
        }
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
