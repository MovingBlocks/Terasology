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

import org.lwjgl.input.Keyboard;
import org.terasology.asset.AssetManager;
import org.terasology.input.events.KeyEvent;
import org.terasology.logic.manager.Config;
import org.terasology.logic.manager.GUIManager;
import org.terasology.logic.manager.InputConfig;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.layout.GridLayout;
import org.terasology.rendering.gui.widgets.UIButton;
import org.terasology.rendering.gui.widgets.UIComposite;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UISlider;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIWindow;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

/**
 * @author Overdhose
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         Date: 29/07/12
 */
public class UIMenuConfigControls extends UIWindow {

    String editButtonCurrent = "";
    UIButton editButton = null;
    final UIImage title;

    final UILabel ForwardButtontext,
            BackwardButtontext,
            AttackButtontext,
            ConsoleButtontext,
            CrouchButtontext,
            ActivateButtontext,   // (frob)
            HideguiButtontext,
            InventoryButtontext,
            JumpButtontext,
            LeftButtontext,
            MinionmodeButtontext,
            PauseButtontext,
            RightButtontext,
            RunButtontext,
            ToolnextButtontext,
            ToolprevButtontext,
            Toolslot1Buttontext,
            Toolslot2Buttontext,
            Toolslot3Buttontext,
            Toolslot4Buttontext,
            Toolslot5Buttontext,
            Toolslot6Buttontext,
            Toolslot7Buttontext,
            Toolslot8Buttontext,
            Toolslot9Buttontext,
            UsehelditemButtontext;

    private final UIButton _backToConfigMenuButton,
            ForwardButton,
            BackwardButton,
            RightButton,
            LeftButton,
            AttackButton,
            ConsoleButton,
            CrouchButton,
            ActivateButton,    // (frob)
            HideguiButton,
            InventoryButton,
            JumpButton,
            MinionmodeButton,
            PauseButton,
            RunButton,
            ToolnextButton,
            ToolprevButton,
            Toolslot1Button,
            Toolslot2Button,
            Toolslot3Button,
            Toolslot4Button,
            Toolslot5Button,
            Toolslot6Button,
            Toolslot7Button,
            Toolslot8Button,
            Toolslot9Button,
            UsehelditemButton,
            defaultButton;

    final UIComposite container;
    final UIComposite group1;
    final UIComposite group2;
    final UIComposite group3;
    final UIComposite group4;
    
    final UISlider MouseSensitivity;
    final UILabel subtitle;

