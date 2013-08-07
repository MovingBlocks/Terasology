/*
 * Copyright 2013 Moving Blocks
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
package org.terasology.world.generator;

import com.google.common.collect.ImmutableList;
import org.terasology.engine.CoreRegistry;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;

import java.util.List;

/**
 * @author Immortius
 */
public class WorldGeneratorManager {

    private ImmutableList<WorldGeneratorInfo> generatorInfo;

    public void refresh() {
        ModuleManager moduleManager = CoreRegistry.get(ModuleManager.class);
        CoreRegistry.get(ModuleManager.class).getAllReflections();
        for (Module module : moduleManager.getModules()) {

        }
    }

    public List<WorldGeneratorInfo> getWorldGenerators() {
        return null;
    }

    public WorldGenerator createGenerator(WorldGeneratorUri uri) {
        return null;
    }

}
