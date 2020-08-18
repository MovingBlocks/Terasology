/*
 * Copyright 2020 MovingBlocks
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
package org.terasology.rendering.nui;

import org.terasology.assets.ResourceUrn;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.nui.Canvas;
import org.terasology.nui.SubRegion;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.nui.internal.TerasologyCanvasImpl;

// HACK: This whole class was created in order to provide access to internal, implementation specific methods.

/**
 * Contains some methods not available within the standard {@link Canvas} interface, such as drawing meshes.
 */
public final class CanvasUtility {
    private CanvasUtility() {
    }

    public static void drawMesh(Canvas canvas, Mesh mesh, Texture texture, Rect2i region, Quat4f rotation, Vector3f offset, float scale) {
        // TODO: Find a way to abstractly implement drawMesh in NUI

        if (!(canvas instanceof TerasologyCanvasImpl)) {
            throw new UnsupportedOperationException("Drawing meshes is only supported using Terasology's internal renderer.");
        }

        ((TerasologyCanvasImpl) canvas).drawMesh(mesh, texture, region, rotation, offset, scale);
    }

    public static void drawMesh(Canvas canvas, Mesh mesh, Material material, Rect2i region, Quat4f rotation, Vector3f offset, float scale) {
        // TODO: Find a way to abstractly implement drawMesh in NUI

        if (!(canvas instanceof TerasologyCanvasImpl)) {
            throw new UnsupportedOperationException("Drawing meshes is only supported using Terasology's internal renderer.");
        }

        ((TerasologyCanvasImpl) canvas).drawMesh(mesh, material, region, rotation, offset, scale);
    }

    public static void drawMaterial(Canvas canvas, Material material, Rect2i region) {
        // TODO: Find a way to abstractly implement drawMaterial in NUI

        if (!(canvas instanceof TerasologyCanvasImpl)) {
            throw new UnsupportedOperationException("Drawing materials is only supported using Terasology's internal renderer.");
        }

        ((TerasologyCanvasImpl) canvas).drawMaterial(material, region);
    }

    public static SubRegion subRegionFBO(Canvas canvas, ResourceUrn uri, BaseVector2i size) {
        // TODO: Find a way to abstractly implement subRegionFBO in NUI

        if (!(canvas instanceof TerasologyCanvasImpl)) {
            throw new UnsupportedOperationException("Used FBO sub-regions is only supported using Terasology's internal renderer.");
        }

        return ((TerasologyCanvasImpl) canvas).subRegionFBO(uri, size);
    }
}
