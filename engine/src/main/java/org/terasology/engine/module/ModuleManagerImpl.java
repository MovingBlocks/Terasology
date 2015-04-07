/*
 * Copyright 2014 MovingBlocks
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

import com.google.common.collect.Sets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.paths.PathManager;
import org.terasology.module.ClasspathModule;
import org.terasology.module.DependencyInfo;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ModuleLoader;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataReader;
import org.terasology.module.ModulePathScanner;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.TableModuleRegistry;
import org.terasology.module.sandbox.APIScanner;
import org.terasology.module.sandbox.BytecodeInjector;
import org.terasology.module.sandbox.ModuleSecurityManager;
import org.terasology.module.sandbox.ModuleSecurityPolicy;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ReflectPermission;
import java.net.URISyntaxException;
import java.security.Policy;
import java.util.Collections;
import java.util.Set;

/**
 * @author Immortius
 */
public class ModuleManagerImpl implements ModuleManager {

    private ModuleSecurityManager moduleSecurityManager;

    private ModuleRegistry registry;
    private ModuleEnvironment environment;
    private ModuleMetadataReader metadataReader;

    public ModuleManagerImpl() {
        metadataReader = new ModuleMetadataReader();
        for (ModuleExtension ext : StandardModuleExtension.values()) {
            metadataReader.registerExtension(ext.getKey(), ext.getValueType());
        }
        Module engineModule;
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/engine-module.txt"), TerasologyConstants.CHARSET)) {
            ModuleMetadata metadata = metadataReader.read(reader);
            engineModule = ClasspathModule.create(metadata, getClass(), Module.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read engine metadata", e);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Failed to convert engine library location to path", e);
        }

        registry = new TableModuleRegistry();
        registry.add(engineModule);
        ModulePathScanner scanner = new ModulePathScanner(new ModuleLoader(metadataReader));
        scanner.getModuleLoader().setModuleInfoPath(TerasologyConstants.MODULE_INFO_FILENAME);
        scanner.scan(registry, PathManager.getInstance().getModulePaths());

        DependencyInfo engineDep = new DependencyInfo();
        engineDep.setId(engineModule.getId());
        engineDep.setMinVersion(engineModule.getVersion());
        engineDep.setMaxVersion(engineModule.getVersion().getNextPatchVersion());

        for (Module mod : registry) {
            if (mod != engineModule) {
                mod.getMetadata().getDependencies().add(engineDep);
            }
        }

        setupSandbox();
        loadEnvironment(Sets.newHashSet(engineModule), true);
    }

    private void setupSandbox() {
        moduleSecurityManager = new ModuleSecurityManager();
        // TODO: This one org.terasology entry is a hack and needs a proper fix
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("org.terasology.world.biomes");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("org.terasology.math.geom");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("java.lang");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("java.lang.ref");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("java.math");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("java.util");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("java.util.concurrent");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("java.util.concurrent.atomic");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("java.util.concurrent.locks");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("java.util.regex");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("java.awt");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("java.awt.geom");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("java.awt.image");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("com.google.common.annotations");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("com.google.common.cache");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("com.google.common.collect");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("com.google.common.base");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("com.google.common.math");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("com.google.common.primitives");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("com.google.common.util.concurrent");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.decorator");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.function");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.iterator");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.iterator.hash");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.list");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.list.array");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.list.linked");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.map");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.map.hash");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.map.custom_hash");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.procedure");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.procedure.array");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.queue");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.set");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.set.hash");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.stack");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.stack.array");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("gnu.trove.strategy");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("javax.vecmath");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("com.yourkit.runtime");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("com.bulletphysics.linearmath");
        moduleSecurityManager.getBasePermissionSet().addAPIPackage("sun.reflect");
        moduleSecurityManager.getBasePermissionSet().addAPIClass(com.esotericsoftware.reflectasm.MethodAccess.class);
        moduleSecurityManager.getBasePermissionSet().addAPIClass(IOException.class);
        moduleSecurityManager.getBasePermissionSet().addAPIClass(InvocationTargetException.class);
        moduleSecurityManager.getBasePermissionSet().addAPIClass(LoggerFactory.class);
        moduleSecurityManager.getBasePermissionSet().addAPIClass(Logger.class);

        APIScanner apiScanner = new APIScanner(moduleSecurityManager);
        for (Module module : registry) {
            if (module.isOnClasspath()) {
                apiScanner.scan(module);
            }
        }

        moduleSecurityManager.getBasePermissionSet().grantPermission("com.google.gson", ReflectPermission.class);
        moduleSecurityManager.getBasePermissionSet().grantPermission("com.google.gson.internal", ReflectPermission.class);

        moduleSecurityManager.getBasePermissionSet().addAPIClass(java.nio.ByteBuffer.class);
        moduleSecurityManager.getBasePermissionSet().addAPIClass(java.nio.IntBuffer.class);

        Policy.setPolicy(new ModuleSecurityPolicy());
        System.setSecurityManager(moduleSecurityManager);
    }

    @Override
    public ModuleRegistry getRegistry() {
        return registry;
    }

    @Override
    public ModuleEnvironment getEnvironment() {
        return environment;
    }

    @Override
    public ModuleEnvironment loadEnvironment(Set<Module> modules, boolean asPrimary) {
        Set<Module> finalModules = Sets.newLinkedHashSet(modules);
        for (Module module : registry) {
            if (module.isOnClasspath()) {
                finalModules.add(module);
            }
        }
        ModuleEnvironment newEnvironment = new ModuleEnvironment(finalModules, moduleSecurityManager, Collections.<BytecodeInjector>emptyList());
        if (asPrimary) {
            if (environment != null) {
                environment.close();
            }
            environment = newEnvironment;
        }
        return newEnvironment;
    }

    @Override
    public ModuleMetadataReader getModuleMetadataReader() {
        return metadataReader;
    }
}
