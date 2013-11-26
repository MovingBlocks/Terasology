/*
 * Copyright 2013 MovingBlocks
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
package org.terasology.rendering.nui.mainMenu.inputSettings;

import com.google.common.collect.Iterators;
import org.terasology.config.BindsConfig;
import org.terasology.input.Input;
import org.terasology.rendering.nui.databinding.Binding;

/**
 * @author Immortius
 */
public class InputConfigBinding implements Binding<Input> {
    private BindsConfig config;
    private String moduleId;
    private String bindId;

    public InputConfigBinding(BindsConfig config, String moduleId, String bindId) {
        this.config = config;
        this.moduleId = moduleId;
        this.bindId = bindId;
    }

    @Override
    public Input get() {
        return Iterators.get(config.getBinds(moduleId, bindId).iterator(), 0, null);
    }

    @Override
    public void set(Input value) {
        config.setBinds(moduleId, bindId, value);
    }
}
