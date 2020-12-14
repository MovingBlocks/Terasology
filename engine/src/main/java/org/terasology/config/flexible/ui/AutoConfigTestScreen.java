// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.config.flexible.ui;

import org.terasology.assets.management.AssetManager;
import org.terasology.config.flexible.AutoConfig;
import org.terasology.config.flexible.AutoConfigManager;
import org.terasology.config.flexible.Setting;
import org.terasology.config.flexible.constraints.NumberRangeConstraint;
import org.terasology.engine.module.ModuleManager;
import org.terasology.nui.databinding.Binding;
import org.terasology.nui.databinding.DefaultBinding;
import org.terasology.nui.layouts.ColumnLayout;
import org.terasology.nui.widgets.UIButton;
import org.terasology.nui.widgets.UIText;
import org.terasology.nui.widgets.types.TypeWidgetLibrary;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;

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
    @In
    private TypeWidgetLibrary typeWidgetLibrary;
    @In
    private ModuleManager moduleManager;
    @In
    private AssetManager assetManager;
    @In
    private AutoConfigManager configManager;
    private AutoConfig testConfig = new TestConfig();

    private ColumnLayout mainContainer;
    private UIText bindingsLog;

    @Override
    public void initialise() {
        mainContainer = find("mainContainer", ColumnLayout.class);
        assert mainContainer != null;


        Binding<AutoConfig> testConfigBinding = new DefaultBinding<>(testConfig);
        mainContainer.addWidget(typeWidgetLibrary.getWidget(testConfigBinding, AutoConfig.class).get());

        for (AutoConfig config : configManager.getLoadedConfigs()) {
            if (config instanceof TestConfig) {
                // skip, used for dump value. below.
                continue;
            }
            Binding<AutoConfig> configBinding = new DefaultBinding<>(config);
            
            mainContainer.addWidget(typeWidgetLibrary.getWidget(configBinding, AutoConfig.class).get());
        }
        
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
        String logs = testConfig.getSettings()
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

        @Override
        public String getName() {
            return "Test Config";
        }
    }
}
