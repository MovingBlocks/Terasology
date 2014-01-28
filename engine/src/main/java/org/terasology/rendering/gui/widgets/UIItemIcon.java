/*
 * Copyright 2013 MovingBlocks
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

package org.terasology.rendering.gui.widgets;

import org.lwjgl.opengl.GL11;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.gui.events.UIItemIconRendered;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.icons.Icon;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemComponent;

import javax.vecmath.Vector2f;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

/**
 * Displays an item as an icon with an optional stack count.
 */
public class UIItemIcon extends UIDisplayContainer {
    private static final Vector2f ITEM_COUNT_POSITION = new Vector2f(26f, 5f);

    private EntityRef item = EntityRef.NULL;

    private boolean displayingItemCount = true;

    //sub elements
    private final UILabel itemCount;

    //rendering
    private Texture terrainTex;

    private InventoryManager inventoryManager;

    private Integer fixedItemCount;

    public UIItemIcon(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        terrainTex = Assets.getTexture("engine:terrain");

        itemCount = new UILabel();
        itemCount.setVisible(false);
        itemCount.setPosition(ITEM_COUNT_POSITION);

        addDisplayElement(itemCount);
        setVisible(false);
    }

    public void setFixedItemCount(int fixedItemCount) {
        this.fixedItemCount = fixedItemCount;
    }

    public void setItem(EntityRef item) {
        this.item = item;

        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        setVisible(itemComponent != null);
        updateCountLabel();
    }


    public boolean isDisplayingItemCount() {
        return displayingItemCount;
    }

    public void setDisplayingItemCount(boolean enable) {
        displayingItemCount = enable;
        updateCountLabel();

    }

    private void updateCountLabel() {
        int stackSize = inventoryManager.getStackSize(item);
        if (fixedItemCount != null) {
            stackSize = fixedItemCount;
        }
        if (stackSize > 1 && displayingItemCount) {
            itemCount.setVisible(true);
            itemCount.setText(Integer.toString(stackSize));
        } else {
            itemCount.setVisible(false);
        }
    }

    public EntityRef getItem() {
        return item;
    }

    @Override
    public void layout() {

    }

    @Override
    public void update() {
        setVisible(item.exists());
    }

    @Override
    public void render() {

        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        if (itemComponent == null) {
            return;
        }

        //render icon
        if (itemComponent.icon == null) {
            BlockItemComponent blockItem = item.getComponent(BlockItemComponent.class);
            if (blockItem != null) {
                renderBlockIcon(blockItem.blockFamily);
            }
        } else {
            Icon icon = Icon.get(itemComponent.icon);
            if (icon != null) {
                renderIcon(icon);
            }
        }
        super.render();
        item.send(new UIItemIconRendered());
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
        if (blockFamily == null) {
            renderIcon(Icon.get("questionmark"));
            return;
        }

        glEnable(GL11.GL_DEPTH_TEST);
        glClear(GL11.GL_DEPTH_BUFFER_BIT);
        glPushMatrix();

        glTranslatef(20f, 20f, 0f);

        GL11.glPushMatrix();
        glTranslatef(4f, 0f, 0f);
        GL11.glScalef(20f, 20f, 20f);
        GL11.glRotatef(170f, 1f, 0f, 0f);
        GL11.glRotatef(-16f, 0f, 1f, 0f);
        glBindTexture(GL11.GL_TEXTURE_2D, terrainTex.getId());

        Block block = blockFamily.getArchetypeBlock();
        block.renderWithLightValue(1.0f, 1.0f);

        GL11.glPopMatrix();

        glPopMatrix();
        glDisable(GL11.GL_DEPTH_TEST);
    }

}
