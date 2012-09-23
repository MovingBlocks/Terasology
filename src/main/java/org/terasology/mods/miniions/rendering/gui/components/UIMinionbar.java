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
package org.terasology.mods.miniions.rendering.gui.components;

import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.GL11;
import org.terasology.asset.AssetManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.model.inventory.Icon;
import org.terasology.mods.miniions.components.MinionBarComponent;
import org.terasology.mods.miniions.components.MinionComponent;
import org.terasology.mods.miniions.components.MinionControllerComponent;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UILabel;

/**
 * A small toolbar placed at the right of the screen.
 *
 * @author Overdhose copied from toolbar
 */
public class UIMinionbar extends UIDisplayContainer {
    private final UIImage backgroundTexture;
    private final UIMinionbarCell[] cells;
    //private int prevSelected = 0;
    
    private class UIMinionbarCell extends UIDisplayContainer {

        private final UIImage selectionRectangle;
        private final UILabel label;

        private int id;
        private boolean selected = false;

        public UIMinionbarCell(int id) {
            this.id = id;

            setSize(new Vector2f(48f, 48f));

            selectionRectangle = new UIImage(AssetManager.loadTexture("engine:gui"));
            selectionRectangle.setTextureSize(new Vector2f(24f, 24f));
            selectionRectangle.setTextureOrigin(new Vector2f(0.0f, 24f));
            selectionRectangle.setSize(new Vector2f(48f, 48f));

            label = new UILabel();
            label.setVisible(true);
            label.setPosition(new Vector2f(30f, 20f));
            
            layout();
        }

        @Override
        public void update() {
            LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);

            selectionRectangle.setVisible(selected);
            setPosition(new Vector2f(2f, (getSize().y - 8f) * id - 2f));

            MinionBarComponent inventory = localPlayer.getEntity().getComponent(MinionBarComponent.class);
            if (inventory == null) {
                return;
            }
            if (inventory.minionSlots.size() > id) {
                MinionControllerComponent minionController = localPlayer.getEntity().getComponent(MinionControllerComponent.class);
                if (minionController != null) {
                    setSelected(minionController.selectedMinion == id);
                }
            }
        }
        
    	@Override
    	public void layout() {

    	}

        @Override
        public void render() {

            selectionRectangle.renderTransformed();

            MinionBarComponent inventory = CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(MinionBarComponent.class);
            if (inventory == null) {
                return;
            }
            if (inventory.minionSlots.size() <= id) {
                return;
            }
            EntityRef minionEntity = inventory.minionSlots.get(id);
            MinionComponent minion = minionEntity.getComponent(MinionComponent.class);
            if (minion == null) {
                return;
            }
            if (minion.icon.isEmpty()) {
                Icon icon = Icon.get("gelcube");
                if (icon != null) {
                    renderIcon(icon);
                }
            } else {
                Icon icon = Icon.get(minion.icon);
                if (icon != null) {
                    renderIcon(icon);
                }
            }

            label.renderTransformed();
        }

        private void renderIcon(Icon icon) {
            glEnable(GL11.GL_DEPTH_TEST);
            glClear(GL11.GL_DEPTH_BUFFER_BIT);
            glPushMatrix();
            glTranslatef(20f, 20f, 0f);
            icon.render();
            glPopMatrix();
            glDisable(GL11.GL_DEPTH_TEST);
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean getSelected() {
            return selected;
        }

        public UILabel getLabel() {
            return label;
        }
    }


    public UIMinionbar() {
        setSize(new Vector2f(44f, 364f));
        setHorizontalAlign(EHorizontalAlign.RIGHT);
        setVerticalAlign(EVerticalAlign.CENTER);

        backgroundTexture = new UIImage(AssetManager.loadTexture("engine:guiMinion"));
        backgroundTexture.setVisible(true);
        backgroundTexture.setTextureSize(new Vector2f(22f, 182f));
        backgroundTexture.setTextureOrigin(new Vector2f(0.0f, 0.0f));
        backgroundTexture.setSize(getSize());

        addDisplayElement(backgroundTexture);

        cells = new UIMinionbarCell[9];

        // Create the toolbar cells
        for (int i = 0; i < 9; i++) {
            cells[i] = new UIMinionbarCell(i);
            cells[i].setVisible(true);
            addDisplayElement(cells[i]);
        }
        
        //load init value of MinionControllerComponent here
        //MinionControllerComponent minionController = CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(MinionControllerComponent.class);
        //cells[minionController.selectedMinion].setSelected(true);
        //cells[0].setSelected(true);
        
        //CoreRegistry.get(EventSystem.class).registerEventHandler(this);
        
        layout();
    }
    
    /*
	@Override
	public void initialise() {

	}

	@Override
	public void shutdown() {

	}

    @ReceiveEvent(components = {LocalPlayerComponent.class, MinionControllerComponent.class})
    public void onMinionChanged(MinionChangedEvent event, EntityRef entity) {
    	MinionControllerComponent minionController = entity.getComponent(MinionControllerComponent.class);
    	if (minionController.selectedMinion != prevSelected) {
	        cells[minionController.selectedMinion].setSelected(true);
	        cells[prevSelected].setSelected(false);
	        prevSelected = minionController.selectedMinion;
	        
	        layout();
    	}
    }
    */
}
