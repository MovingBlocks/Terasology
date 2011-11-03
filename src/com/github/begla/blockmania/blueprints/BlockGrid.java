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
package com.github.begla.blockmania.blueprints;

import com.github.begla.blockmania.datastructures.AABB;
import com.github.begla.blockmania.datastructures.BlockPosition;
import com.github.begla.blockmania.rendering.RenderableObject;
import javolution.util.FastList;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

/**
 * TODO
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class BlockGrid implements RenderableObject {

    private AABB _displayAABB = new AABB(new Vector3f(), new Vector3f(0.5f, 0.5f, 0.5f));
    private FastList<BlockPosition> _gridPositions = FastList.newInstance();

    public void render() {
       for (BlockPosition gp : _gridPositions) {
           GL11.glPushMatrix();

           GL11.glTranslatef(gp.x, gp.y, gp.z);
          _displayAABB.render();

           GL11.glPopMatrix();
       }
    }

    public void addGridPosition(BlockPosition gridPosition) {
        _gridPositions.add(gridPosition);
    }

    public void removeGridPosition(BlockPosition gridPosition) {
       _gridPositions.remove(gridPosition);
    }

    public void clear() {
        _gridPositions.clear();
    }

    public void update() {
       // Nothing to do.
    }

}
