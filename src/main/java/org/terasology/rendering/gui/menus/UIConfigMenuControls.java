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
package org.terasology.rendering.gui.menus;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.terasology.asset.AssetManager;
import org.terasology.logic.manager.InputConfig;
import org.terasology.rendering.gui.components.UIButton;
import org.terasology.rendering.gui.components.UIImageOverlay;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIDisplayWindow;
import org.terasology.rendering.gui.framework.UIGraphicsElement;
import org.terasology.rendering.gui.framework.events.IClickListener;

import javax.vecmath.Vector2f;

/**
 * @author Overdhose
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 *         Date: 29/07/12
 */
public class UIConfigMenuControls extends UIDisplayWindow {

	String editButtonCurrent = "";
	UIButton editButton = null;
    final UIImageOverlay overlay;
    final UIGraphicsElement title;

    final UIText ForwardButtontext,
            BackwardButtontext,
            JumpbehaviourButtontext,
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
            JumpbehaviourButton,
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

    final UIText subtitle;

    public UIConfigMenuControls() {
        maximize();
        
        IClickListener editButtonClick = new IClickListener() {
			@Override
			public void click(UIDisplayElement element, int button) {
				if (editButton == null) {
					editButton = (UIButton) element;
					editButtonCurrent = editButton.getLabel().getText();
					editButton.getLabel().setText("...");
				}
			}
		};

        title = new UIGraphicsElement(AssetManager.loadTexture("engine:terasology"));
        title.setVisible(true);
        title.setSize(new Vector2f(512f, 128f));

        subtitle = new UIText("Control Settings");
        subtitle.setVisible(true);

        overlay = new UIImageOverlay(AssetManager.loadTexture("engine:loadingBackground"));
        overlay.setVisible(true);

        _backToConfigMenuButton = new UIButton(new Vector2f(256f, 32f));
        _backToConfigMenuButton.getLabel().setText("Back");
        _backToConfigMenuButton.setVisible(true);

        ForwardButton = new UIButton(new Vector2f(64f, 32f));
        ForwardButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyForward()));
		ForwardButton.addClickListener(editButtonClick);
        ForwardButton.setVisible(true);
        BackwardButton = new UIButton(new Vector2f(64f, 32f));
        BackwardButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyBackward()));
        BackwardButton.addClickListener(editButtonClick);
        BackwardButton.setVisible(true);
        RightButton = new UIButton(new Vector2f(64f, 32f));
        RightButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyRight()));
        RightButton.addClickListener(editButtonClick);
        RightButton.setVisible(true);
        LeftButton = new UIButton(new Vector2f(64f, 32f));
        LeftButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyLeft()));
        LeftButton.addClickListener(editButtonClick);
        LeftButton.setVisible(true);
        JumpbehaviourButton = new UIButton(new Vector2f(64f, 32f));
        JumpbehaviourButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyJumpbehaviour()));
        JumpbehaviourButton.addClickListener(editButtonClick);
        JumpbehaviourButton.setVisible(true);
        AttackButton = new UIButton(new Vector2f(64f, 32f));
        AttackButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyAttack()));
        AttackButton.addClickListener(editButtonClick);
        AttackButton.setVisible(true);
        ConsoleButton = new UIButton(new Vector2f(64f, 32f));
        ConsoleButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyConsole()));
        ConsoleButton.addClickListener(editButtonClick);
        ConsoleButton.setVisible(true);
        CrouchButton = new UIButton(new Vector2f(64f, 32f));
        CrouchButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyCrouch()));
        CrouchButton.addClickListener(editButtonClick);
        CrouchButton.setVisible(true);
        ActivateButton = new UIButton(new Vector2f(64f, 32f));    // (frob)
        ActivateButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyFrob()));
        ActivateButton.addClickListener(editButtonClick);
        ActivateButton.setVisible(true);
        HideguiButton = new UIButton(new Vector2f(64f, 32f));
        HideguiButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyHidegui()));
        HideguiButton.addClickListener(editButtonClick);
        HideguiButton.setVisible(true);
        InventoryButton = new UIButton(new Vector2f(64f, 32f));
        InventoryButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyInventory()));
        InventoryButton.addClickListener(editButtonClick);
        InventoryButton.setVisible(true);
        JumpButton = new UIButton(new Vector2f(64f, 32f));
        JumpButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyJump()));
        JumpButton.addClickListener(editButtonClick);
        JumpButton.setVisible(true);
        MinionmodeButton = new UIButton(new Vector2f(64f, 32f));
        MinionmodeButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyMinionmode()));
        MinionmodeButton.addClickListener(editButtonClick);
        MinionmodeButton.setVisible(true);
        PauseButton = new UIButton(new Vector2f(64f, 32f));
        PauseButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyPause()));
        PauseButton.addClickListener(editButtonClick);
        PauseButton.setVisible(true);
        RunButton = new UIButton(new Vector2f(64f, 32f));
        RunButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyRun()));
        RunButton.addClickListener(editButtonClick);
        RunButton.setVisible(true);
        ToolnextButton = new UIButton(new Vector2f(64f, 32f));
        ToolnextButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolnext()));
        ToolnextButton.addClickListener(editButtonClick);
        ToolnextButton.setVisible(true);
        ToolprevButton = new UIButton(new Vector2f(64f, 32f));
        ToolprevButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolprev()));
        ToolprevButton.addClickListener(editButtonClick);
        ToolprevButton.setVisible(true);
        Toolslot1Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot1Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot1()));
        Toolslot1Button.addClickListener(editButtonClick);
        Toolslot1Button.setVisible(true);
        Toolslot2Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot2Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot2()));
        Toolslot2Button.addClickListener(editButtonClick);
        Toolslot2Button.setVisible(true);
        Toolslot3Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot3Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot3()));
        Toolslot3Button.addClickListener(editButtonClick);
        Toolslot3Button.setVisible(true);
        Toolslot4Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot4Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot4()));
        Toolslot4Button.addClickListener(editButtonClick);
        Toolslot4Button.setVisible(true);
        Toolslot5Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot5Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot5()));
        Toolslot5Button.addClickListener(editButtonClick);
        Toolslot5Button.setVisible(true);
        Toolslot6Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot6Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot6()));
        Toolslot6Button.addClickListener(editButtonClick);
        Toolslot6Button.setVisible(true);
        Toolslot7Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot7Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot7()));
        Toolslot7Button.addClickListener(editButtonClick);
        Toolslot7Button.setVisible(true);
        Toolslot8Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot8Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot8()));
        Toolslot8Button.addClickListener(editButtonClick);
        Toolslot8Button.setVisible(true);
        Toolslot9Button = new UIButton(new Vector2f(64f, 32f));
        Toolslot9Button.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyToolslot9()));
        Toolslot9Button.addClickListener(editButtonClick);
        Toolslot9Button.setVisible(true);
        UsehelditemButton = new UIButton(new Vector2f(64f, 32f));
        UsehelditemButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyUsehelditem()));
        UsehelditemButton.addClickListener(editButtonClick);
        UsehelditemButton.setVisible(true);
        defaultButton = new UIButton(new Vector2f(80f, 32f));
        defaultButton.getLabel().setText("Default");
        defaultButton.setVisible(true);
        defaultButton.addClickListener(new IClickListener() {	
			@Override
			public void click(UIDisplayElement element, int button) {
				InputConfig.getInstance().loadDefaultConfig();
				
		        ForwardButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyForward()));
		        BackwardButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyBackward()));
		        RightButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyRight()));
		        LeftButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyLeft()));
		        JumpbehaviourButton.getLabel().setText(keyToStrShort(InputConfig.getInstance().getKeyJumpbehaviour()));
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
		});

        ForwardButtontext = new UIText("Forward");
        BackwardButtontext = new UIText("Backward");
        RightButtontext = new UIText("Right");
        LeftButtontext = new UIText("Left");

        UsehelditemButtontext = new UIText("Use Item");
        AttackButtontext = new UIText("Attack");
        ToolnextButtontext = new UIText("Next Item");
        ToolprevButtontext = new UIText("Previous Item");

        ActivateButtontext = new UIText("Activate");   // (frob)
        JumpButtontext = new UIText("Jump");
        RunButtontext = new UIText("Run");
        CrouchButtontext = new UIText("Crouch");
        InventoryButtontext = new UIText("Inventory");
        PauseButtontext = new UIText("Pause");
        HideguiButtontext = new UIText("Hide HUD");
        MinionmodeButtontext = new UIText("Minion mode");
        JumpbehaviourButtontext = new UIText("Jump behaviour");
        ConsoleButtontext = new UIText("Console");

        Toolslot1Buttontext = new UIText("Hotkey 1");
        Toolslot2Buttontext = new UIText("Hotkey 2");
        Toolslot3Buttontext = new UIText("Hotkey 3");
        Toolslot4Buttontext = new UIText("Hotkey 4");
        Toolslot5Buttontext = new UIText("Hotkey 5");
        Toolslot6Buttontext = new UIText("Hotkey 6");
        Toolslot7Buttontext = new UIText("Hotkey 7");
        Toolslot8Buttontext = new UIText("Hotkey 8");
        Toolslot9Buttontext = new UIText("Hotkey 9");

        ForwardButtontext.setVisible(true);
        BackwardButtontext.setVisible(true);
        RightButtontext.setVisible(true);
        LeftButtontext.setVisible(true);
        JumpbehaviourButtontext.setVisible(true);
        AttackButtontext.setVisible(true);
        ConsoleButtontext.setVisible(true);
        CrouchButtontext.setVisible(true);
        ActivateButtontext.setVisible(true);   // (frob)
        HideguiButtontext.setVisible(true);
        InventoryButtontext.setVisible(true);
        JumpButtontext.setVisible(true);
        MinionmodeButtontext.setVisible(true);
        PauseButtontext.setVisible(true);
        RunButtontext.setVisible(true);
        ToolnextButtontext.setVisible(true);
        ToolprevButtontext.setVisible(true);
        Toolslot1Buttontext.setVisible(true);
        Toolslot2Buttontext.setVisible(true);
        Toolslot3Buttontext.setVisible(true);
        Toolslot4Buttontext.setVisible(true);
        Toolslot5Buttontext.setVisible(true);
        Toolslot6Buttontext.setVisible(true);
        Toolslot7Buttontext.setVisible(true);
        Toolslot8Buttontext.setVisible(true);
        Toolslot9Buttontext.setVisible(true);
        UsehelditemButtontext.setVisible(true);

        addDisplayElement(overlay);
        addDisplayElement(title);
        addDisplayElement(subtitle);

        addDisplayElement(ForwardButtontext);
        addDisplayElement(BackwardButtontext);
        addDisplayElement(JumpbehaviourButtontext);
        addDisplayElement(AttackButtontext);
        addDisplayElement(ConsoleButtontext);
        addDisplayElement(CrouchButtontext);
        addDisplayElement(ActivateButtontext);   // (frob)
        addDisplayElement(HideguiButtontext);
        addDisplayElement(InventoryButtontext);
        addDisplayElement(JumpButtontext);
        addDisplayElement(LeftButtontext);
        addDisplayElement(MinionmodeButtontext);
        addDisplayElement(PauseButtontext);
        addDisplayElement(RightButtontext);
        addDisplayElement(RunButtontext);
        addDisplayElement(ToolnextButtontext);
        addDisplayElement(ToolprevButtontext);
        addDisplayElement(Toolslot1Buttontext);
        addDisplayElement(Toolslot2Buttontext);
        addDisplayElement(Toolslot3Buttontext);
        addDisplayElement(Toolslot4Buttontext);
        addDisplayElement(Toolslot5Buttontext);
        addDisplayElement(Toolslot6Buttontext);
        addDisplayElement(Toolslot7Buttontext);
        addDisplayElement(Toolslot8Buttontext);
        addDisplayElement(Toolslot9Buttontext);
        addDisplayElement(UsehelditemButtontext);

        addDisplayElement(_backToConfigMenuButton, "backToConfigMenuButton");
        addDisplayElement(ForwardButton, "ForwardButton");
        addDisplayElement(BackwardButton, "BackwardButton");
        addDisplayElement(RightButton, "RightButton");
        addDisplayElement(LeftButton, "LeftButton");
        addDisplayElement(JumpbehaviourButton, "JumpbehaviourButton");
        addDisplayElement(AttackButton, "AttackButton");
        addDisplayElement(ConsoleButton, "ConsoleButton");
        addDisplayElement(CrouchButton, "CrouchButton");
        addDisplayElement(ActivateButton, "ActivateButton");    // (frob)
        addDisplayElement(HideguiButton, "HideguiButton");
        addDisplayElement(InventoryButton, "InventoryButton");
        addDisplayElement(JumpButton, "JumpButton");
        addDisplayElement(MinionmodeButton, "MinionmodeButton");
        addDisplayElement(PauseButton, "PauzeButton");
        addDisplayElement(RunButton, "RunButton");
        addDisplayElement(ToolnextButton, "ToolnextButton");
        addDisplayElement(ToolprevButton, "ToolprevButton");
        addDisplayElement(Toolslot1Button, "Toolslot1Button");
        addDisplayElement(Toolslot2Button, "Toolslot2Button");
        addDisplayElement(Toolslot3Button, "Toolslot3Button");
        addDisplayElement(Toolslot4Button, "Toolslot4Button");
        addDisplayElement(Toolslot5Button, "Toolslot5Button");
        addDisplayElement(Toolslot6Button, "Toolslot6Button");
        addDisplayElement(Toolslot7Button, "Toolslot7Button");
        addDisplayElement(Toolslot8Button, "Toolslot8Button");
        addDisplayElement(Toolslot9Button, "Toolslot9Button");
        addDisplayElement(UsehelditemButton, "UsehelditemButton");
        addDisplayElement(defaultButton, "defaultButton");
        
        layout();
    }
    
    @Override
    public void processKeyboardInput(int key) {
    	if (editButton != null) {
    		changeButton(editButton, key);
    		editButton = null;
    	}
    	
    	super.processKeyboardInput(key);
    }

	@Override
    public void processMouseInput(int button, boolean state, int wheelMoved) {
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
    	
    	super.processMouseInput(button, state, wheelMoved);
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
		else if (button == JumpbehaviourButton)
			InputConfig.getInstance().setKeyJumpbehaviour(key);
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

    @Override
    public void layout() {
        super.layout();

        if (subtitle != null) {
	        float center = Display.getWidth() / 2;
	        float rowWidth = 110;
	        float marginTextTop = 7;
	        
	        subtitle.centerHorizontally();
	        subtitle.getPosition().y = 130f;
	
	        //row 1
	        ForwardButtontext.getPosition().x = center - 4 * rowWidth;
	        ForwardButtontext.getPosition().y = 200f + marginTextTop;
	        ForwardButton.getPosition().x = center - 3 * rowWidth;
	        ForwardButton.getPosition().y = 200f;
	
	        BackwardButtontext.getPosition().x = center - 4 * rowWidth;
	        BackwardButtontext.getPosition().y = 200f + 40f + marginTextTop;
	        BackwardButton.getPosition().x = center - 3 * rowWidth;
	        BackwardButton.getPosition().y = 200f + 40f;
	
	        LeftButtontext.getPosition().x = center - 4 * rowWidth;
	        LeftButtontext.getPosition().y = 200f + 2 * 40f + marginTextTop;
	        LeftButton.getPosition().x = center - 3 * rowWidth;
	        LeftButton.getPosition().y = 200f + 2 * 40f;
	
	        RightButtontext.getPosition().x = center - 4 * rowWidth;
	        RightButtontext.getPosition().y = 200f + 3 * 40f + marginTextTop;
	        RightButton.getPosition().x = center - 3 * rowWidth;
	        RightButton.getPosition().y = 200f + 3 * 40f;
	
	
	        AttackButtontext.getPosition().x = center - 4 * rowWidth;
	        AttackButtontext.getPosition().y = 200f + 5 * 40f + marginTextTop;
	        AttackButton.getPosition().x = center - 3 * rowWidth;
	        AttackButton.getPosition().y = 200f + 5 * 40f;
	
	        UsehelditemButtontext.getPosition().x = center - 4 * rowWidth;
	        UsehelditemButtontext.getPosition().y = 200f + 6 * 40f + marginTextTop;
	        UsehelditemButton.getPosition().x = center - 3 * rowWidth;
	        UsehelditemButton.getPosition().y = 200f + 6 * 40f;
	
	        ToolnextButtontext.getPosition().x = center - 4 * rowWidth;
	        ToolnextButtontext.getPosition().y = 200f + 7 * 40f + marginTextTop;
	        ToolnextButton.getPosition().x = center - 3 * rowWidth;
	        ToolnextButton.getPosition().y = 200f + 7 * 40f;
	
	        ToolprevButtontext.getPosition().x = center - 4 * rowWidth;
	        ToolprevButtontext.getPosition().y = 200f + 8 * 40f + marginTextTop;
	        ToolprevButton.getPosition().x = center - 3 * rowWidth;
	        ToolprevButton.getPosition().y = 200f + 8 * 40f;
	        
	        defaultButton.getPosition().x = center - 4 * rowWidth;
	        defaultButton.getPosition().y = 300f + 7 * 40f;
	
	        //row 2
	        ActivateButtontext.getPosition().x = center - 2 * rowWidth;   // (frob)
	        ActivateButtontext.getPosition().y = 200f + marginTextTop;   // (frob)
	        ActivateButton.getPosition().x = center - rowWidth;    // (frob)
	        ActivateButton.getPosition().y = 200f;    // (frob)
	
	        InventoryButtontext.getPosition().x = center - 2 * rowWidth;
	        InventoryButtontext.getPosition().y = 200f + 40f + marginTextTop;
	        InventoryButton.getPosition().x = center - rowWidth;
	        InventoryButton.getPosition().y = 200f + 40f;
	
	        JumpButtontext.getPosition().x = center - 2 * rowWidth;
	        JumpButtontext.getPosition().y = 200f + 3 * 40f + marginTextTop;
	        JumpButton.getPosition().x = center - rowWidth;
	        JumpButton.getPosition().y = 200f + 3 * 40f;
	
	        RunButtontext.getPosition().x = center - 2 * rowWidth;
	        RunButtontext.getPosition().y = 200f + 4 * 40f + marginTextTop;
	        RunButton.getPosition().x = center - rowWidth;
	        RunButton.getPosition().y = 200f + 4 * 40f;
	
	        CrouchButtontext.getPosition().x = center - 2 * rowWidth;
	        CrouchButtontext.getPosition().y = 200f + 5 * 40f + marginTextTop;
	        CrouchButton.getPosition().x = center - rowWidth;
	        CrouchButton.getPosition().y = 200f + 5 * 40f;
	
	        PauseButtontext.getPosition().x = center - 2 * rowWidth;
	        PauseButtontext.getPosition().y = 200f + 7 * 40f + marginTextTop;
	        PauseButton.getPosition().x = center - rowWidth;
	        PauseButton.getPosition().y = 200f + 7 * 40f;
	
	        ConsoleButtontext.getPosition().x = center - 2 * rowWidth;
	        ConsoleButtontext.getPosition().y = 200f + 8 * 40f + marginTextTop;
	        ConsoleButton.getPosition().x = center - rowWidth;
	        ConsoleButton.getPosition().y = 200f + 8 * 40f;
	        
	        //row 3
	        HideguiButtontext.getPosition().x = center;
	        HideguiButtontext.getPosition().y = 200f + marginTextTop;
	        HideguiButton.getPosition().x = center + rowWidth;
	        HideguiButton.getPosition().y = 200f;
	
	        JumpbehaviourButtontext.getPosition().x = center;
	        JumpbehaviourButtontext.getPosition().y = 200f + 40f + marginTextTop;
	        JumpbehaviourButton.getPosition().x = center + rowWidth;
	        JumpbehaviourButton.getPosition().y = 200f + 40f;
	
	        MinionmodeButtontext.getPosition().x = center;
	        MinionmodeButtontext.getPosition().y = 200f + 2 * 40f + marginTextTop;
	        MinionmodeButton.getPosition().x = center + rowWidth;
	        MinionmodeButton.getPosition().y = 200f + 2 * 40f;
	        
	        //row 4
	        Toolslot1Buttontext.getPosition().x = center + 2 * rowWidth;
	        Toolslot1Buttontext.getPosition().y = 200f + marginTextTop;
	        Toolslot1Button.getPosition().x = center + 3 * rowWidth;
	        Toolslot1Button.getPosition().y = 200f;
	        
	        Toolslot2Buttontext.getPosition().x = center + 2 * rowWidth;
	        Toolslot2Buttontext.getPosition().y = 200f + 40f + marginTextTop;
	        Toolslot2Button.getPosition().x = center + 3 * rowWidth;
	        Toolslot2Button.getPosition().y = 200f + 40f;
	        
	        Toolslot3Buttontext.getPosition().x = center + 2 * rowWidth;
	        Toolslot3Buttontext.getPosition().y = 200f + 2 * 40f + marginTextTop;
	        Toolslot3Button.getPosition().x = center + 3 * rowWidth;
	        Toolslot3Button.getPosition().y = 200f + 2 * 40f;
	        
	        Toolslot4Buttontext.getPosition().x = center + 2 * rowWidth;
	        Toolslot4Buttontext.getPosition().y = 200f + 3 * 40f + marginTextTop;
	        Toolslot4Button.getPosition().x = center + 3 * rowWidth;
	        Toolslot4Button.getPosition().y = 200f + 3 * 40f;
	        
	        Toolslot5Buttontext.getPosition().x = center + 2 * rowWidth;
	        Toolslot5Buttontext.getPosition().y = 200f + 4 * 40f + marginTextTop;
	        Toolslot5Button.getPosition().x = center + 3 * rowWidth;
	        Toolslot5Button.getPosition().y = 200f + 4 * 40f;
	        
	        Toolslot6Buttontext.getPosition().x = center + 2 * rowWidth;
	        Toolslot6Buttontext.getPosition().y = 200f + 5 * 40f + marginTextTop;
	        Toolslot6Button.getPosition().x = center + 3 * rowWidth;
	        Toolslot6Button.getPosition().y = 200f + 5 * 40f;
	        
	        Toolslot7Buttontext.getPosition().x = center + 2 * rowWidth;
	        Toolslot7Buttontext.getPosition().y = 200f + 6 * 40f + marginTextTop;
	        Toolslot7Button.getPosition().x = center + 3 * rowWidth;
	        Toolslot7Button.getPosition().y = 200f + 6 * 40f;
	        
	        Toolslot8Buttontext.getPosition().x = center + 2 * rowWidth;
	        Toolslot8Buttontext.getPosition().y = 200f + 7 * 40f + marginTextTop;
	        Toolslot8Button.getPosition().x = center + 3 * rowWidth;
	        Toolslot8Button.getPosition().y = 200f + 7 * 40f;
	        
	        Toolslot9Buttontext.getPosition().x = center + 2 * rowWidth;
	        Toolslot9Buttontext.getPosition().y = 200f + 8 * 40f + marginTextTop;
	        Toolslot9Button.getPosition().x = center + 3 * rowWidth;
	        Toolslot9Button.getPosition().y = 200f + 8 * 40f;
	
	        
	        _backToConfigMenuButton.centerHorizontally();
	        _backToConfigMenuButton.getPosition().y = 300f + 7 * 40f;
	
	        title.centerHorizontally();
	        title.getPosition().y = 28f;
        }
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
}
