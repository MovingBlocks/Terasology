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
package com.github.begla.blockmania.rendering;

import javolution.util.FastList;

/**
 * Scene that contains multiple renderable objects.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public abstract class RenderableScene implements RenderableObject {

    private FastList<RenderableObject> _renderableObjects = FastList.newInstance();

    public void render() {
        for (RenderableObject ro : _renderableObjects)
            ro.render();

    }

    public void update() {
        for (RenderableObject ro : _renderableObjects)
            ro.update();
    }

    /**
     * Add a new renderable object.
     *
     * @param ro
     */
    public void addRenderableObject(RenderableObject ro) {
        _renderableObjects.add(ro);
    }

    /**
     * Remove an existing renderable object.
     *
     * @param ro
     */
    public void removeRenderableObject(RenderableObject ro) {
        _renderableObjects.remove(ro);
    }
}
