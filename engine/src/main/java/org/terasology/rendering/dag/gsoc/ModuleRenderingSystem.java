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
package org.terasology.rendering.dag.gsoc;

import org.terasology.context.Context;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.rendering.dag.api.RenderDagApiInterface;

public abstract class ModuleRenderingSystem extends BaseComponentSystem {

    @In
    protected Context context;
    protected Name providingModule;
    protected RenderDagApiInterface renderDagApi;

    @Override
    public void initialise() {
        super.initialise();
        renderDagApi = context.get(RenderDagApiInterface.class);

        /* Class implementingClass;
        ModuleManager moduleManager = context.get(ModuleManager.class);
        try {
            implementingClass = Class.forName(this.getClass().getName());
        } catch (Exception e) {
            throw new RuntimeException("Error in getting name of the class implementing ModuleRenderingSystem: " + e);
        }
        providingModule =  moduleManager.getEnvironment().getModuleProviding(implementingClass);
         */
    }

    protected void setProvidingModule(Class implementingClass) {
        ModuleManager moduleManager = context.get(ModuleManager.class);
        this.providingModule = moduleManager.getEnvironment().getModuleProviding(implementingClass);
    }

}
