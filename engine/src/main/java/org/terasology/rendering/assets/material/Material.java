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

package org.terasology.rendering.assets.material;

import org.terasology.assets.Asset;
import org.terasology.assets.AssetType;
import org.terasology.assets.ResourceUrn;
import org.terasology.math.geom.Matrix3f;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector4f;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.cameras.Camera;

import java.nio.FloatBuffer;

/**
 */
public abstract class Material extends Asset<MaterialData> {

    protected Material(ResourceUrn urn, AssetType<?, MaterialData> assetType) {
        super(urn, assetType);
    }

    /**
     * @return Whether this material is currently in a renderable state (all textures loaded)
     */
    public abstract boolean isRenderable();

    public abstract void recompile();

    /**
     * Enables the material for rendering
     */
    public abstract void enable();

    /**
     * Sets a float uniform parameter (for all feature permutations)
     *
     * @param name
     * @param f
     */
    public abstract void setFloat(String name, float f);

    public abstract void setFloat(String name, float f, boolean currentOnly);

    /**
     * Sets a float1 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param buffer
     */
    public abstract void setFloat1(String name, FloatBuffer buffer);

    public abstract void setFloat1(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a float2 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param f1
     * @param f2
     */
    public abstract void setFloat2(String name, float f1, float f2);

    public abstract void setFloat2(String name, float f1, float f2, boolean currentOnly);

    /**
     * Sets a float2 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param value
     */
    public abstract void setFloat2(String name, Vector2f value);

    public abstract void setFloat2(String name, Vector2f value, boolean currentOnly);

    /**
     * Sets a float2 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param buffer
     */
    public abstract void setFloat2(String name, FloatBuffer buffer);

    public abstract void setFloat2(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a float3 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param f1
     * @param f2
     * @param f3
     */
    public abstract void setFloat3(String name, float f1, float f2, float f3);

    public abstract void setFloat3(String name, float f1, float f2, float f3, boolean currentOnly);

    /**
     * Sets a float3 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param value
     */
    public abstract void setFloat3(String name, Vector3f value);

    public abstract void setFloat3(String name, Vector3f value, boolean currentOnly);

    /**
     * Sets a float3 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param buffer
     */
    public abstract void setFloat3(String name, FloatBuffer buffer);

    public abstract void setFloat3(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a float4 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param f1
     * @param f2
     * @param f3
     * @param f4
     */
    public abstract void setFloat4(String name, float f1, float f2, float f3, float f4);

    public abstract void setFloat4(String name, float f1, float f2, float f3, float f4, boolean currentOnly);

    /**
     * Sets a float4 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param value
     */
    public abstract void setFloat4(String name, Vector4f value);

    public abstract void setFloat4(String name, Vector4f value, boolean currentOnly);

    /**
     * Sets a float4 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param buffer
     */
    public abstract void setFloat4(String name, FloatBuffer buffer);

    public abstract void setFloat4(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets an int uniform parameter (for all feature permutations)
     *
     * @param name
     * @param i
     */
    public abstract void setInt(String name, int i);

    public abstract void setInt(String name, int i, boolean currentOnly);

    /**
     * Sets a boolean (int 1 or 0) uniform parameter (for all feature permutations}
     *
     * @param name
     * @param value
     */
    public abstract void setBoolean(String name, boolean value);

    public abstract void setBoolean(String name, boolean value, boolean currentOnly);

    /**
     * Sets a matrix3 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param matrix
     */
    public abstract void setMatrix3(String name, Matrix3f matrix);

    public abstract void setMatrix3(String name, Matrix3f matrix, boolean currentOnly);

    /**
     * Sets a matrix3 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param buffer
     */
    public abstract void setMatrix3(String name, FloatBuffer buffer);

    public abstract void setMatrix3(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a matrix4 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param matrix
     */
    public abstract void setMatrix4(String name, Matrix4f matrix);

    public abstract void setMatrix4(String name, Matrix4f matrix, boolean currentOnly);

    /**
     * Sets a matrix3 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param buffer
     */
    public abstract void setMatrix4(String name, FloatBuffer buffer);

    public abstract void setMatrix4(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a texture parameter
     *
     * @param name
     * @param texture
     */
    public abstract void setTexture(String name, Texture texture);

    /**
     * Sets up this matertial for the given camera
     *
     * @param camera
     */
    public abstract void setCamera(Camera camera);

    public abstract boolean supportsFeature(ShaderProgramFeature feature);

    /**
     * Activates a feature of this material, if available
     *
     * @param feature
     */
    public abstract void activateFeature(ShaderProgramFeature feature);

    /**
     * Deactivates a feature of this material if active
     *
     * @param feature
     */
    public abstract void deactivateFeature(ShaderProgramFeature feature);

    /**
     * Deactivates a set of features of this material
     *
     * @param features
     */
    public abstract void deactivateFeatures(ShaderProgramFeature... features);

    public abstract void bindTextures();
}
