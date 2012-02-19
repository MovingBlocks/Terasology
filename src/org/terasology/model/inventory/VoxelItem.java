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
package org.terasology.model.inventory;

import org.terasology.logic.characters.Player;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.MeshFactory;
import org.terasology.rendering.shader.ShaderProgram;

import static org.lwjgl.opengl.GL11.*;

/**
 * @author Benjamin 'begla' Glatzel <benjamin.glatzel@me.com>
 */
public abstract class VoxelItem extends Item {

    private Mesh _itemMesh;

    public VoxelItem(Player parent) {
        super(parent);

        _toolId = (byte) 1;
        _stackSize = 8;
    }

    @Override
    public boolean renderFirstPersonView() {

        ShaderProgram shader = ShaderManager.getInstance().getShaderProgram("block");
        shader.enable();
        shader.setInt("textured", 0);

        glPushMatrix();

        glTranslatef(1.0f, -1.3f + (float) getParent().calcBobbingOffset((float) Math.PI / 8f, 0.05f, 2.5f) - getParent().getHandMovementAnimationOffset() * 0.5f, -1.5f - getParent().getHandMovementAnimationOffset() * 0.5f);
        glRotatef(-getParent().getHandMovementAnimationOffset() * 64.0f, 1.0f, 0.0f, 0.0f);
        glRotatef(-20f, 1.0f, 0.0f, 0.0f);
        glRotatef(-80f, 0.0f, 1.0f, 0.0f);
        glRotatef(45f, 0.0f, 0.0f, 1.0f);

        if (_itemMesh == null)
            _itemMesh = MeshFactory.getInstance().generateItemMesh(_iconX, _iconY);

        _itemMesh.render();

        glPopMatrix();

        return true;
    }
}
