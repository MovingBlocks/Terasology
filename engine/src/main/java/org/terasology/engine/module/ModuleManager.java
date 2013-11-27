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
package org.terasology.engine.module;

import org.reflections.Reflections;

import java.util.List;
import java.util.Set;

/**
 * @author Immortius
 */
public interface ModuleManager {
    Reflections getActiveModuleReflections();

    void disableAllModules();

    void enableModule(Module module);

    void enableModuleAndDependencies(Module module);

    void disableModule(Module module);

    /**
     * Provides the ability to reflect over the engine and all modules, not just active modules.  This should be used sparingly,
     * and classes retrieved from it should not be instantiated and used - this uses a different classloader than the
     * rest of the system.
     *
     * @return Reflections over the engine and all available modules
     */
    Reflections loadInactiveReflections();

    /**
     * Rescans for modules.  This should not be done while a game is running, as it drops the module classloader.
     */
    void refresh();

    void applyActiveModules();

    List<Module> getModules();

    List<String> getModuleIds();

    List<Module> getCodeModules();

    Module getActiveModule(String id);

    Iterable<Module> getActiveModules();

    Module getLatestModuleVersion(String id);

    Module getLatestModuleVersion(String id, Version minVersion, Version maxVersion);

    Iterable<Module> getActiveCodeModules();

    boolean isEnabled(Module module);

    Iterable<Module> getAllDependencies(Module module);

    Set<String> getDependencyNamesOf(Module context);

    Module getModule(String moduleId, Version version);

}
