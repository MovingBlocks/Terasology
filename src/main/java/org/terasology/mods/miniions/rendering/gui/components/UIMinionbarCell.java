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

import org.lwjgl.opengl.GL11;
import org.terasology.entitySystem.EntityRef;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.AssetManager;
import org.terasology.model.inventory.Icon;
import org.terasology.mods.miniions.components.MinionBarComponent;
import org.terasology.mods.miniions.components.MinionComponent;
import org.terasology.mods.miniions.components.MinionControllerComponent;
import org.terasology.rendering.gui.components.UIText;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.UIGraphicsElement;

import javax.vecmath.Vector2f;

import static org.lwjgl.opengl.GL11.*;

/**
 * A single cell of the toolbar with a small text label and a selection
 * rectangle.
 *
 * @author Overdhose, copied from toolbar
 */
public class UIMinionbarCell extends UIDisplayElement {

    private final UIGraphicsElement selectionRectangle;
    private final UIText label;

    private int id;
    private boolean selected = false;

    public UIMinionbarCell(int id) {
        this.id = id;

        setSize(new Vector2f(48f, 48f));

        selectionRectangle = new UIGraphicsElement(AssetManager.loadTexture("engine:gui"));
        selectionRectangle.getTextureSize().set(new Vector2f(24f / 256f, 24f / 256f));
        selectionRectangle.getTextureOrigin().set(new Vector2f(0.0f, 24f / 256f));
        selectionRectangle.setSize(new Vector2f(48f, 48f));

        label = new UIText();
        label.setVisible(true);
        label.setPosition(new Vector2f(30f, 20f));
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
        if (inventory.MinionSlots.size() > id) {
            MinionControllerComponent minionController = localPlayer.getEntity().getComponent(MinionControllerComponent.class);
            if (minionController != null) {
                setSelected(minionController.selectedMinion == id);
            }
        }
    }

    @Override
    public void render() {

        selectionRectangle.renderTransformed();

        MinionBarComponent inventory = CoreRegistry.get(LocalPlayer.class).getEntity().getComponent(MinionBarComponent.class);
        if (inventory == null) {
            return;
        }
        if (inventory.MinionSlots.size() <= id) {
            return;
        }
        EntityRef minionEntity = inventory.MinionSlots.get(id);
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

    public UIText getLabel() {
        return label;
    }
}
