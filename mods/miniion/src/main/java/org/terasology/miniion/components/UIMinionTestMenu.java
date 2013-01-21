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
package org.terasology.miniion.components;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.Display;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.GUIManager;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIItemCell;
import org.terasology.rendering.gui.widgets.UIWindow;

/**
 * Created with IntelliJ IDEA.
 * User: Overdhose
 * Date: 8/05/12
 * Time: 20:25
 * used as a dial menu without ungrabbing the mouse
 */
public class UIMinionTestMenu extends UIWindow {

    //private UIButton buttonMove;
    private final UIImage background;
    private final UIImage selectionrectangle;

    public UIMinionTestMenu() {
        setId("minionTest");
        setModal(true);

        addMouseButtonListener(new MouseButtonListener() {

            @Override
            public void wheel(UIDisplayElement element, int wheel, boolean intersect) {

            }

            @Override
            public void up(UIDisplayElement element, int button, boolean intersect) {
            	 if (button == 1) {
            		 close(); 
                 }
            }

            @Override
            public void down(UIDisplayElement element, int button, boolean intersect) {
               
            }
        });
        
        setSize(new Vector2f(60f, 180f));
        background = new UIImage(Assets.getTexture("engine:guiMinion"));
        background.getTextureSize().set(new Vector2f(60f / 256f, 180f / 256f));
        background.getTextureOrigin().set(new Vector2f(30.0f / 256f, 20.0f / 256f));
        background.setSize(getSize());
        addDisplayElement(background);
        background.setVisible(true);

        selectionrectangle = new UIImage(Assets.getTexture("engine:guiMinion"));
        selectionrectangle.getTextureSize().set(new Vector2f(60f / 256f, 20f / 256f));
        selectionrectangle.getTextureOrigin().set(new Vector2f(30f / 256, 0.0f));
        selectionrectangle.setSize(new Vector2f(60f, 20f));
        selectionrectangle.setVisible(true);
        addDisplayElement(selectionrectangle);

        update();
    }

    @Override
    public void update() {
    	super.update();
    	
        LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
        if (localPlayer != null) {
            MinionBarComponent inventory = localPlayer.getEntity().getComponent(MinionBarComponent.class);
            if (inventory == null) {
                return;
            }
            
            MinionControllerComponent minionController = localPlayer.getEntity().getComponent(MinionControllerComponent.class);
            if (minionController != null) {
	            int selectedMinion = minionController.selectedMinion;
	            EntityRef minion = inventory.minionSlots.get(selectedMinion);
	            if (minion != null) {
	                MinionComponent minioncomp = minion.getComponent(MinionComponent.class);
	                if (minioncomp != null) {
	                    int selection = 20 * (minioncomp.minionBehaviour.ordinal());
	                    int startpos = (44 * (6 - (selectedMinion + 1)));
	                    selectionrectangle.setPosition(new Vector2f(Display.getWidth() - (300), (Display.getHeight() / 3) - startpos + selection));
	                    //setPosition(new Vector2f(2f, (getSize().y - 8f) * selectedMinion - 2f));
	                    background.setPosition(new Vector2f(Display.getWidth() - (300), (Display.getHeight() / 3) - startpos)); //(25 *(6-(selectedMinion+1)))
	                }
	            }
            }
        }
    }
}
