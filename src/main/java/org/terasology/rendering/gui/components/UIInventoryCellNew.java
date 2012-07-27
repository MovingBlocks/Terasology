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
package org.terasology.rendering.gui.components;

import com.google.common.collect.Lists;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.block.BlockItemComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.logic.manager.AssetManager;
import org.terasology.model.blocks.Block;
import org.terasology.model.blocks.BlockFamily;
import org.terasology.model.inventory.Icon;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

/**
 * A single cell of the toolbar with a small text label and a selection
 * rectangle.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class UIInventoryCellNew extends UIDisplayElement {

    private final UIGraphicsElement selectionRectangle;
    private final UIText label;
    private final UIText label2;
    private final UIGraphicsElement background;

    private EntityRef entity;
    private int slot;
    private boolean selected = false;
    private Texture terrainTex;

    private List<CellSubscriber> subscribers = Lists.newArrayList();

    public interface CellSubscriber {
        public void onCellActivated(UIInventoryCellNew cell);
    }

    public UIInventoryCellNew(EntityRef entity, int slot, Vector2f size) {
        Texture guiTex = AssetManager.loadTexture("engine:gui");
        terrainTex = AssetManager.loadTexture("engine:terrain");
        this.slot = slot;
        this.entity = entity;

        setSize(size);

        selectionRectangle = new UIGraphicsElement(guiTex);
        selectionRectangle.getTextureSize().set(new Vector2f(24f / 256f, 24f / 256f));
        selectionRectangle.getTextureOrigin().set(new Vector2f(0.0f, 23f / 256f));
        selectionRectangle.setSize(getSize());

        background = new UIGraphicsElement(guiTex);
        background.getTextureSize().set(new Vector2f(20f / 256f, 20f / 256f));
        background.getTextureOrigin().set(new Vector2f(1.0f / 256f, 1f / 256f));
        background.setSize(getSize());
        background.setVisible(true);

        label = new UIText();
        label.setVisible(true);
        label.setPosition(new Vector2f(30f, 20f));

        label2 = new UIText();
        label2.setVisible(true);
        label2.setPosition(new Vector2f(0f, -14f));
    }

    public void subscribe(CellSubscriber subscriber) {
        subscribers.add(subscriber);
    }

    public void unsubscribe(CellSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    public int getSlot() {
        return slot;
    }

    @Override
    public void update() {
        processMouseInput();

        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        if (inventory == null || inventory.itemSlots.size() < slot) {
            getLabel().setVisible(false);
            return;
        }

        EntityRef itemEntity = inventory.itemSlots.get(slot);
        ItemComponent item = itemEntity.getComponent(ItemComponent.class);
        BlockItemComponent blockItem = itemEntity.getComponent(BlockItemComponent.class);
        if (item != null && item.stackCount > 1) {
            getLabel().setVisible(true);
            getLabel().setText(Integer.toString(item.stackCount));
            if (blockItem != null) {
                label2.setText(blockItem.blockFamily.getTitle());
            }
        } else if (item != null) {
            if (blockItem != null) {
                label2.setText(blockItem.blockFamily.getTitle());
            } else {
                label2.setText(item.name);
            }
        } else {
            getLabel().setVisible(false);
        }
    }

    public void processMouseInput(int button, boolean state, int wheelMoved) {
        if (button == 0 && state && !_mouseUp) {
            _mouseDown = true;
            _mouseUp = false;
            _clickSoundPlayed = false;
        } else if (button == 0 && !state && _mouseDown) {
            _mouseUp = true;
            _mouseDown = false;
        }
    }

    private void processMouseInput() {
        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());

        if (intersects(mousePos)) {
            selectionRectangle.setVisible(true);
            label2.setVisible(true);
            if (_mouseUp) {
                _mouseUp = false;
                for (CellSubscriber subscriber : subscribers) {
                    subscriber.onCellActivated(this);
                }
            }
        } else {
            _clickSoundPlayed = false;
            _mouseUp = false;
            _mouseDown = false;
            selectionRectangle.setVisible(false);
            label2.setVisible(false);
        }
    }

    @Override
    public void render() {
        background.renderTransformed();

        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        if (inventory == null || inventory.itemSlots.size() < slot) {
            return;
        }

        EntityRef itemEntity = inventory.itemSlots.get(slot);
        ItemComponent item = itemEntity.getComponent(ItemComponent.class);

        if (item == null) return;

        if (item.icon.isEmpty()) {
            BlockItemComponent blockItem = itemEntity.getComponent(BlockItemComponent.class);
            if (blockItem != null) {
                renderBlockIcon(blockItem.blockFamily);
            }
        } else {
            Icon icon = Icon.get(item.icon);
            if (icon != null) {
                renderIcon(icon);
            }
        }

        selectionRectangle.renderTransformed();
        label.renderTransformed();
        label2.renderTransformed();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean getSelected() {
        return selected;
    }

    public UIText getLabel() {
        return label;
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
        glBindTexture(GL11.GL_TEXTURE_2D, terrainTex.getId());

        Block block = blockFamily.getArchetypeBlock();
        block.renderWithLightValue(1.0f);

        GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        glPopMatrix();
        glDisable(GL11.GL_DEPTH_TEST);
    }
}
