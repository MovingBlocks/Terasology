/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.engine.module;

import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.module.sandbox.API;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.registry.Share;


@Share(ModuleEnvironmentInfo.class)
@API
@RegisterSystem
public class ModuleEnvironmentInfoImpl extends BaseComponentSystem implements ModuleEnvironmentInfo {
    @In
    private ModuleManager moduleManager;

    @Override
    public boolean moduleLoaded(String name) {
        return moduleLoaded(new Name(name));
    }

    @Override
    public boolean moduleLoaded(Name name) {
        return moduleManager.getEnvironment().get(name) != null;
    }
}
