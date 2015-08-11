/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.rendering.nui.layers.mainMenu;

import java.net.URL;
import java.util.Collections;

import org.terasology.module.BaseModule;
import org.terasology.module.ModuleMetadata;
import com.google.common.collect.ImmutableList;

/**
 * A module that lives in a remote location.
 */
class RemoteModule extends BaseModule {

    public RemoteModule(ModuleMetadata meta) {
        super(Collections.emptyList(), meta);
    }

    @Override
    public ImmutableList<URL> getClasspaths() {
        return ImmutableList.of();
    }

    @Override
    public boolean isOnClasspath() {
        return false;
    }

    @Override
    public boolean isCodeModule() {
        return true;
    }

}
