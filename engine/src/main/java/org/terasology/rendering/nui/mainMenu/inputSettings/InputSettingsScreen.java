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
package org.terasology.rendering.nui.mainMenu.inputSettings;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.registry.In;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.InputCategory;
import org.terasology.input.RegisterBindButton;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.UIScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.VerticalAlign;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.layouts.ScrollableArea;
import org.terasology.rendering.nui.layouts.relative.HorizontalHint;
import org.terasology.rendering.nui.layouts.relative.RelativeLayout;
import org.terasology.rendering.nui.layouts.relative.VerticalHint;
import org.terasology.rendering.nui.widgets.ActivateEventListener;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UICheckbox;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UISlider;
import org.terasology.rendering.nui.widgets.UISpace;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Immortius
 */
public class InputSettingsScreen extends UIScreenLayer {

    private int horizontalSpacing = 4;

    @In
    private Config config;

    @In
    private ModuleManager moduleManager;


    @Override
    public void initialise() {

        ColumnLayout mainLayout = new ColumnLayout();
        mainLayout.setHorizontalSpacing(8);
        mainLayout.setVerticalSpacing(8);
        mainLayout.setFamily("option-grid");
        UISlider mouseSensitivity = new UISlider("mouseSensitivity");
        mouseSensitivity.setIncrement(0.025f);
        mouseSensitivity.setPrecision(3);

        mainLayout.addWidget(new UILabel("mouseLabel", "heading-input", "Mouse"));
        mainLayout.addWidget(new RowLayout(new UILabel("Mouse Sensitivity:"), mouseSensitivity).setColumnRatios(0.4f).setHorizontalSpacing(horizontalSpacing));
        mainLayout.addWidget(new RowLayout(new UILabel("Invert Mouse:"), new UICheckbox("mouseYAxisInverted")).setColumnRatios(0.4f).setHorizontalSpacing(horizontalSpacing));

        Map<String, InputCategory> inputCategories = Maps.newHashMap();
        Map<SimpleUri, RegisterBindButton> inputsById = Maps.newHashMap();
        for (Module module : moduleManager.getModules()) {
            if (module.isCodeModule()) {
                for (Class<?> holdingType : module.getReflections().getTypesAnnotatedWith(InputCategory.class)) {
                    InputCategory inputCategory = holdingType.getAnnotation(InputCategory.class);
                    inputCategories.put(module.getId() + ":" + inputCategory.id(), inputCategory);
                }
                for (Class<?> bindEvent : module.getReflections().getTypesAnnotatedWith(RegisterBindButton.class)) {
                    if (BindButtonEvent.class.isAssignableFrom(bindEvent)) {
                        RegisterBindButton bindRegister = bindEvent.getAnnotation(RegisterBindButton.class);
                        inputsById.put(new SimpleUri(module.getId(), bindRegister.id()), bindRegister);
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

        ScrollableArea area = new ScrollableArea();
        area.setContent(mainLayout);
        //area.setContentHeight(mainLayout.getRowCount() * 32);

        ColumnLayout footerGrid = new ColumnLayout("footer");
        footerGrid.setFamily("menu-options");
        footerGrid.setColumns(2);
        footerGrid.addWidget(new UIButton("reset", "Restore Defaults"));
        footerGrid.addWidget(new UIButton("close", "Back"));
        footerGrid.setHorizontalSpacing(8);

        RelativeLayout layout = new RelativeLayout();
        layout.addWidget(new UIImage("title", Assets.getTexture("engine:terasology")),
                HorizontalHint.create().fixedWidth(512).center(),
                VerticalHint.create().fixedHeight(128).alignTop(48));
        layout.addWidget(new UILabel("subtitle", "title", "Input Settings"),
                HorizontalHint.create().center(),
                VerticalHint.create().fixedHeight(48).alignTopRelativeTo("title", VerticalAlign.BOTTOM));
        layout.addWidget(area,
                HorizontalHint.create().fixedWidth(640).center(),
                VerticalHint.create().alignTopRelativeTo("subtitle", VerticalAlign.BOTTOM).alignBottomRelativeTo("footer", VerticalAlign.TOP, 48));
        layout.addWidget(footerGrid,
                HorizontalHint.create().center().fixedWidth(400),
                VerticalHint.create().fixedHeight(48).alignBottom(48));

        setContents(layout);
    }

    private void addInputSection(InputCategory category, ColumnLayout layout, Map<SimpleUri, RegisterBindButton> inputsById) {
        if (category != null) {
            layout.addWidget(new UISpace(new Vector2i(0, 16)));

            UILabel categoryHeader = new UILabel(category.displayName());
            categoryHeader.setFamily("heading-input");
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

    private void addInputBindRow(SimpleUri uri, RegisterBindButton bind, ColumnLayout layout) {
        UIInputBind inputBind = new UIInputBind();
        inputBind.bindInput(new InputConfigBinding(config.getInput().getBinds(), uri));
        UIInputBind secondaryInputBind = new UIInputBind();
        secondaryInputBind.bindInput(new InputConfigBinding(config.getInput().getBinds(), uri, 1));
        layout.addWidget(new RowLayout(new UILabel(bind.description()), inputBind, secondaryInputBind).setColumnRatios(0.4f).setHorizontalSpacing(horizontalSpacing));
    }

    @Override
    public void setContents(UIWidget contents) {
        super.setContents(contents);
        find("mouseSensitivity", UISlider.class).bindValue(BindHelper.bindBeanProperty("mouseSensitivity", config.getInput(), Float.TYPE));
        find("mouseYAxisInverted", UICheckbox.class).bindChecked(BindHelper.bindBeanProperty("mouseYAxisInverted", config.getInput(), Boolean.TYPE));
        find("reset", UIButton.class).subscribe(new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                config.getInput().reset();
            }
        });
        find("close", UIButton.class).subscribe(new ActivateEventListener() {
            @Override
            public void onActivated(UIWidget button) {
                getManager().popScreen();
            }
        });
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
            return bind.description().compareTo(o.bind.description());
        }
    }


}
