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

import com.google.common.base.Objects;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetSource;
import org.terasology.engine.CoreRegistry;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Immortius
 */
public class ExtensionModule implements Module {
    private ModuleManager manager;
    private ModuleInfo moduleInfo;
    private Path moduleRoot;
    private AssetSource moduleSource;
    private ClassLoader inactiveClassLoader;
    private ClassLoader activeClassLoader;
    private Reflections reflections;
    private String id;
    private Version version;

    public ExtensionModule(ModuleManager manager, Path moduleRoot, ModuleInfo info, Version version, AssetSource moduleSource) {
        if (info == null) {
            throw new IllegalArgumentException("Module info must not be null");
        }
        this.manager = manager;
        this.moduleInfo = info;
        this.moduleRoot = moduleRoot;
        this.moduleSource = moduleSource;
        this.id = UriUtil.normalise(info.getId());
        this.version = version;
    }

    void enable() {
        CoreRegistry.get(AssetManager.class).addAssetSource(moduleSource);
    }

    void disable() {
        CoreRegistry.get(AssetManager.class).removeAssetSource(moduleSource);
    }

    URL getModuleClasspathUrl() {
        try {
            if (Files.isDirectory(moduleRoot)) {
                Path classesDir = moduleRoot.resolve("classes");
                if (Files.isDirectory(classesDir)) {
                    return classesDir.toUri().toURL();
                }
            } else if (Files.isRegularFile(moduleRoot)) {
                String extension = com.google.common.io.Files.getFileExtension(moduleRoot.getFileName().toString());
                if (extension.equals(".jar")) {
                    return moduleRoot.toUri().toURL();
                }
            }
        } catch (MalformedURLException e) {
            return null;
        }
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Version getVersion() {
        return version;
    }

    @Override
    public Reflections getReflections() {
        if (reflections == null) {
            URL url = getModuleClasspathUrl();
            if (url != null) {
                ConfigurationBuilder configurationBuilder = new ConfigurationBuilder().addUrls(url).setScanners(new TypeAnnotationsScanner(), new SubTypesScanner());
                if (activeClassLoader != null) {
                    configurationBuilder.addClassLoader(activeClassLoader);
                } else {
                    configurationBuilder.addClassLoader(inactiveClassLoader);
                }
                reflections = new Reflections(configurationBuilder);
            }
        }
        return reflections;
    }

    @Override
    public boolean isCodeModule() {
        return getModuleClasspathUrl() != null;
    }

    void setActiveClassLoader(ClassLoader activeClassLoader) {
        this.activeClassLoader = activeClassLoader;
        reflections = null;
    }

    void setInactiveClassLoader(ClassLoader inactiveClassLoader) {
        this.inactiveClassLoader = inactiveClassLoader;
    }

    @Override
    public ModuleInfo getModuleInfo() {
        return moduleInfo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ExtensionModule) {
            ExtensionModule other = (ExtensionModule) obj;
            return Objects.equal(other.getId(), getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return id + ":" + version;
    }
}
