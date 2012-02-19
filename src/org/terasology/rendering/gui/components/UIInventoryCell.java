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
import org.terasology.game.Terasology;
import org.terasology.model.inventory.Icon;
import org.terasology.model.inventory.Inventory;
import org.terasology.model.inventory.Item;
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
public class UIInventoryCell extends UIDisplayElement {

    private final UIGraphicsElement _selectionRectangle;
    private final UIText _label;

    private int _id;
    private boolean _selected = false;

    public UIInventoryCell(int id) {
        _id = id;

        setSize(new Vector2f(48f, 48f));

        _selectionRectangle = new UIGraphicsElement("gui");
        _selectionRectangle.getTextureSize().set(new Vector2f(24f / 256f, 24f / 256f));
        _selectionRectangle.getTextureOrigin().set(new Vector2f(0.0f, 23f / 256f));
        _selectionRectangle.setSize(getSize());

        _label = new UIText();
        _label.setVisible(true);
        _label.setPosition(new Vector2f(30f, 20f));
    }

    @Override
    public void update() {
        setPosition(new Vector2f(16f + (getSize().x - 3f) * (_id % 9), 208f + ((getSize().y - 3f) * (_id / 9))));

        Inventory inventory = Terasology.getInstance().getActiveWorldRenderer().getPlayer().getInventory();
        processMouseInput();

        Item item = inventory.getItemAt(_id);

        if (item != null) {
            getLabel().setVisible(true);
            getLabel().setText(Integer.toString(inventory.getItemCount(_id)));
        } else {
            getLabel().setVisible(false);
        }
    }

    private void processMouseInput() {
        Vector2f mousePos = new Vector2f(Mouse.getX(), Display.getHeight() - Mouse.getY());

        if (intersects(mousePos)) {
            _selectionRectangle.setVisible(true);
        } else {
            _clickSoundPlayed = false;
            _mouseUp = false;
            _mouseDown = false;
            _selectionRectangle.setVisible(false);
        }
    }

    @Override
    public void render() {
        _selectionRectangle.renderTransformed();

        glEnable(GL11.GL_DEPTH_TEST);

        Inventory inventory = Terasology.getInstance().getActiveWorldRenderer().getPlayer().getInventory();
        Item item = inventory.getItemAt(_id);

        if (item != null) {
            glPushMatrix();
            glTranslatef(20f, 20f, 0f);
            Icon.get(item).render();
//            item.renderIcon();
            glPopMatrix();
            glDisable(GL11.GL_CULL_FACE);
        }

        glDisable(GL11.GL_DEPTH_TEST);

        _label.renderTransformed();
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
}
