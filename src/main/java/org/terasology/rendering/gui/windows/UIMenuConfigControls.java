/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
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
package org.terasology.rendering.gui.windows;

import com.google.common.collect.Lists;
import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.config.BindsConfig;
import org.terasology.config.Config;
import org.terasology.game.CoreRegistry;
import org.terasology.input.Input;
import org.terasology.input.InputType;
import org.terasology.input.events.KeyEvent;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.framework.events.KeyListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.layout.GridLayout;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIComposite;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UISlider;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;
import java.util.Collection;
import java.util.List;

/**
 * @author Overdhose
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         Date: 29/07/12
 */
public final class UIMenuConfigControls extends UIWindow {

    // TODO: Much of this can be derived from the register bind button annotations, more generic input screen?
    private static class ButtonDefinition {
        public String bindId;
        public String displayText;
        public int group;

        public ButtonDefinition(String bindId, String displayText, int group) {
            this.bindId = bindId;
            this.displayText = displayText;
            this.group = group;
        }
    }

    // TODO: Yeah, this is rough as, populate better
    private List<ButtonDefinition> buttonDefs = Lists.newArrayList(
            new ButtonDefinition("engine:forwards", "Forwards", 0),
            new ButtonDefinition("engine:backwards", "Backwards", 0),
            new ButtonDefinition("engine:left", "Left", 0),
            new ButtonDefinition("engine:right", "Right", 0),
            new ButtonDefinition("engine:useItem", "Use Item", 0),
            new ButtonDefinition("engine:attack", "Attack", 0),
            new ButtonDefinition("engine:toolbarNext", "Next Item", 0),
            new ButtonDefinition("engine:toolbarPrev", "Previous Item", 0),
            new ButtonDefinition("engine:frob", "Activate Target", 1),
            new ButtonDefinition("engine:jump", "Jump", 1),
            new ButtonDefinition("engine:run", "Run", 1),
            new ButtonDefinition("engine:crouch", "Crouch", 1),
            new ButtonDefinition("engine:inventory", "Inventory", 1),
            new ButtonDefinition("engine:pause", "Pause", 1),
            new ButtonDefinition("engine:console", "Console", 1),
            new ButtonDefinition("miniion:toggleMinionMode", "Minion Mode", 2),
            new ButtonDefinition("engine:toolbarSlot0", "Hotkey 1", 3),
            new ButtonDefinition("engine:toolbarSlot1", "Hotkey 2", 3),
            new ButtonDefinition("engine:toolbarSlot2", "Hotkey 3", 3),
            new ButtonDefinition("engine:toolbarSlot3", "Hotkey 4", 3),
            new ButtonDefinition("engine:toolbarSlot4", "Hotkey 5", 3),
            new ButtonDefinition("engine:toolbarSlot5", "Hotkey 6", 3),
            new ButtonDefinition("engine:toolbarSlot6", "Hotkey 7", 3),
            new ButtonDefinition("engine:toolbarSlot7", "Hotkey 8", 3),
            new ButtonDefinition("engine:toolbarSlot8", "Hotkey 9", 3),
            new ButtonDefinition("engine:toolbarSlot9", "Hotkey 10", 3)
    );

    private List<UILabel> inputLabels = Lists.newArrayList();
    private List<UIButton> inputButtons = Lists.newArrayList();

    String editButtonCurrent = "";
    UIButton editButton = null;
    final UIImage title;

    private final Config config = CoreRegistry.get(Config.class);

    private final UIButton backToConfigMenuButton;
    private final UIButton defaultButton;

    private final UIComposite container;
    private final UIComposite[] buttonGroups = new UIComposite[4];

    private final UISlider mouseSensitivity;
    private final UILabel subtitle;

