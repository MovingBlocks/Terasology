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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.engine.SimpleUri;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.systems.In;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.InputCategory;
import org.terasology.input.RegisterBindButton;
import org.terasology.math.Rect2f;
import org.terasology.math.Vector2i;
import org.terasology.math.Border;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.UIScreen;
import org.terasology.rendering.nui.VerticalAlign;
import org.terasology.rendering.nui.baseLayouts.relative.HorizontalHint;
import org.terasology.rendering.nui.baseLayouts.relative.RelativeLayout;
import org.terasology.rendering.nui.baseLayouts.relative.VerticalHint;
import org.terasology.rendering.nui.baseWidgets.ButtonEventListener;
import org.terasology.rendering.nui.baseWidgets.UIButton;
import org.terasology.rendering.nui.baseWidgets.UICheckbox;
import org.terasology.rendering.nui.baseWidgets.UIImage;
import org.terasology.rendering.nui.baseWidgets.UILabel;
import org.terasology.rendering.nui.baseWidgets.UISlider;
import org.terasology.rendering.nui.baseWidgets.UISpace;
import org.terasology.rendering.nui.databinding.BindHelper;
import org.terasology.rendering.nui.baseLayouts.ColumnLayout;
import org.terasology.rendering.nui.baseLayouts.RowLayout;
import org.terasology.rendering.nui.baseLayouts.ScrollableArea;

import javax.vecmath.Vector2f;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Immortius
 */
public class InputSettingsScreen extends UIScreen {

    @In
    private NUIManager nuiManager;

    @In
    private Config config;

    @In
    private ModuleManager moduleManager;


    @Override
    public void initialise() {

        RowLayout mainLayout = new RowLayout();
        mainLayout.setPadding(new Border(4, 4, 4, 4));
        mainLayout.setFamily("option-grid");
        UISlider mouseSensitivity = new UISlider("mouseSensitivity");
        mouseSensitivity.setIncrement(0.025f);
        mouseSensitivity.setPrecision(3);
        mainLayout.addRow(new UILabel("mouseLabel", "heading-input", "Mouse"));
        mainLayout.addRow(new UILabel("Mouse Sensitivity:"), mouseSensitivity).setColumnRatios(0.4f);
        mainLayout.addRow(new UILabel("Invert Mouse:"), new UICheckbox("mouseYAxisInverted")).setColumnRatios(0.4f);

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

        ScrollableArea area = new ScrollableArea();
        area.setContent(mainLayout);
        area.setContentHeight(mainLayout.getRowCount() * 32);

        ColumnLayout footerGrid = new ColumnLayout("footer");
        footerGrid.setColumns(2);
        footerGrid.addWidget(new UIButton("reset", "Restore Defaults"));
        footerGrid.addWidget(new UIButton("close", "Back"));
        footerGrid.setPadding(new Border(4, 4, 0, 0));

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

    private void addInputSection(InputCategory category, RowLayout layout, Map<SimpleUri, RegisterBindButton> inputsById) {
        if (category != null) {
            layout.addRow(new UISpace());

            UILabel categoryHeader = new UILabel(category.displayName());
            categoryHeader.setFamily("heading-input");
            layout.addRow(categoryHeader);

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

    private void addInputBindRow(SimpleUri uri, RegisterBindButton bind, RowLayout layout) {
        UIInputBind inputBind = new UIInputBind();
        inputBind.bindInput(new InputConfigBinding(config.getInput().getBinds(), uri));
        UIInputBind secondaryInputBind = new UIInputBind();
        secondaryInputBind.bindInput(new InputConfigBinding(config.getInput().getBinds(), uri, 1));
        layout.addRow(new UILabel(bind.description()), inputBind, secondaryInputBind).setColumnRatios(0.4f);
    }

    @Override
    public void setContents(UIWidget contents) {
        super.setContents(contents);
        find("mouseSensitivity", UISlider.class).bindValue(BindHelper.bindBeanProperty("mouseSensitivity", config.getInput(), Float.TYPE));
        find("mouseYAxisInverted", UICheckbox.class).bindChecked(BindHelper.bindBeanProperty("mouseYAxisInverted", config.getInput(), Boolean.TYPE));
        find("reset", UIButton.class).subscribe(new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                config.getInput().reset();
            }
        });
        find("close", UIButton.class).subscribe(new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                nuiManager.popScreen();
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
