/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.layers.mainMenu.inputSettings;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.terasology.config.BindsConfig;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.input.Input;
import org.terasology.rendering.nui.databinding.Binding;

import java.util.List;

/**
 */
public class InputConfigBinding implements Binding<Input> {

    private BindsConfig config;
    private SimpleUri bindUri;
    private int position;

    public InputConfigBinding(BindsConfig config, SimpleUri bindUri) {
        this.config = config;
        this.bindUri = bindUri;
    }

    public InputConfigBinding(BindsConfig config, SimpleUri bindUri, int position) {
        this.config = config;
        this.bindUri = bindUri;
        this.position = position;
    }

    @Override
    public Input get() {
        return Iterators.get(config.getBinds(bindUri).iterator(), position, null);
    }

    @Override
    public void set(Input value) {
        List<Input> binds = Lists.newArrayList(config.getBinds(bindUri));
        if (value == null) {
            if (position < binds.size()) {
                binds.set(position, null);
            }
        } else {
            while (binds.size() <= position) {
                binds.add(null);
            }
            binds.set(position, value);
        }
        config.setBinds(bindUri, binds);
    }

    public void reset(Context context) {
        BindsConfig defaultBindings = BindsConfig.createDefault(context);
        set(Iterators.get(defaultBindings.getBinds(bindUri).iterator(), position, null));
    }

    public Input getDefault(Context context) {
        BindsConfig defaultBindings = BindsConfig.createDefault(context);
        return Iterators.get(defaultBindings.getBinds(bindUri).iterator(), position, null);
    }
}
