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

import org.terasology.asset.Assets;
import org.terasology.config.Config;
import org.terasology.context.Context;
import org.terasology.engine.module.ModuleManager;
import org.terasology.input.Input;
import org.terasology.input.InputSystem;
import org.terasology.math.geom.Vector2i;
import org.terasology.registry.In;
import org.terasology.rendering.nui.CoreScreenLayer;
import org.terasology.rendering.nui.UIWidget;
import org.terasology.rendering.nui.VerticalAlign;
import org.terasology.rendering.nui.WidgetUtil;
import org.terasology.rendering.nui.databinding.Binding;
import org.terasology.rendering.nui.databinding.DefaultBinding;
import org.terasology.rendering.nui.layouts.ColumnLayout;
import org.terasology.rendering.nui.layouts.RowLayout;
import org.terasology.rendering.nui.layouts.ScrollableArea;
import org.terasology.rendering.nui.layouts.relative.HorizontalHint;
import org.terasology.rendering.nui.layouts.relative.RelativeLayout;
import org.terasology.rendering.nui.layouts.relative.VerticalHint;
import org.terasology.rendering.nui.widgets.UIButton;
import org.terasology.rendering.nui.widgets.UIImage;
import org.terasology.rendering.nui.widgets.UILabel;
import org.terasology.rendering.nui.widgets.UISpace;


public class InputChangingScreen extends CoreScreenLayer {

    @In
    private Config config;

    @In
    private ModuleManager moduleManager;

    @In
    private InputSystem inputSystem;

    @In
    private Context context;

    private String description;

    private Binding<Input> input = new DefaultBinding<>();

    private UIInputBind inputBind;
    private InputConfigBinding binding;

    @Override
    public void onOpened() {
        inputBind = new UIInputBind();
        inputBind.bindInput(getInput());
        initialise();
    }

    @Override
    public void initialise() {
        ColumnLayout mainLayout = new ColumnLayout();
        mainLayout.setHorizontalSpacing(8);
        mainLayout.setVerticalSpacing(8);
        mainLayout.setFamily("option-grid");

        mainLayout.addWidget(new UISpace(new Vector2i(1, 16)));
        mainLayout.addWidget(new UILabel("subtitle", "title", getDescription()));

        RowLayout currentBinding = new RowLayout("current-binding");
        currentBinding.addWidget(new UILabel("Current binding"), null);
        UIButton currentBindingName = new UIButton();
        currentBindingName.setEnabled(false);
        if (inputBind.getInput() != null) {
            currentBindingName.setText(inputBind.getInput().getDisplayName());
        }
        currentBinding.addWidget(currentBindingName, null);
        currentBinding.setColumnRatios(0.3f);
        int horizontalSpacing = 20;
        currentBinding.setHorizontalSpacing(horizontalSpacing);

        mainLayout.addWidget(currentBinding);
        mainLayout.addWidget(new UISpace(new Vector2i(1, 16)));

        RowLayout newBinding = new RowLayout("new-binding");
        newBinding.addWidget(new UILabel("New binding"), null);
        newBinding.addWidget(inputBind, null);
        newBinding.setColumnRatios(0.3f);
        newBinding.setHorizontalSpacing(horizontalSpacing);

        mainLayout.addWidget(newBinding);
        mainLayout.addWidget(new UISpace(new Vector2i(1, 16)));

        ColumnLayout optionsGrid = new ColumnLayout("options");
        optionsGrid.setFamily("menu-options");
        optionsGrid.setColumns(2);
        optionsGrid.addWidget(new UIButton("remove", "Remove"));
        optionsGrid.addWidget(new UIButton("default", "Default"));
        optionsGrid.setHorizontalSpacing(8);

        mainLayout.addWidget(optionsGrid);

        ScrollableArea area = new ScrollableArea();
        area.setContent(mainLayout);

        ColumnLayout footerGrid = new ColumnLayout("footer");
        footerGrid.setFamily("menu-options");
        footerGrid.setColumns(2);
        footerGrid.addWidget(new UIButton("save", "Save"));
        footerGrid.addWidget(new UIButton("cancel", "Cancel"));
        footerGrid.setHorizontalSpacing(8);

        RelativeLayout layout = new RelativeLayout();
        layout.addWidget(new UIImage("title", Assets.getTexture("engine:terasology").get()),
                HorizontalHint.create().fixedWidth(512).center(),
                VerticalHint.create().fixedHeight(128).alignTop(48));
        layout.addWidget(new UILabel("subtitle", "title", "Input Binding"),
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

    @Override
    public void setContents(UIWidget contents) {
        super.setContents(contents);
        WidgetUtil.trySubscribe(this, "remove", button -> inputBind.setNewInput(null));
        WidgetUtil.trySubscribe(this, "default", button -> inputBind.setNewInput(binding.getDefault(context)));
        WidgetUtil.trySubscribe(this, "save", widget -> {
            inputBind.saveInput();
            getManager().popScreen();
        });
        WidgetUtil.trySubscribe(this, "cancel", button ->
            getManager().popScreen()
        );
    }

    @Override
    public void onClosed() {
        config.getInput().getBinds().applyBinds(inputSystem, moduleManager);
    }

    @Override
    public boolean isLowerLayerVisible() {
        return false;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Binding<Input> getInput() {
        return input;
    }

    public void setInput(Binding<Input> input) {
        this.input = input;
    }

    public void bindInput(InputConfigBinding newBinding) {
        this.binding = newBinding;
        setInput(newBinding);
    }
}
