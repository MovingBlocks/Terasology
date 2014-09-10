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

import com.google.common.collect.Lists;
import org.terasology.engine.GameEngine;
import org.terasology.engine.bootstrap.ApplyModulesUtil;
import org.terasology.engine.modes.StateMainMenu;
import org.terasology.engine.module.ModuleManager;
import org.terasology.game.GameManifest;
import org.terasology.module.DependencyResolver;
import org.terasology.module.ResolutionResult;
import org.terasology.naming.Name;
import org.terasology.naming.NameVersion;
import org.terasology.registry.CoreRegistry;

import java.util.List;

/**
 * @author Immortius
 */
public class RegisterMods extends SingleStepLoadProcess {

    private GameManifest gameManifest;

    public RegisterMods(GameManifest gameManifest) {
        this.gameManifest = gameManifest;
    }

    @Override
    public String getMessage() {
        return "Registering Mods...";
    }

    @Override
    public boolean step() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        List<Name> moduleIds = Lists.newArrayListWithCapacity(gameManifest.getModules().size());
        for (NameVersion moduleInfo : gameManifest.getModules()) {
            moduleIds.add(moduleInfo.getName());
        }

        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        ResolutionResult result = resolver.resolve(moduleIds);
        if (result.isSuccess()) {
            moduleManager.loadEnvironment(result.getModules(), true);
            ApplyModulesUtil.applyModules();
        } else {
            CoreRegistry.get(GameEngine.class).changeState(new StateMainMenu("Missing required module or dependency"));
        }
        return true;
    }

    @Override
    public int getExpectedCost() {
        return 1;
    }
}
