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

import java.util.List;

import org.terasology.assets.ResourceUrn;
import org.terasology.config.BindsConfig;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.input.Input;
import org.terasology.input.InputSystem;
import org.terasology.input.RegisterBindButton;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UILabel;


public class ChangeBindingPopup extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:changeBindingPopup");

    @In
    private Config config;

    @In
    private ModuleManager moduleManager;

    @In
    private InputSystem inputSystem;

    @In
    private Context context;

    private UIInputBind bindButton;

    private BindsConfig defaultBinds;

    @Override
    public void initialise() {
        defaultBinds = BindsConfig.createDefault(context);

        bindButton = find("new-binding", UIInputBind.class);
        WidgetUtil.trySubscribe(this, "remove", button -> bindButton.setNewInput(null));
        WidgetUtil.trySubscribe(this, "ok", button -> {
            bindButton.saveInput();
            getManager().popScreen();
        });
        WidgetUtil.trySubscribe(this, "cancel", button -> getManager().popScreen());
    }

    public void setBindingData(SimpleUri uri, RegisterBindButton bind, int index) {
        find("title", UILabel.class).setText(bind.description());
        BindsConfig bindConfig = config.getInput().getBinds();
        bindButton.bindInput(new InputConfigBinding(bindConfig, uri, index));
        List<Input> defaults = defaultBinds.getBinds(uri);
        find("default-binding", UILabel.class).setText(
                defaults.size() > index ? defaults.get(index).getDisplayName() : "<none>");
        find("default", UIButton.class).subscribe(e -> bindButton.setNewInput(
                defaults.size() > index ? defaults.get(index) : null));
    }

    @Override
    public void onClosed() {
        BindsConfig bindConfig = config.getInput().getBinds();
        bindConfig.applyBinds(inputSystem, moduleManager);
    }
}
