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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.terasology.assets.ResourceUrn;
import org.terasology.config.BindsConfig;
import org.terasology.config.Config;
import org.terasology.config.ControllerConfig.ControllerInfo;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.ModuleManager;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.Input;
import org.terasology.input.InputCategory;
import org.terasology.input.InputSystem;
import org.terasology.input.RegisterBindButton;
import org.terasology.math.geom.Vector2i;
import org.terasology.module.DependencyResolver;
import org.terasology.module.Module;
import org.terasology.module.ModuleEnvironment;
import org.terasology.module.ResolutionResult;
import org.terasology.module.predicates.FromModule;
import org.terasology.naming.Name;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.databinding.ReadOnlyBinding;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.layouts.ScrollableArea;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UICheckbox;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UISlider;
import org.terasology.rendering.nui.widgets.UISpace;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 */
public class InputSettingsScreen extends CoreScreenLayer {

    public static final ResourceUrn ASSET_URI = new ResourceUrn("engine:inputSettingsScreen");

    private int horizontalSpacing = 12;

    @In
    private Config config;

    @In
    private ModuleManager moduleManager;

    @In
    private InputSystem inputSystem;

    @In
    private Context context;

    @Override
    public void initialise() {
        ColumnLayout mainLayout = new ColumnLayout();
        mainLayout.setHorizontalSpacing(8);
        mainLayout.setVerticalSpacing(8);
        mainLayout.setFamily("option-grid");

        UISlider mouseSensitivity = new UISlider("mouseSensitivity");
        mouseSensitivity.bindValue(BindHelper.bindBeanProperty("mouseSensitivity", config.getInput(), Float.TYPE));
        mouseSensitivity.setIncrement(0.025f);
        mouseSensitivity.setPrecision(3);

        UICheckbox mouseInverted = new UICheckbox("mouseYAxisInverted");
        mouseInverted.bindChecked(BindHelper.bindBeanProperty("mouseYAxisInverted", config.getInput(), Boolean.TYPE));

        mainLayout.addWidget(new UILabel("mouseLabel", "subheading", "Mouse"));
        mainLayout.addWidget(new RowLayout(new UILabel("Mouse Sensitivity:"), mouseSensitivity)
                .setColumnRatios(0.4f)
                .setHorizontalSpacing(horizontalSpacing));
        mainLayout.addWidget(new RowLayout(new UILabel("Invert Mouse:"), mouseInverted)
                .setColumnRatios(0.4f)
                .setHorizontalSpacing(horizontalSpacing));

        Map<String, InputCategory> inputCategories = Maps.newHashMap();
        Map<SimpleUri, RegisterBindButton> inputsById = Maps.newHashMap();
        DependencyResolver resolver = new DependencyResolver(moduleManager.getRegistry());
        for (Name moduleId : moduleManager.getRegistry().getModuleIds()) {
            Module module = moduleManager.getRegistry().getLatestModuleVersion(moduleId);
            if (module.isCodeModule()) {
                ResolutionResult result = resolver.resolve(moduleId);
                if (result.isSuccess()) {
                    try (ModuleEnvironment environment = moduleManager.loadEnvironment(result.getModules(), false)) {
                        for (Class<?> holdingType : environment.getTypesAnnotatedWith(InputCategory.class, new FromModule(environment, moduleId))) {
                            InputCategory inputCategory = holdingType.getAnnotation(InputCategory.class);
                            inputCategories.put(module.getId() + ":" + inputCategory.id(), inputCategory);
                        }
                        for (Class<?> bindEvent : environment.getTypesAnnotatedWith(RegisterBindButton.class, new FromModule(environment, moduleId))) {
                            if (BindButtonEvent.class.isAssignableFrom(bindEvent)) {
                                RegisterBindButton bindRegister = bindEvent.getAnnotation(RegisterBindButton.class);
                                inputsById.put(new SimpleUri(module.getId(), bindRegister.id()), bindRegister);
                            }
                        }
                    }
                }

            }
        }

        addInputSection(inputCategories.remove("engine:movement"), mainLayout, inputsById);
        addInputSection(inputCategories.remove("engine:interaction"), mainLayout, inputsById);
        addInputSection(inputCategories.remove("engine:inventory"), mainLayout, inputsById);
        addInputSection(inputCategories.remove("engine:general"), mainLayout, inputsById);
        for (InputCategory category : inputCategories.values()) {
            addInputSection(category, mainLayout, inputsById);
        }
        mainLayout.addWidget(new UISpace(new Vector2i(1, 16)));

        List<String> controllers = inputSystem.getControllerDevice().getControllers();
        for (String name : controllers) {
            ControllerInfo cfg = config.getInput().getControllers().getController(name);
            addInputSection(mainLayout, name, cfg);
        }

        ScrollableArea area = find("area", ScrollableArea.class);
        area.setContent(mainLayout);

        WidgetUtil.trySubscribe(this, "reset", button -> config.getInput().reset(context));
        WidgetUtil.trySubscribe(this, "back", button -> getManager().popScreen());
    }

    private void addInputSection(InputCategory category, ColumnLayout layout, Map<SimpleUri, RegisterBindButton> inputsById) {
        if (category != null) {
            layout.addWidget(new UISpace(new Vector2i(0, 16)));

            UILabel categoryHeader = new UILabel(category.displayName());
            categoryHeader.setFamily("subheading");
            layout.addWidget(categoryHeader);

            Set<SimpleUri> processedBinds = Sets.newHashSet();

            for (String bindId : category.ordering()) {
                SimpleUri bindUri = new SimpleUri(bindId);
                if (bindUri.isValid()) {
                    RegisterBindButton bind = inputsById.get(new SimpleUri(bindId));
                    if (bind != null) {
                        addInputBindRow(bindUri, bind, layout);
                        processedBinds.add(bindUri);
                    }
                }
            }


            List<ExtensionBind> extensionBindList = Lists.newArrayList();
            for (Map.Entry<SimpleUri, RegisterBindButton> bind : inputsById.entrySet()) {
                if (bind.getValue().category().equals(category.id()) && !processedBinds.contains(bind.getKey())) {
                    extensionBindList.add(new ExtensionBind(bind.getKey(), bind.getValue()));
                }
            }
            Collections.sort(extensionBindList);
            for (ExtensionBind extension : extensionBindList) {
                addInputBindRow(extension.uri, extension.bind, layout);
            }
        }
    }

