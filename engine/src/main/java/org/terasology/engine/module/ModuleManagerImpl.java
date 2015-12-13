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
import org.terasology.assets.Asset;
import org.terasology.engine.TerasologyConstants;
import org.terasology.engine.paths.PathManager;
import org.terasology.module.ClasspathModule;
import org.terasology.module.DependencyInfo;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ModuleLoader;
import org.terasology.module.ModuleMetadata;
import org.terasology.module.ModuleMetadataJsonAdapter;
import org.terasology.module.ModulePathScanner;
import org.terasology.module.ModuleRegistry;
import org.terasology.module.TableModuleRegistry;
import org.terasology.module.sandbox.APIScanner;
import org.terasology.module.sandbox.BytecodeInjector;
import org.terasology.module.sandbox.ModuleSecurityManager;
import org.terasology.module.sandbox.ModuleSecurityPolicy;
import org.terasology.module.sandbox.StandardPermissionProviderFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ReflectPermission;
import java.net.URISyntaxException;
import java.security.Policy;
import java.util.Collections;
import java.util.Set;

/**
 */
public class ModuleManagerImpl implements ModuleManager {

    private StandardPermissionProviderFactory permissionProviderFactory = new StandardPermissionProviderFactory();

    private ModuleRegistry registry;
    private ModuleEnvironment environment;
    private ModuleMetadataJsonAdapter metadataReader;

    public ModuleManagerImpl() {
        metadataReader = new ModuleMetadataJsonAdapter();
        for (ModuleExtension ext : StandardModuleExtension.values()) {
            metadataReader.registerExtension(ext.getKey(), ext.getValueType());
        }
        Module engineModule;
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/engine-module.txt"), TerasologyConstants.CHARSET)) {
            ModuleMetadata metadata = metadataReader.read(reader);
            engineModule = ClasspathModule.create(metadata, getClass(), Module.class, Asset.class);
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
        // TODO: This one org.terasology entry is a hack and needs a proper fix
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("org.terasology.world.biomes");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("org.terasology.math.geom");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("java.lang");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("java.lang.invoke");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("java.lang.ref");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("java.math");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("java.util");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("java.util.concurrent");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("java.util.concurrent.atomic");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("java.util.concurrent.locks");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("java.util.function");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("java.util.regex");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("java.util.stream");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("java.awt");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("java.awt.geom");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("java.awt.image");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("com.google.common.annotations");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("com.google.common.cache");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("com.google.common.collect");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("com.google.common.base");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("com.google.common.math");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("com.google.common.primitives");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("com.google.common.util.concurrent");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.decorator");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.function");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.iterator");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.iterator.hash");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.list");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.list.array");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.list.linked");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.map");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.map.hash");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.map.custom_hash");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.procedure");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.procedure.array");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.queue");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.set");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.set.hash");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.stack");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.stack.array");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("gnu.trove.strategy");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("javax.vecmath");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("com.yourkit.runtime");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("com.bulletphysics.linearmath");
        permissionProviderFactory.getBasePermissionSet().addAPIPackage("sun.reflect");
        permissionProviderFactory.getBasePermissionSet().addAPIClass(com.esotericsoftware.reflectasm.MethodAccess.class);
        permissionProviderFactory.getBasePermissionSet().addAPIClass(IOException.class);
        permissionProviderFactory.getBasePermissionSet().addAPIClass(InvocationTargetException.class);
        permissionProviderFactory.getBasePermissionSet().addAPIClass(LoggerFactory.class);
        permissionProviderFactory.getBasePermissionSet().addAPIClass(Logger.class);
        permissionProviderFactory.getBasePermissionSet().addAPIClass(Reader.class);
        permissionProviderFactory.getBasePermissionSet().addAPIClass(StringReader.class);
        permissionProviderFactory.getBasePermissionSet().addAPIClass(BufferedReader.class);
        permissionProviderFactory.getBasePermissionSet().addAPIClass(java.awt.datatransfer.UnsupportedFlavorException.class);

        APIScanner apiScanner = new APIScanner(permissionProviderFactory);
        for (Module module : registry) {
            if (module.isOnClasspath()) {
                apiScanner.scan(module);
            }
        }

        permissionProviderFactory.getBasePermissionSet().grantPermission("com.google.gson", ReflectPermission.class);
        permissionProviderFactory.getBasePermissionSet().grantPermission("com.google.gson.internal", ReflectPermission.class);

        permissionProviderFactory.getBasePermissionSet().addAPIClass(java.nio.ByteBuffer.class);
        permissionProviderFactory.getBasePermissionSet().addAPIClass(java.nio.IntBuffer.class);

        Policy.setPolicy(new ModuleSecurityPolicy());
        System.setSecurityManager(new ModuleSecurityManager());
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
        ModuleEnvironment newEnvironment = new ModuleEnvironment(finalModules, permissionProviderFactory, Collections.<BytecodeInjector>emptyList());
        if (asPrimary) {
            environment = newEnvironment;
        }
        return newEnvironment;
    }

    @Override
    public ModuleMetadataJsonAdapter getModuleMetadataReader() {
        return metadataReader;
    }
}
