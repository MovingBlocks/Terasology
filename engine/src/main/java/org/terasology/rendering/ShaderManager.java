/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.rendering;

import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.texture.Texture;

public interface ShaderManager {

    void initShaders();

    void setActiveMaterial(Material material);

    void bindTexture(int slot, Texture texture);

    Material getActiveMaterial();

    void recompileAllShaders();

    /**
     * Enables the default shader program.
     */
    void enableDefault();

    /**
     * Enables the default shader program.
     */
    void enableDefaultTextured();

    void disableShader();

}
