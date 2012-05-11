/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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
package org.terasology.rendering.gui.components;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.components.BlockItemComponent;
import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.AssetManager;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockFamily;
import org.terasology.model.inventory.Icon;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

import static org.lwjgl.opengl.GL11.*;

/**
 * A single cell of the toolbar with a small text label and a selection
 * rectangle.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
// TODO: Combine with UIToolbarCell?
public class UIInventoryCell extends UIDisplayElement {

    private final UIGraphicsElement _selectionRectangle;
    private final UIText _label;
    private final UIText _label2;

    private int _id;
    private boolean _selected = false;
    private Texture terrainTex;

    public UIInventoryCell(int id) {
        _id = id;

        setSize(new Vector2f(48f, 48f));
        terrainTex = AssetManager.loadTexture("engine:terrain");

        _selectionRectangle = new UIGraphicsElement(AssetManager.loadTexture("engine:gui"));
        _selectionRectangle.getTextureSize().set(new Vector2f(24f / 256f, 24f / 256f));
        _selectionRectangle.getTextureOrigin().set(new Vector2f(0.0f, 23f / 256f));
        _selectionRectangle.setSize(getSize());

        _label = new UIText();
        _label.setVisible(true);
        _label.setPosition(new Vector2f(30f, 20f));

        _label2 = new UIText();
        _label2.setVisible(false);
        _label2.setPosition(new Vector2f(0f, -14f));
    }

    private Vector2f findPosition() {
        int realId = _id - 9;
        Vector2f result;

        if (realId >= 0) {
            result = new Vector2f(16f + (getSize().x - 3f) * (realId % 9), 208f + ((getSize().y - 3f) * (realId / 9)));
        } else {
            realId = _id + 27;
            result = new Vector2f(16f + (getSize().x - 3f) * (realId % 9), 208f + 10f + ((getSize().y - 3f) * (realId / 9)));
        }

        return result;
    }

    @Override
    public void update() {
        setPosition(findPosition());

        processMouseInput();

        InventoryComponent inventory = CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(InventoryComponent.class);
        if (inventory == null || inventory.itemSlots.size() < _id) {
            getLabel().setVisible(false);
            return;
        }

        EntityRef itemEntity = inventory.itemSlots.get(_id);
        ItemComponent item = itemEntity.getComponent(ItemComponent.class);
        BlockItemComponent blockItem = itemEntity.getComponent(BlockItemComponent.class);
        if (item != null && item.stackCount > 1) {
            getLabel().setVisible(true);
            getLabel().setText(Integer.toString(item.stackCount));
            if (blockItem != null) {
                _label2.setText(blockItem.blockFamily.getTitle());
            }
        }else if (item != null){
            if (blockItem != null) {
                _label2.setText(blockItem.blockFamily.getTitle());
            }

            else{
                _label2.setText(item.icon);
            }
        }
        else {
            getLabel().setVisible(false);
        }
    }

    private void processMouseInput() {
        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());

        if (intersects(mousePos)) {
            _selectionRectangle.setVisible(true);
            _label2.setVisible(true);
        } else {
            _clickSoundPlayed = false;
            _mouseUp = false;
            _mouseDown = false;
            _selectionRectangle.setVisible(false);
            _label2.setVisible(false);
        }
    }

    @Override
    public void render() {
        _selectionRectangle.renderTransformed();

        InventoryComponent inventory = CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(InventoryComponent.class);
        if (inventory == null || inventory.itemSlots.size() < _id) {
            return;
        }

        EntityRef itemEntity = inventory.itemSlots.get(_id);
        ItemComponent item = itemEntity.getComponent(ItemComponent.class);

        if (item == null) return;

        if (item.icon.isEmpty()) {
            BlockItemComponent blockItem = itemEntity.getComponent(BlockItemComponent.class);
            if (blockItem != null) {
                renderBlockIcon(blockItem.blockFamily);
            }
        } else {
            Icon icon = Icon.get(item.icon);
            if (icon != null)
            {
                renderIcon(icon);
            }
        }

        _label.renderTransformed();
        _label2.renderTransformed();
    }

    public void setSelected(boolean selected) {
        _selected = selected;
    }

    public boolean getSelected() {
        return _selected;
    }

    public UIText getLabel() {
        return _label;
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

    private void renderBlockIcon(BlockFamily blockFamily) {
        if (blockFamily == null) return;

        glEnable(GL11.GL_DEPTH_TEST);
        glClear(GL11.GL_DEPTH_BUFFER_BIT);
        glPushMatrix();
        glTranslatef(20f, 20f, 0f);

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPushMatrix();
        glTranslatef(4f, 0f, 0f);
        GL11.glScalef(20f, 20f, 20f);
        GL11.glRotatef(170f, 1f, 0f, 0f);
        GL11.glRotatef(-16f, 0f, 1f, 0f);
        glBindTexture(GL11.GL_TEXTURE_2D, terrainTex.getId());;

        Block block = blockFamily.getArchetypeBlock();
        block.render();

        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        glPopMatrix();
        glDisable(GL11.GL_DEPTH_TEST);
    }
}
