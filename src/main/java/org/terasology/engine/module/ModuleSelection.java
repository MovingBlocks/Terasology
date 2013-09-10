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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.engine.TerasologyConstants;

import java.util.List;
import java.util.Map;

/**
 * @author Immortius
 */
public class ModuleSelection {

    private final ModuleManager moduleManager;
    private boolean valid = true;
    private Map<String, Module> modulesById = Maps.newLinkedHashMap();
    private List<String> validationMessages = Lists.newArrayList();

    public ModuleSelection(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
        modulesById.put(TerasologyConstants.ENGINE_MODULE, moduleManager.getLatestModuleVersion(TerasologyConstants.ENGINE_MODULE));
    }

    public ModuleSelection(ModuleSelection original) {
        this.moduleManager = original.moduleManager;
        this.valid = original.valid;
        this.modulesById.putAll(original.modulesById);
        this.validationMessages.addAll(original.validationMessages);
    }

    public boolean isValid() {
        return valid;
    }

    public List<Module> getSelection() {
        return Lists.newArrayList(modulesById.values());
    }

    public ModuleSelection add(String moduleId) {
        Module module = moduleManager.getLatestModuleVersion(moduleId);
        if (module != null) {
            return add(module);
        } else {
            ModuleSelection result = new ModuleSelection(this);
            result.valid = false;
            result.validationMessages.add("Unable to resolve module '" + moduleId + "'");
            return result;
        }
    }

    public ModuleSelection add(Module module) {
        ModuleSelection result = new ModuleSelection(this);
        for (DependencyInfo dependency : module.getModuleInfo().getDependencies()) {
            Module existing = modulesById.get(UriUtil.normalise(dependency.getId()));
            if (existing != null) {
                if (dependency.getMinVersion().compareTo(existing.getVersion()) > 0 || dependency.getMaxVersion().compareTo(existing.getVersion()) <= 0) {
                    result.valid = false;
                    result.validationMessages.add("'" + module.toString() + "' incompatible with '" + existing.toString() + "'");
                }
            } else {
                Module newDependency = moduleManager.getLatestModuleVersion(dependency.getId(), dependency.getMinVersion(), dependency.getMaxVersion());
                if (newDependency != null) {
                    result = result.add(newDependency);
                } else {
                    result.valid = false;
                    result.validationMessages.add("Missing dependency '" + dependency.getId() + ":[" + dependency.getMinVersion() + "-" + dependency.getMaxVersion() + ")'");
                }
            }
        }
        if (result.isValid()) {
            result.modulesById.put(UriUtil.normalise(module.getId()), module);
        }
        return result;
    }

    public List<String> getValidationMessages() {
        return validationMessages;
    }

    public boolean contains(String id) {
        return modulesById.containsKey(UriUtil.normalise(id));
    }

    public ModuleSelection remove(String id) {
        Module removeModule = modulesById.get(UriUtil.normalise(id));
        if (removeModule != null) {
            ModuleSelection result = new ModuleSelection(moduleManager);
            for (Module module : modulesById.values()) {
                if (module != removeModule) {
                    ModuleSelection newResult = result.add(module);
                    if (newResult.isValid() && !newResult.contains(id)) {
                        result = newResult;
                    }
                }
            }
            return result;
        }
        return this;
    }
}
