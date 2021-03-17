// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.nui.layers.mainMenu.inputSettings;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import org.terasology.engine.config.BindsConfig;
import org.terasology.engine.core.SimpleUri;
import org.terasology.input.Input;
import org.terasology.nui.databinding.Binding;

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

}
