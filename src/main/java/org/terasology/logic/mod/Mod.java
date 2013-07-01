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

package org.terasology.logic.mod;

import com.google.common.base.Objects;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;
import org.terasology.asset.AssetManager;
import org.terasology.asset.AssetSource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Immortius
 */
public class Mod {
    private ModInfo modInfo;
    private File modRoot;
    private AssetSource modSource;
    private boolean enabled;
    private ClassLoader inactiveClassLoader;
    private ClassLoader activeClassLoader;
    private Reflections reflections;

    public Mod(File modRoot, ModInfo info, AssetSource modSource) {
        if (info == null) {
            throw new IllegalArgumentException("Mod info must not be null");
        }
        this.modInfo = info;
        this.modRoot = modRoot;
        this.modSource = modSource;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                AssetManager.getInstance().addAssetSource(modSource);
            } else {
                AssetManager.getInstance().removeAssetSource(modSource);
            }
        }
    }

    public File getModRoot() {
        return modRoot;
    }

    public URL getModClasspathUrl() {
        try {
            if (modRoot.isDirectory()) {
                File classesDir = new File(modRoot, "classes");
                if (classesDir.exists() && classesDir.isDirectory()) {
                    return classesDir.toURI().toURL();
                }
            } else {
                if (modRoot.getAbsolutePath().endsWith(".jar")) {
                    return modRoot.toURI().toURL();
                }
            }
        } catch (MalformedURLException e) {
            return null;
        }
        return null;
    }

    public Reflections getReflections() {
        if (reflections == null) {
            URL url = getModClasspathUrl();
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

    public boolean isCodeMod() {
        return getModClasspathUrl() != null;
    }

    public ClassLoader getActiveClassLoader() {
        return activeClassLoader;
    }

    void setActiveClassLoader(ClassLoader activeClassLoader) {
        this.activeClassLoader = activeClassLoader;
        reflections = null;
    }

    void setInactiveClassLoader(ClassLoader inactiveClassLoader) {
        this.inactiveClassLoader = inactiveClassLoader;
    }

    public ModInfo getModInfo() {
        return modInfo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Mod) {
            Mod other = (Mod) obj;
            return Objects.equal(other.getModInfo().getId(), modInfo.getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return modInfo.getId().hashCode();
    }
}
