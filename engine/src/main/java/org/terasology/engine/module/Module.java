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
import org.terasology.asset.AssetSource;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Immortius
 */
public interface Module {

    AssetSource getModuleSource();

    String getId();

    Version getVersion();

    Reflections getReflections();

    boolean isCodeModule();

    ModuleInfo getModuleInfo();

    boolean isDataAvailable();

    InputStream getData() throws IOException;

    long getSize();
}
