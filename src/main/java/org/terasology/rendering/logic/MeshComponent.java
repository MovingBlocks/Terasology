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
package org.terasology.rendering.logic;

import org.terasology.entitySystem.Component;
import org.terasology.network.Replicate;
import org.terasology.rendering.assets.Material;
import org.terasology.rendering.primitives.Mesh;

import javax.vecmath.Color4f;

/**
 * @author Immortius <immortius@gmail.com>
 */
public final class MeshComponent implements Component {

    // Temporary render details
    public enum RenderType {
        Normal,
        GelatinousCube
    }

    @Replicate
    public RenderType renderType = RenderType.Normal;
    @Replicate
    public Mesh mesh;
    @Replicate
    public Material material;

    public boolean hideFromOwner;

    // TODO: Some sort of Texture + Shader type?
    //public String material;

    // This should be elsewhere I think, probably in the material
    @Replicate
    public Color4f color = new Color4f(0, 0, 0, 1);

}