    public UIMenuConfigControls() {
        setId("config:controls");
        setBackgroundImage("engine:loadingbackground");
        setModal(true);
        maximize();

        addMouseButtonListener(new MouseButtonListener() {
            @Override
            public void wheel(UIDisplayElement element, int wheel, boolean intersect) {
                if (editButton != null) {
                    changeButton(editButton, new Input(InputType.MOUSE_WHEEL, wheel));
                    editButton = null;
                }
            }

            @Override
            public void up(UIDisplayElement element, int button, boolean intersect) {

            }

            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {
                if (editButton != null) {
                    changeButton(editButton, new Input(InputType.MOUSE_BUTTON, button));
                    editButton = null;
                }
            }
        });

        addKeyListener(new KeyListener() {
            @Override
            public void key(UIDisplayElement element, KeyEvent event) {
                if (editButton != null) {
                    changeButton(editButton, new Input(InputType.KEY, event.getKey()));
                    editButton = null;
                }
            }
        });

        ClickListener editButtonClick = new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                if (editButton == null) {
                    editButton = (UIButton) element;
                    editButtonCurrent = editButton.getLabel().getText();
                    editButton.getLabel().setText("...");
                }
            }
        };

        title = new UIImage(Assets.getTexture("engine:terasology"));
        title.setSize(new Vector2f(512f, 128f));
        title.setHorizontalAlign(EHorizontalAlign.CENTER);
        title.setPosition(new Vector2f(0f, 28f));
        title.setVisible(true);

        subtitle = new UILabel("Control Settings", Color.black);
        subtitle.setHorizontalAlign(EHorizontalAlign.CENTER);
        subtitle.setPosition(new Vector2f(0f, 128f));
        subtitle.setVisible(true);

        backToConfigMenuButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        backToConfigMenuButton.getLabel().setText("Back");
        backToConfigMenuButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        backToConfigMenuButton.setPosition(new Vector2f(200f, 570f));
        backToConfigMenuButton.setVisible(true);
        backToConfigMenuButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                getGUIManager().openWindow("config");
            }
        });

        GridLayout buttonGroupLayout = new GridLayout(2);
        buttonGroupLayout.setCellPadding(new Vector4f(5f, 5f, 5f, 5f));
        buttonGroupLayout.setVerticalCellAlign(EVerticalAlign.CENTER);

        for (int i = 0; i < buttonGroups.length; ++i) {
            buttonGroups[i] = new UIComposite();
            buttonGroups[i].setLayout(buttonGroupLayout);
            buttonGroups[i].setVisible(true);
        }

        for (ButtonDefinition def : buttonDefs) {
            UIButton button = new UIButton(new Vector2f(96f, 32f), UIButton.ButtonType.NORMAL);
            button.setUserData(def.bindId);
            button.addClickListener(editButtonClick);
            button.setVisible(true);
            inputButtons.add(button);
            UILabel label = new UILabel(def.displayText, Color.black);
            label.setVisible(true);
            inputLabels.add(label);

            buttonGroups[def.group].addDisplayElement(label);
            buttonGroups[def.group].addDisplayElement(button);
        }

        mouseSensitivity = new UISlider(new Vector2f(256f, 32f), 20, 1000);
        mouseSensitivity.setHorizontalAlign(EHorizontalAlign.CENTER);
        mouseSensitivity.setPosition(new Vector2f(-245f, 570f));
        mouseSensitivity.setVisible(true);
        mouseSensitivity.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                UISlider slider = (UISlider) element;
                slider.setText("Mouse Sensitivity: " + String.valueOf(slider.getValue()));
                config.getInput().setMouseSensitivity((float) slider.getValue() / 1000f);
            }
        });
        mouseSensitivity.setValue((int) (config.getInput().getMouseSensitivity() * 1000));
        defaultButton = new UIButton(new Vector2f(128f, 32f), UIButton.ButtonType.NORMAL);
        defaultButton.getLabel().setText("Default");
        defaultButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        defaultButton.setPosition(new Vector2f(-30f, 570f));
        defaultButton.setVisible(true);
        defaultButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                CoreRegistry.get(Config.class).getInputConfig().getBinds().setInputs(BindsConfig.createDefault());
                config.getInput().setMouseSensitivity(0.075f);

                setup();
                mouseSensitivity.setValue((int) (config.getInput().getMouseSensitivity() * 1000));
            }
        });

        GridLayout layout = new GridLayout(4);
        layout.setCellPadding(new Vector4f(0f, 20f, 0f, 20f));

        container = new UIComposite();
        container.setHorizontalAlign(EHorizontalAlign.CENTER);
        container.setPosition(new Vector2f(0f, 170f));
        container.setLayout(layout);
        container.setVisible(true);

        addDisplayElement(title);
        addDisplayElement(subtitle);
        addDisplayElement(container);
        for (UIComposite group : buttonGroups) {
            container.addDisplayElement(group);
        }

        addDisplayElement(mouseSensitivity);
        addDisplayElement(defaultButton);
        addDisplayElement(backToConfigMenuButton);

        setup();
    }

    private void changeButton(UIButton button, Input input) {
        String bindId = button.getUserData().toString();
        CoreRegistry.get(Config.class).getInputConfig().getBinds().setInputs(bindId, input);

        editButton.getLabel().setText(input.toShortString());
    }

    public void setup() {
        BindsConfig bindsConfig = CoreRegistry.get(Config.class).getInputConfig().getBinds();
        for (UIButton button : inputButtons) {
            String bindId = button.getUserData().toString();
            Collection<Input> inputs = bindsConfig.getInputs(bindId);
            if (inputs.size() > 0) {
                // TODO: Support multiple binds?
                button.getLabel().setText(inputs.iterator().next().toShortString());
            } else {
                button.getLabel().setText("");
            }
        }
    }
}
