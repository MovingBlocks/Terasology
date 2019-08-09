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
package org.terasology.testUtil;

import com.google.common.collect.Sets;
import org.mockito.Mockito;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.module.ModuleManager;
import org.terasology.engine.module.ModuleManagerImpl;
import org.terasology.module.ClasspathModule;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataReader;
import org.terasology.naming.Name;
import org.terasology.reflection.internal.TypeRegistryImpl;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Set;

/**
 */
public final class ModuleManagerFactory {
    private ModuleManagerFactory() {
    }

    // Named "create" for legacy reasons, but does more than create
    public static ModuleManager create() throws Exception {
        ModuleManager manager = create(Mockito.mock(TypeRegistryImpl.class));
        manager.loadEnvironment(Sets.newHashSet(manager.getRegistry().getLatestModuleVersion(new Name("engine"))), true);
        return manager;
    }

    /**
     * Creates a new {@link ModuleManager} instance, but does not
     * {@link ModuleManager#loadEnvironment(Set, boolean) load it's environment}.
     */
    public static ModuleManager create(TypeRegistryImpl typeRegistry) throws Exception {
        ModuleManager moduleManager = new ModuleManagerImpl("", typeRegistry);
        try (Reader reader = new InputStreamReader(ModuleManagerFactory.class.getResourceAsStream("/module.txt"), TerasologyConstants.CHARSET)) {
            ModuleMetadata metadata = new ModuleMetadataReader().read(reader);
            moduleManager.getRegistry().add(ClasspathModule.create(metadata, ModuleManagerFactory.class));
        }
        return moduleManager;
    }
}
