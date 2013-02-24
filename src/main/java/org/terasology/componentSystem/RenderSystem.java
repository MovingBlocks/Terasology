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
package org.terasology.componentSystem;

import org.terasology.entitySystem.ComponentSystem;

/**
 * @author Immortius <immortius@gmail.com>
 */
public interface RenderSystem extends ComponentSystem {

    public void renderOpaque();

    public void renderTransparent();

    public void renderOverlay();

    public void renderFirstPerson();

    public void renderShadows();
}
