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

import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.engine.module.Module;
import org.terasology.engine.module.ModuleManager;
import org.terasology.entitySystem.systems.In;
import org.terasology.input.BindButtonEvent;
import org.terasology.input.RegisterBindButton;
import org.terasology.math.Rect2f;
import org.terasology.math.Vector2i;
import org.terasology.rendering.nui.Border;
import org.terasology.rendering.nui.NUIManager;
import org.terasology.rendering.nui.UIScreen;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.baseWidgets.ButtonEventListener;
import org.terasology.rendering.nui.baseWidgets.UIButton;
import org.terasology.rendering.nui.baseWidgets.UICheckbox;
import org.terasology.rendering.nui.baseWidgets.UIImage;
import org.terasology.rendering.nui.baseWidgets.UILabel;
import org.terasology.rendering.nui.baseWidgets.UISlider;
import org.terasology.rendering.nui.layout.ArbitraryLayout;
import org.terasology.rendering.nui.layout.ColumnLayout;
import org.terasology.rendering.nui.layout.RowLayout;
import org.terasology.rendering.nui.layout.ScrollableArea;

import javax.vecmath.Vector2f;

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
        mainLayout.addRow(new UILabel("Mouse Sensitivity:"), new UISlider("mouseSensitivity"));
        mainLayout.addRow(new UILabel("Invert Mouse:"), new UICheckbox("invertMouse"));
        for (Module module : moduleManager.getModules()) {
            if (module.isCodeModule()) {
                for (Class<?> bindEvent : module.getReflections().getTypesAnnotatedWith(RegisterBindButton.class)) {
                    if (BindButtonEvent.class.isAssignableFrom(bindEvent)) {
                        RegisterBindButton bindRegister = bindEvent.getAnnotation(RegisterBindButton.class);
                        UIInputBind inputBind = new UIInputBind();
                        inputBind.bindInput(new InputConfigBinding(config.getInput().getBinds(), module.getId(), bindRegister.id()));
                        mainLayout.addRow(new UILabel(bindRegister.description()), inputBind);
                    }
                }
            }
        }

        ScrollableArea area = new ScrollableArea();
        area.setContent(mainLayout);
        area.setContentSize(new Vector2i(500, mainLayout.getRowCount() * 32));

        ColumnLayout footerGrid = new ColumnLayout();
        footerGrid.setColumns(2);
        footerGrid.addWidget(new UIButton("reset", "Restore Defaults"));
        footerGrid.addWidget(new UIButton("close", "Back"));
        footerGrid.setPadding(new Border(4, 4, 0, 0));

        ArbitraryLayout layout = new ArbitraryLayout();
        layout.addFixedWidget(new UIImage(Assets.getTexture("engine:terasology")), new Vector2i(512, 128), new Vector2f(0.5f, 0.1f));
        layout.addFillWidget(new UILabel("title", "title", "Input Settings"), Rect2f.createFromMinAndSize(0.0f, 0.2f, 1.0f, 0.1f));
        layout.addFillWidget(area, Rect2f.createFromMinAndSize(0.3f, 0.25f, 0.4f, 0.6f));
        layout.addFixedWidget(footerGrid, new Vector2i(400, 32), new Vector2f(0.50f, 0.95f));

        setContents(layout);
    }

    @Override
    public void setContents(UIWidget contents) {
        super.setContents(contents);
        find("close", UIButton.class).subscribe(new ButtonEventListener() {
            @Override
            public void onButtonActivated(UIButton button) {
                nuiManager.popScreen();
            }
        });
    }
}
