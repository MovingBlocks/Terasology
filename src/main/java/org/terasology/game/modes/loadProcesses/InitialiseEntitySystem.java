/*
 * Copyright 2012  Benjamin Glatzel <benjamin.glatzel@me.com>
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

package org.terasology.game.modes.loadProcesses;

import org.terasology.game.CoreRegistry;
import org.terasology.game.bootstrap.EntitySystemBuilder;
import org.terasology.game.modes.LoadProcess;
import org.terasology.logic.mod.ModManager;

/**
 * @author Immortius
 */
public class InitialiseEntitySystem implements LoadProcess {
    @Override
    public String getMessage() {
        return "Initialising Entity System...";
    }

    @Override
    public boolean step() {
        ModManager modManager = CoreRegistry.get(ModManager.class);
        new EntitySystemBuilder().build(modManager);
        return true;
    }

    @Override
    public int begin() {
        return 1;
    }

}
