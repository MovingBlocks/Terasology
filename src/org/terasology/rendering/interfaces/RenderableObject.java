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
package org.terasology.rendering.interfaces;

/**
 * The base class of all renderable objects.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public interface RenderableObject {

    /**
     * Rendering operations have to implement this method.
     */
    public void render();

    /**
     * Updating operations have to implement this method.
     */
    public void update();

}
