/*
 * Copyright 2013 MovingBlocks
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
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.nui.Color;
import org.terasology.world.block.ForceBlockActive;

/**
 */
@ForceBlockActive
public final class MeshComponent implements Component {

    @Replicate
    public Mesh mesh;
    @Replicate
    public Material material;

    // TODO: This should be a setting on the material
    @Replicate
    public boolean translucent;

    public boolean hideFromOwner;

    // TODO: Some sort of Texture + Shader type?
    //public String material;

    // This should be elsewhere I think, probably in the material
    @Replicate
    public Color color = Color.WHITE;

}
