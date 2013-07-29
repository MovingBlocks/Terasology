/*
 * Copyright 2013 Moving Blocks
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

import org.terasology.asset.Asset;
import org.terasology.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.rendering.assets.texture.Texture;
import org.terasology.rendering.cameras.Camera;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Tuple2f;
import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple4f;
import java.nio.FloatBuffer;

/**
 * @author Immortius
 */
public interface Material extends Asset<MaterialData> {

    void recompile();

    /**
     * Enables the material for rendering
     */
    void enable();

    /**
     * Sets a float uniform parameter (for all feature permutations)
     *
     * @param name
     * @param f
     */
    void setFloat(String name, float f);

    void setFloat(String name, float f, boolean currentOnly);

    /**
     * Sets a float1 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param buffer
     */
    void setFloat1(String name, FloatBuffer buffer);

    void setFloat1(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a float2 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param f1
     * @param f2
     */
    void setFloat2(String name, float f1, float f2);

    void setFloat2(String name, float f1, float f2, boolean currentOnly);

    /**
     * Sets a float2 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param value
     */
    void setFloat2(String name, Tuple2f value);

    void setFloat2(String name, Tuple2f value, boolean currentOnly);

    /**
     * Sets a float2 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param buffer
     */
    void setFloat2(String name, FloatBuffer buffer);

    void setFloat2(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a float3 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param f1
     * @param f2
     * @param f3
     */
    void setFloat3(String name, float f1, float f2, float f3);

    void setFloat3(String name, float f1, float f2, float f3, boolean currentOnly);

    /**
     * Sets a float3 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param value
     */
    void setFloat3(String name, Tuple3f value);

    void setFloat3(String name, Tuple3f value, boolean currentOnly);

    /**
     * Sets a float3 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param buffer
     */
    void setFloat3(String name, FloatBuffer buffer);

    void setFloat3(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a float4 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param f1
     * @param f2
     * @param f3
     * @param f4
     */
    void setFloat4(String name, float f1, float f2, float f3, float f4);

    void setFloat4(String name, float f1, float f2, float f3, float f4, boolean currentOnly);

    /**
     * Sets a float4 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param value
     */
    void setFloat4(String name, Tuple4f value);

    void setFloat4(String name, Tuple4f value, boolean currentOnly);

    /**
     * Sets a float4 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param buffer
     */
    void setFloat4(String name, FloatBuffer buffer);

    void setFloat4(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets an int uniform parameter (for all feature permutations)
     *
     * @param name
     * @param i
     */
    void setInt(String name, int i);

    void setInt(String name, int i, boolean currentOnly);

    /**
     * Sets a boolean (int 1 or 0) uniform parameter (for all feature permutations}
     *
     * @param name
     * @param value
     */
    void setBoolean(String name, boolean value);

    void setBoolean(String name, boolean value, boolean currentOnly);

    /**
     * Sets a matrix3 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param matrix
     */
    void setMatrix3(String name, Matrix3f matrix);

    void setMatrix3(String name, Matrix3f matrix, boolean currentOnly);

    /**
     * Sets a matrix3 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param buffer
     */
    void setMatrix3(String name, FloatBuffer buffer);

    void setMatrix3(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a matrix4 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param matrix
     */
    void setMatrix4(String name, Matrix4f matrix);

    void setMatrix4(String name, Matrix4f matrix, boolean currentOnly);

    /**
     * Sets a matrix3 uniform parameter (for all feature permutations)
     *
     * @param name
     * @param buffer
     */
    void setMatrix4(String name, FloatBuffer buffer);

    void setMatrix4(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a texture parameter
     *
     * @param name
     * @param texture
     */
    void setTexture(String name, Texture texture);

    /**
     * Sets up this matertial for the given camera
     *
     * @param camera
     */
    void setCamera(Camera camera);

    /**
     * Activates a feature of this material, if available
     *
     * @param feature
     */
    void activateFeature(ShaderProgramFeature feature);

    /**
     * Deactivates a feature of this material if active
     *
     * @param feature
     */
    void deactivateFeature(ShaderProgramFeature feature);

    /**
     * Deactivates a set of features of this material
     *
     * @param features
     */
    void deactivateFeatures(ShaderProgramFeature... features);

    void bindTextures();
}
