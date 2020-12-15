/*
 * Copyright 2014 MovingBlocks
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
package org.terasology.rendering.nui.internal;

import org.joml.Quaternionfc;
import org.joml.Rectanglei;
import org.joml.Vector2ic;
import org.joml.Vector3fc;
import org.terasology.assets.ResourceUrn;
import org.terasology.nui.canvas.CanvasRenderer;
import org.terasology.rendering.assets.material.Material;
import org.terasology.rendering.assets.mesh.Mesh;
import org.terasology.rendering.opengl.FrameBufferObject;

/**
 */
public interface TerasologyCanvasRenderer extends CanvasRenderer {
    FrameBufferObject getFBO(ResourceUrn urn, Vector2ic size);

    void drawMesh(Mesh mesh, Material material, Rectanglei drawRegion, Rectanglei cropRegion, Quaternionfc rotation, Vector3fc offset, float scale, float alpha);

    void drawMaterialAt(Material material, Rectanglei drawRegion);
}
