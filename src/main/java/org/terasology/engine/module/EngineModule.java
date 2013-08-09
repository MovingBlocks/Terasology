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
package org.terasology.engine.module;

import org.reflections.Reflections;

/**
 * @author Immortius
 */
public class EngineModule implements Module {

    private Reflections reflections;
    private ModuleInfo info;

    public EngineModule(Reflections reflections) {
        this.reflections = reflections;
        this.info = new ModuleInfo();
        this.info.setId("engine");
        this.info.setDisplayName("Engine");
        this.info.setDescription("The engine module");
    }

    @Override
    public Reflections getReflections() {
        return reflections;
    }

    @Override
    public boolean isCodeModule() {
        return true;
    }

    @Override
    public ModuleInfo getModuleInfo() {
        return info;
    }

}
