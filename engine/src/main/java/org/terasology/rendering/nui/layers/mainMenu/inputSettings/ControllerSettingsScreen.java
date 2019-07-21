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
package org.terasology.rendering.nui.layers.mainMenu.inputSettings;

import org.terasology.assets.ResourceUrn;
import org.terasology.config.ControllerConfig;
import org.terasology.config.facade.InputDeviceConfiguration;
import org.terasology.engine.subsystem.config.BindsManager;
import org.terasology.i18n.TranslationSystem;
import org.terasology.input.BindableButton;
import org.terasology.input.ControllerInput;
import org.terasology.input.InputSystem;
import org.terasology.math.geom.Vector2i;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.animation.MenuAnimationSystems;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.widgets.UICheckbox;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UISlider;
import org.terasology.rendering.nui.widgets.UISpace;

import java.util.List;
import java.util.Map;

public class ControllerSettingsScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:controllerSettingsScreen");
    private int horizontalSpacing = 12;

    @In
    private InputSystem inputSystem;

    @In
    private InputDeviceConfiguration inputDeviceConfiguration;

    @In
    private TranslationSystem translationSystem;

    @In
    private BindsManager bindsManager;

    @Override
    public void initialise() {
        setAnimationSystem(MenuAnimationSystems.createDefaultSwipeAnimation());
        ColumnLayout mainLayout = find("main", ColumnLayout.class);

        if (mainLayout != null) {

            List<String> controllers = inputSystem.getControllerDevice().getControllers();
            if (controllers.isEmpty()) {
                addNoControllersMessage(mainLayout);
            } else {
                for (String name : controllers) {
                    ControllerConfig.ControllerInfo cfg = inputDeviceConfiguration.getController(name);
                    if (cfg.isEmpty()) {
                        // Get and save information about the controller from the ControllerDevice
                        cfg = inputSystem.getControllerDevice().describeController(name);
                        cfg = inputDeviceConfiguration.updateController(name, cfg);
                    }
                    addInputSection(mainLayout, name, cfg);
                }
            }
            WidgetUtil.trySubscribe(this, "back", button -> triggerBackAnimation());
        }
    }

    private void addNoControllersMessage(ColumnLayout layout) {
        layout.addWidget(new RowLayout(
                new UISpace(new Vector2i(10, 0)),
                new UILabel(translationSystem.translate("${engine:menu#no-controllers}")),
                new UISpace(new Vector2i(10, 0)))
                .setColumnRatios(0.3f, 0.4f, 0.3f)
                .setHorizontalSpacing(horizontalSpacing));
    }

    private void addInputSection(ColumnLayout layout, String name, ControllerConfig.ControllerInfo info) {
        UILabel categoryHeader = new UILabel(name);
        categoryHeader.setFamily("subheading");
        layout.addWidget(categoryHeader);

        float columnRatio = 0.4f;

        UICheckbox enabled = new UICheckbox();
        enabled.bindChecked(BindHelper.bindBeanProperty("enabled", info, Boolean.TYPE));
        layout.addWidget(new RowLayout(new UILabel(translationSystem.translate("${engine:menu#enabled}")), enabled)
                .setColumnRatios(columnRatio)
                .setHorizontalSpacing(horizontalSpacing));

        layout.addWidget(new UISpace(new Vector2i(0, 12)));

        for (ControllerConfig.Axis axis : info.getAxes()) {
            addAxis(layout, columnRatio, axis);
        }
        Map<ControllerInput, BindableButton> controllerBinds = bindsManager.getControllerBinds();
        controllerBinds.forEach((input, button) -> {
            addButtonMapping(layout, columnRatio, input, button);
        });

        layout.addWidget(new UISpace(new Vector2i(0, 16)));
    }

    private void addAxis(ColumnLayout layout, float columnRatio, ControllerConfig.Axis axis) {

        layout.addWidget(new RowLayout(new UILabel(axis.getDisplayName()), new UISpace(new Vector2i(10, 0)))
                .setColumnRatios(columnRatio)
                .setHorizontalSpacing(horizontalSpacing));

        UICheckbox inverted = new UICheckbox();
        inverted.bindChecked(BindHelper.bindBeanProperty("inverted", axis, Boolean.TYPE));
        layout.addWidget(new RowLayout(new UILabel(translationSystem.translate("${engine:menu#invert-axis}")), inverted)
                .setColumnRatios(columnRatio)
                .setHorizontalSpacing(horizontalSpacing));

        UISlider mvmtDeadZone = new UISlider();
        mvmtDeadZone.setIncrement(0.01f);
        mvmtDeadZone.setMinimum(0);
        mvmtDeadZone.setRange(1);
        mvmtDeadZone.setPrecision(2);
        mvmtDeadZone.bindValue(BindHelper.bindBeanProperty("deadZone", axis, Float.TYPE));
        layout.addWidget(new RowLayout(new UILabel(translationSystem.translate("${engine:menu#movement-dead-zone}")), mvmtDeadZone)
                .setColumnRatios(columnRatio)
                .setHorizontalSpacing(horizontalSpacing));

        layout.addWidget(new UISpace(new Vector2i(0, 12)));
    }

    private void addButtonMapping(ColumnLayout layout, float columnRatio, ControllerInput input, BindableButton button) {

        layout.addWidget(new RowLayout(
                new UILabel(input.getDisplayName()),
                new UILabel(translationSystem.translate(button.getDisplayName())),
                new UISpace(new Vector2i(10, 0))
        )
                .setColumnRatios(columnRatio)
                .setHorizontalSpacing(horizontalSpacing));

        layout.addWidget(new UISpace(new Vector2i(0, 12)));
    }
}
