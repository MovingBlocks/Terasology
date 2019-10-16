/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.config.flexible.ui;

import org.terasology.assets.management.AssetManager;
import org.terasology.config.flexible.AutoConfig;
import org.terasology.config.flexible.Setting;
import org.terasology.config.flexible.constraints.NumberRangeConstraint;
import org.terasology.engine.module.ModuleManager;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIText;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.terasology.config.flexible.SettingArgument.constraint;
import static org.terasology.config.flexible.SettingArgument.defaultValue;
import static org.terasology.config.flexible.SettingArgument.description;
import static org.terasology.config.flexible.SettingArgument.name;
import static org.terasology.config.flexible.SettingArgument.type;

public class AutoConfigTestScreen extends CoreScreenLayer {
    private AutoConfig config = new TestConfig();

    private AutoConfigWidgetFactory configWidgetFactory;

    private ColumnLayout mainContainer;
    private UIText bindingsLog;

    @In
    ModuleManager moduleManager;

    @In
    AssetManager assetManager;

    @Override
    public void initialise() {
        configWidgetFactory = new AutoConfigWidgetFactory(moduleManager, assetManager);

        mainContainer = find("mainContainer", ColumnLayout.class);
        assert mainContainer != null;

        mainContainer.addWidget(configWidgetFactory.buildWidgetFor(config));

        UIButton logSettingButton = new UIButton();
        logSettingButton.setText("Log Setting Values");
        logSettingButton.subscribe(widget -> dumpBindings());

        mainContainer.addWidget(logSettingButton);

        bindingsLog = new UIText();

        bindingsLog.setReadOnly(true);
        bindingsLog.setMultiline(true);

        mainContainer.addWidget(bindingsLog);
    }


    public String toString(Object object) {
        if (object != null && object.getClass().isArray()) {
            return Arrays.toString((Object[]) object);
        }

        return Objects.toString(object);
    }

    private void dumpBindings() {
        String logs = config.getSettings()
                .stream()
                .map(setting ->
                        MessageFormat.format(
                                "Setting has a value {0} of type {1}",
                                toString(setting.get()),
                                setting.get().getClass().getSimpleName()
                        )
                )
                .collect(Collectors.joining("\n"));

        bindingsLog.setText(logs);
    }

    public static class TestConfig extends AutoConfig {
        public final Setting<Integer> integerSetting = setting(
                type(Integer.class),
                defaultValue(0),
                constraint(new NumberRangeConstraint<>(-5, 5, true, true)),
                name("Integer Test Setting"),
                description("Integer Test Setting with Number Range")
            );
    }
}
