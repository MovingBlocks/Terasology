package org.terasology.rendering.gui.components;

import org.lwjgl.opengl.GL11;
import org.terasology.asset.AssetManager;
import org.terasology.components.ItemComponent;
import org.terasology.components.block.BlockItemComponent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.model.inventory.Icon;
import org.terasology.rendering.assets.Texture;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.world.block.Block;
import org.terasology.world.block.family.BlockFamily;

import javax.vecmath.Vector2f;

import static org.lwjgl.opengl.GL11.*;

/**
 * Displays a little icon and item count for an item cell.
 *
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 */
public class UIItemCellIcon extends UIDisplayContainer {
    //entity
    private EntityRef itemEntity;
    private ItemComponent itemComponent;

    //sub elements
    private final UIText itemCount;

    //layout
    private Texture terrainTex;
    private Vector2f itemCountPosition = new Vector2f(26f, 5f);

    public UIItemCellIcon() {
        itemEntity = EntityRef.NULL;

        terrainTex = AssetManager.loadTexture("engine:terrain");

        itemCount = new UIText();
        itemCount.setVisible(false);
        itemCount.setPosition(itemCountPosition);

        addDisplayElement(itemCount);
    }

    @Override
    public void layout() {

    }

    @Override
    public void update() {
        //item count visibility
        if (itemComponent != null) {
            if (itemComponent.stackCount > 1) {
                itemCount.setVisible(true);
                itemCount.setText(Integer.toString(itemComponent.stackCount));
            } else {
                itemCount.setVisible(false);
            }
        }
    }

    @Override
    public void render() {
        if (!itemEntity.exists())
            return;

        if (itemComponent == null)
            return;

        //render icon
        if (itemComponent.icon.isEmpty()) {
            BlockItemComponent blockItem = itemEntity.getComponent(BlockItemComponent.class);
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
        if (blockFamily == null)
            return;

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

    public EntityRef getItemEntity() {
        return itemEntity;
    }

    public void setItemEntity(EntityRef itemEntity) {
        this.itemEntity = itemEntity;
        if (itemEntity.exists())
            this.itemComponent = itemEntity.getComponent(ItemComponent.class);
        else
            this.itemComponent = null;
    }
}