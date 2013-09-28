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
import org.terasology.engine.TerasologyConstants;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Immortius
 */
public class EngineModule implements Module {

    private Reflections reflections;
    private ModuleInfo info;
    private String id = TerasologyConstants.ENGINE_MODULE;
    private Version version;

    public EngineModule(Reflections reflections, ModuleInfo moduleInfo) {
        this.reflections = reflections;
        this.info = moduleInfo;
        this.version = Version.create(moduleInfo.getVersion());
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

    @Override
    public boolean isDataAvailable() {
        return false;
    }

    @Override
    public InputStream getData() throws IOException {
        throw new IOException("Cannot get data for the engine module");
    }

    @Override
    public long getSize() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof EngineModule) {
            return ((EngineModule) obj).getId().equals(getId());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id + ":" + version;
    }
}