    private void addInputSection(ColumnLayout layout, String name, ControllerInfo info) {
        UILabel categoryHeader = new UILabel(name);
        categoryHeader.setFamily("subheading");
        layout.addWidget(categoryHeader);

        float columnRatio = 0.4f;

        UICheckbox invertX = new UICheckbox();
        invertX.bindChecked(BindHelper.bindBeanProperty("invertX", info, Boolean.TYPE));
        layout.addWidget(new RowLayout(new UILabel("Invert X Axis"), invertX)
                .setColumnRatios(columnRatio)
                .setHorizontalSpacing(horizontalSpacing));

        UICheckbox invertY = new UICheckbox();
        invertY.bindChecked(BindHelper.bindBeanProperty("invertY", info, Boolean.TYPE));
        layout.addWidget(new RowLayout(new UILabel("Invert Y Axis"), invertY)
                .setColumnRatios(columnRatio)
                .setHorizontalSpacing(horizontalSpacing));

        UICheckbox invertZ = new UICheckbox();
        invertZ.bindChecked(BindHelper.bindBeanProperty("invertZ", info, Boolean.TYPE));
        layout.addWidget(new RowLayout(new UILabel("Invert Z Axis"), invertZ)
                .setColumnRatios(columnRatio)
                .setHorizontalSpacing(horizontalSpacing));

        UISlider mvmtDeadZone = new UISlider();
        mvmtDeadZone.setIncrement(0.01f);
        mvmtDeadZone.setMinimum(0);
        mvmtDeadZone.setRange(1);
        mvmtDeadZone.setPrecision(2);
        mvmtDeadZone.bindValue(BindHelper.bindBeanProperty("movementDeadZone", info, Float.TYPE));
        layout.addWidget(new RowLayout(new UILabel("Movement Axis Dead Zone"), mvmtDeadZone)
            .setColumnRatios(columnRatio)
            .setHorizontalSpacing(horizontalSpacing));

        UISlider rotDeadZone = new UISlider();
        rotDeadZone.setIncrement(0.01f);
        rotDeadZone.setMinimum(0);
        rotDeadZone.setRange(1);
        rotDeadZone.setPrecision(2);
        rotDeadZone.bindValue(BindHelper.bindBeanProperty("rotationDeadZone", info, Float.TYPE));

        layout.addWidget(new RowLayout(new UILabel("Rotation Axis Dead Zone"), rotDeadZone)
                .setColumnRatios(columnRatio)
                .setHorizontalSpacing(horizontalSpacing));

        layout.addWidget(new UISpace(new Vector2i(0, 16)));
    }

    private void addInputBindRow(SimpleUri uri, RegisterBindButton bind, ColumnLayout layout) {
        BindsConfig bindConfig = config.getInput().getBinds();
        List<Input> binds = bindConfig.getBinds(uri);
        UIButton primaryInputBind = new UIButton();
        primaryInputBind.bindText(new BindingText(binds, 0));
        primaryInputBind.subscribe(event -> {
            ChangeBindingPopup popup = getManager().pushScreen(ChangeBindingPopup.ASSET_URI, ChangeBindingPopup.class);
            popup.setBindingData(uri, bind, 0);
        });

        UIButton secondaryInputBind = new UIButton();
        secondaryInputBind.bindText(new BindingText(binds, 1));
        secondaryInputBind.subscribe(event -> {
            ChangeBindingPopup popup = getManager().pushScreen(ChangeBindingPopup.ASSET_URI, ChangeBindingPopup.class);
            popup.setBindingData(uri, bind, 1);
        });
        layout.addWidget(new RowLayout(new UILabel(bind.description()), primaryInputBind, secondaryInputBind)
                .setColumnRatios(0.4f)
                .setHorizontalSpacing(horizontalSpacing));
    }

    @Override
    public void onClosed() {
        config.getInput().getBinds().applyBinds(inputSystem, moduleManager);
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    private final class BindingText extends ReadOnlyBinding<String> {

        private List<Input> binds;
        private int index;

        public BindingText(List<Input> binds, int index) {
            this.binds = binds;
            this.index = index;
        }

        @Override
        public String get() {
            if (binds.size() > index) {
                Input input = binds.get(index);
                if (input != null) {
                    return input.getDisplayName();
                }
            }
            return "<not bound>";
        }
    }

    private static final class ExtensionBind implements Comparable<ExtensionBind> {
        private SimpleUri uri;
        private RegisterBindButton bind;

        private ExtensionBind(SimpleUri uri, RegisterBindButton bind) {
            this.uri = uri;
            this.bind = bind;
        }

        @Override
        public int compareTo(ExtensionBind o) {
            int descriptionOrder = bind.description().compareTo(o.bind.description());
            if (descriptionOrder == 0) {
                return uri.compareTo(o.uri);
            }
            return descriptionOrder;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof ExtensionBind) {
                ExtensionBind other = (ExtensionBind) obj;
                return Objects.equals(bind.description(), other.bind.description()) && Objects.equals(uri, other.uri);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(uri, bind.description());
        }
    }


}