    public UIMenuConfigControls() {
        setId("config:controls");
        setBackgroundImage("engine:loadingbackground");
        setModal(true);
        maximize();
        
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

        title = new UIImage(AssetManager.loadTexture("engine:terasology"));
        title.setSize(new Vector2f(512f, 128f));
        title.setHorizontalAlign(EHorizontalAlign.CENTER);
        title.setPosition(new Vector2f(0f, 28f));
        title.setVisible(true);

        subtitle = new UILabel("Control Settings");
        subtitle.setHorizontalAlign(EHorizontalAlign.CENTER);
        subtitle.setPosition(new Vector2f(0f, 128f));
        subtitle.setVisible(true);

        _backToConfigMenuButton = new UIButton(new Vector2f(128f, 32f), UIButton.eButtonType.NORMAL);
        _backToConfigMenuButton.getLabel().setText("Back");
        _backToConfigMenuButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        _backToConfigMenuButton.setPosition(new Vector2f(306f, 570f));
        _backToConfigMenuButton.setVisible(true);
        _backToConfigMenuButton.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
                GUIManager.getInstance().openWindow("config");
            }
        });

        ForwardButton = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        ForwardButton.addClickListener(editButtonClick);
        ForwardButton.setVisible(true);
        BackwardButton = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        BackwardButton.addClickListener(editButtonClick);
        BackwardButton.setVisible(true);
        RightButton = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        RightButton.addClickListener(editButtonClick);
        RightButton.setVisible(true);
        LeftButton = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        LeftButton.addClickListener(editButtonClick);
        LeftButton.setVisible(true);
        AttackButton = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        AttackButton.addClickListener(editButtonClick);
        AttackButton.setVisible(true);  
        ConsoleButton = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        ConsoleButton.addClickListener(editButtonClick);
        ConsoleButton.setVisible(true);
        CrouchButton = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        CrouchButton.addClickListener(editButtonClick);
        CrouchButton.setVisible(true);
        ActivateButton = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);    // (frob)
        ActivateButton.addClickListener(editButtonClick);
        ActivateButton.setVisible(true);
        HideguiButton = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        HideguiButton.addClickListener(editButtonClick);
        HideguiButton.setVisible(true);
        InventoryButton = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        InventoryButton.addClickListener(editButtonClick);
        InventoryButton.setVisible(true);
        JumpButton = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        JumpButton.addClickListener(editButtonClick);
        JumpButton.setVisible(true);
        MinionmodeButton = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        MinionmodeButton.addClickListener(editButtonClick);
        MinionmodeButton.setVisible(true);
        PauseButton = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        PauseButton.addClickListener(editButtonClick);
        PauseButton.setVisible(true);
        RunButton = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        RunButton.addClickListener(editButtonClick);
        RunButton.setVisible(true);
        ToolnextButton = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        ToolnextButton.addClickListener(editButtonClick);
        ToolnextButton.setVisible(true);
        ToolprevButton = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        ToolprevButton.addClickListener(editButtonClick);
        ToolprevButton.setVisible(true);
        Toolslot1Button = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        Toolslot1Button.addClickListener(editButtonClick);
        Toolslot1Button.setVisible(true);
        Toolslot2Button = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        Toolslot2Button.addClickListener(editButtonClick);
        Toolslot2Button.setVisible(true);
        Toolslot3Button = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        Toolslot3Button.addClickListener(editButtonClick);
        Toolslot3Button.setVisible(true);
        Toolslot4Button = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        Toolslot4Button.addClickListener(editButtonClick);
        Toolslot4Button.setVisible(true);
        Toolslot5Button = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        Toolslot5Button.addClickListener(editButtonClick);
        Toolslot5Button.setVisible(true);
        Toolslot6Button = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        Toolslot6Button.addClickListener(editButtonClick);
        Toolslot6Button.setVisible(true);
        Toolslot7Button = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        Toolslot7Button.addClickListener(editButtonClick);
        Toolslot7Button.setVisible(true);
        Toolslot8Button = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        Toolslot8Button.addClickListener(editButtonClick);
        Toolslot8Button.setVisible(true);
        Toolslot9Button = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        Toolslot9Button.addClickListener(editButtonClick);
        Toolslot9Button.setVisible(true);
        UsehelditemButton = new UIButton(new Vector2f(64f, 32f), UIButton.eButtonType.NORMAL);
        UsehelditemButton.addClickListener(editButtonClick);
        UsehelditemButton.setVisible(true);
        MouseSensitivity = new UISlider(new Vector2f(256f, 32f), 20, 150);
        MouseSensitivity.setHorizontalAlign(EHorizontalAlign.CENTER);
        MouseSensitivity.setPosition(new Vector2f(-245f, 570f));
        MouseSensitivity.setVisible(true);
        MouseSensitivity.addChangedListener(new ChangedListener() {
            @Override
            public void changed(UIDisplayElement element) {
                UISlider slider = (UISlider) element;
                slider.setText("Mouse Sensitivity: " + String.valueOf(slider.getValue()));
                Config.getInstance().setMouseSens((float)slider.getValue() / 1000f);
            }
        });
        MouseSensitivity.setValue((int) (Config.getInstance().getMouseSens() * 1000));
        defaultButton = new UIButton(new Vector2f(128f, 32f), UIButton.eButtonType.NORMAL);
        defaultButton.getLabel().setText("Default");
        defaultButton.setHorizontalAlign(EHorizontalAlign.CENTER);
        defaultButton.setPosition(new Vector2f(-30f, 570f));
        defaultButton.setVisible(true);
        defaultButton.addClickListener(new ClickListener() {    
            @Override
            public void click(UIDisplayElement element, int button) {
                InputConfig.getInstance().loadDefaultConfig();
                Config.getInstance().setMouseSens(0.075f);
                
                setup();
                MouseSensitivity.setValue((int) (Config.getInstance().getMouseSens() * 1000));
            }
        });

        //labels
        ForwardButtontext = new UILabel("Forward");
        ForwardButtontext.setVisible(true);
        BackwardButtontext = new UILabel("Backward");
        BackwardButtontext.setVisible(true);
        RightButtontext = new UILabel("Right");
        RightButtontext.setVisible(true);
        LeftButtontext = new UILabel("Left");
        LeftButtontext.setVisible(true);

        UsehelditemButtontext = new UILabel("Use Item");
        UsehelditemButtontext.setVisible(true);
        AttackButtontext = new UILabel("Attack");
        AttackButtontext.setVisible(true);
        ToolnextButtontext = new UILabel("Next Item");
        ToolnextButtontext.setVisible(true);
        ToolprevButtontext = new UILabel("Previous Item");
        ToolprevButtontext.setVisible(true);

        ActivateButtontext = new UILabel("Activate");    // (frob)
        ActivateButtontext.setVisible(true);            // (frob)
        JumpButtontext = new UILabel("Jump");
        JumpButtontext.setVisible(true);
        RunButtontext = new UILabel("Run");
        RunButtontext.setVisible(true);
        CrouchButtontext = new UILabel("Crouch");
        CrouchButtontext.setVisible(true);
        InventoryButtontext = new UILabel("Inventory");
        InventoryButtontext.setVisible(true);
        PauseButtontext = new UILabel("Pause");
        PauseButtontext.setVisible(true);
        HideguiButtontext = new UILabel("Hide HUD");
        HideguiButtontext.setVisible(true);
        MinionmodeButtontext = new UILabel("Minion mode");
        MinionmodeButtontext.setVisible(true);
        ConsoleButtontext = new UILabel("Console");
        ConsoleButtontext.setVisible(true);

        Toolslot1Buttontext = new UILabel("Hotkey 1");
        Toolslot1Buttontext.setVisible(true);
        Toolslot2Buttontext = new UILabel("Hotkey 2");
        Toolslot2Buttontext.setVisible(true);
        Toolslot3Buttontext = new UILabel("Hotkey 3");
        Toolslot3Buttontext.setVisible(true);
        Toolslot4Buttontext = new UILabel("Hotkey 4");
        Toolslot4Buttontext.setVisible(true);
        Toolslot5Buttontext = new UILabel("Hotkey 5");
        Toolslot5Buttontext.setVisible(true);
        Toolslot6Buttontext = new UILabel("Hotkey 6");
        Toolslot6Buttontext.setVisible(true);
        Toolslot7Buttontext = new UILabel("Hotkey 7");
        Toolslot7Buttontext.setVisible(true);
        Toolslot8Buttontext = new UILabel("Hotkey 8");
        Toolslot8Buttontext.setVisible(true);
        Toolslot9Buttontext = new UILabel("Hotkey 9");
        Toolslot9Buttontext.setVisible(true);
        
        GridLayout layout = new GridLayout(4);
        layout.setCellPadding(new Vector4f(0f, 20f, 0f, 20f));
        
        container = new UIComposite();
        container.setHorizontalAlign(EHorizontalAlign.CENTER);
        container.setPosition(new Vector2f(0f, 170f));
        container.setLayout(layout);
        container.setVisible(true);
        
        layout = new GridLayout(2);
        layout.setCellPadding(new Vector4f(5f, 5f, 5f, 5f));
        layout.setVerticalCellAlign(EVerticalAlign.CENTER);
        
        //group 1
        group1 = new UIComposite();
        group1.setLayout(layout);
        group1.setVisible(true);

        group1.addDisplayElement(ForwardButtontext);
        group1.addDisplayElement(ForwardButton);
        group1.addDisplayElement(BackwardButtontext);
        group1.addDisplayElement(BackwardButton);
        group1.addDisplayElement(LeftButtontext);
        group1.addDisplayElement(LeftButton);
        group1.addDisplayElement(RightButtontext);
        group1.addDisplayElement(RightButton);
        group1.addDisplayElement(AttackButtontext);
        group1.addDisplayElement(AttackButton);
        group1.addDisplayElement(UsehelditemButtontext);
        group1.addDisplayElement(UsehelditemButton);
        group1.addDisplayElement(ToolnextButtontext);
        group1.addDisplayElement(ToolnextButton);
        group1.addDisplayElement(ToolprevButtontext);
        group1.addDisplayElement(ToolprevButton);
        
        //group 2
        group2 = new UIComposite();
        group2.setLayout(layout);
        group2.setVisible(true);
        
        group2.addDisplayElement(ActivateButtontext);          // (frob)
        group2.addDisplayElement(ActivateButton);              // (frob)
        group2.addDisplayElement(InventoryButtontext);
        group2.addDisplayElement(InventoryButton);
        group2.addDisplayElement(JumpButtontext);
        group2.addDisplayElement(JumpButton);
        group2.addDisplayElement(RunButtontext);
        group2.addDisplayElement(RunButton);
        group2.addDisplayElement(CrouchButtontext);
        group2.addDisplayElement(CrouchButton);
        group2.addDisplayElement(PauseButtontext);
        group2.addDisplayElement(PauseButton);
        group2.addDisplayElement(ConsoleButtontext);
        group2.addDisplayElement(ConsoleButton);
        
        //group 3
        group3 = new UIComposite();
        group3.setLayout(layout);
        group3.setVisible(true);

        group3.addDisplayElement(HideguiButtontext);
        group3.addDisplayElement(HideguiButton);
        group3.addDisplayElement(MinionmodeButtontext);
        group3.addDisplayElement(MinionmodeButton);
        
        //group 3
        group4 = new UIComposite();
        group4.setLayout(layout);
        group4.setVisible(true);
 
        group4.addDisplayElement(Toolslot1Buttontext);
        group4.addDisplayElement(Toolslot1Button);
        group4.addDisplayElement(Toolslot2Buttontext);
        group4.addDisplayElement(Toolslot2Button);
        group4.addDisplayElement(Toolslot3Buttontext);
        group4.addDisplayElement(Toolslot3Button);
        group4.addDisplayElement(Toolslot4Buttontext);
        group4.addDisplayElement(Toolslot4Button);
        group4.addDisplayElement(Toolslot5Buttontext);
        group4.addDisplayElement(Toolslot5Button);
        group4.addDisplayElement(Toolslot6Buttontext);
        group4.addDisplayElement(Toolslot6Button);
        group4.addDisplayElement(Toolslot7Buttontext);
        group4.addDisplayElement(Toolslot7Button);
        group4.addDisplayElement(Toolslot8Buttontext);
        group4.addDisplayElement(Toolslot8Button);
        group4.addDisplayElement(Toolslot9Buttontext);
        group4.addDisplayElement(Toolslot9Button);

        addDisplayElement(title);
        addDisplayElement(subtitle);
        addDisplayElement(container);
        container.addDisplayElement(group1);
        container.addDisplayElement(group2);
        container.addDisplayElement(group3);
        container.addDisplayElement(group4);

        addDisplayElement(MouseSensitivity);
        addDisplayElement(defaultButton);
        addDisplayElement(_backToConfigMenuButton);
        
        setup();
    }
    
    @Override
    public void processKeyboardInput(KeyEvent event) {
        super.processKeyboardInput(event);
        
        if (editButton != null) {
            changeButton(editButton, event.getKey());
            editButton = null;
        }
    }

    @Override
    public boolean processMouseInput(int button, boolean state, int wheelMoved, boolean consumed) {
        if (editButton != null) {
            int key = -1;
            if (button == 0)
                key = 256;
            else if (button == 1)
                key = 257;
            else if (button == 2)
                key = 258;
            else if (wheelMoved > 0)
                key = 259;
            else if (wheelMoved < 0)
                key = 260;
            
            if (key != -1) {
                changeButton(editButton, key);
                editButton = null;
            }
        }
        
        consumed = super.processMouseInput(button, state, wheelMoved, consumed);
        
        return consumed;
    }
    
    private void changeButton(UIButton button, int key) {
        if (button == ForwardButton)
            InputConfig.getInstance().setKeyForward(key);
        else if (button == BackwardButton)
            InputConfig.getInstance().setKeyBackward(key);
        else if (button == RightButton)
            InputConfig.getInstance().setKeyRight(key);
        else if (button == LeftButton)
            InputConfig.getInstance().setKeyLeft(key);
        else if (button == AttackButton)
            InputConfig.getInstance().setKeyAttack(key);
        else if (button == ConsoleButton)
            InputConfig.getInstance().setKeyConsole(key);
        else if (button == CrouchButton)
            InputConfig.getInstance().setKeyCrouch(key);
        else if (button == ActivateButton)
            InputConfig.getInstance().setKeyForward(key);
        else if (button == HideguiButton)
            InputConfig.getInstance().setKeyHidegui(key);
        else if (button == InventoryButton)
            InputConfig.getInstance().setKeyInventory(key);
        else if (button == JumpButton)
            InputConfig.getInstance().setKeyJump(key);
        else if (button == MinionmodeButton)
            InputConfig.getInstance().setKeyMinionmode(key);
        else if (button == PauseButton)
            InputConfig.getInstance().setKeyPause(key);
        else if (button == RunButton)
            InputConfig.getInstance().setKeyRun(key);
        else if (button == ToolnextButton)
            InputConfig.getInstance().setKeyToolnext(key);
        else if (button == ToolprevButton)
            InputConfig.getInstance().setKeyToolprev(key);
        else if (button == Toolslot1Button)
            InputConfig.getInstance().setKeyToolslot1(key);
        else if (button == Toolslot2Button)
            InputConfig.getInstance().setKeyToolslot2(key);
        else if (button == Toolslot3Button)
            InputConfig.getInstance().setKeyToolslot3(key);
        else if (button == Toolslot4Button)
            InputConfig.getInstance().setKeyToolslot4(key);
        else if (button == Toolslot5Button)
            InputConfig.getInstance().setKeyToolslot5(key);
        else if (button == Toolslot6Button)
            InputConfig.getInstance().setKeyToolslot6(key);
        else if (button == Toolslot7Button)
            InputConfig.getInstance().setKeyToolslot7(key);
        else if (button == Toolslot8Button)
            InputConfig.getInstance().setKeyToolslot8(key);
        else if (button == Toolslot9Button)
            InputConfig.getInstance().setKeyToolslot9(key);
        else if (button == UsehelditemButton)
            InputConfig.getInstance().setKeyUsehelditem(key);
        else {
            editButton.getLabel().setText(editButtonCurrent);
            return;
        }
        
        editButton.getLabel().setText(keyToStrShort(key));
    }
 
    private String keyToStrShort(int key) {
        if (key < 256) {
            //replace the names of the buttons which are to long with shorter names here.. i am just to lazy :D
            return Keyboard.getKeyName(key);
        } else {
            if (key == 256) {
                return "ML";
            } else if (key == 257) {
                return "MR";
            } else if (key == 258) {
                return "MM";
            } else if (key == 259) {
                return "UP";
            } else if (key == 260) {
                return "DOWN";
            } else {
                return "MOVE";
            }
        }
    }

    public void setup() {
        ForwardButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyForward()));
        BackwardButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyBackward()));
        RightButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyRight()));
        LeftButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyLeft()));
        AttackButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyAttack()));
        ConsoleButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyConsole()));
        CrouchButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyCrouch()));
        ActivateButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyFrob()));
        HideguiButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyHidegui()));
        InventoryButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyInventory()));
        JumpButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyJump()));
        MinionmodeButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyMinionmode()));
        PauseButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyPause()));
        RunButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyRun()));
        ToolnextButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolnext()));
        ToolprevButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolprev()));
        Toolslot1Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot1()));
        Toolslot2Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot2()));
        Toolslot3Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot3()));
        Toolslot4Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot4()));
        Toolslot5Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot5()));
        Toolslot6Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot6()));
        Toolslot7Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot7()));
        Toolslot8Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot8()));
        Toolslot9Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot9()));
        UsehelditemButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyUsehelditem()));
    }
}
