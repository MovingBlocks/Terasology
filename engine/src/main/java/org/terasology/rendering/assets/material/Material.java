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

import org.terasology.asset.Asset;
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
 * @author Immortius
 */
public interface Material extends Asset<MaterialData> {

    /**
     * Storage qualifier of GLSL (input) parameters.
     */
    public static enum StorageQualifier {
        UNIFORM,
        ATTRIBUTE
    }

    void recompile();

    /**
     * Enables the material for rendering
     */
    void enable();

    /**
     * Sets a float uniform/attribute parameter (for all feature permutations)
     *
     * @param qualifier The storage qualifier of the parameter, UNIFORM or ATTRIBUTE.
     * @param name
     * @param f
     */
    void setFloat(StorageQualifier qualifier, String name, float f);

    void setFloat(StorageQualifier qualifier, String name, float f, boolean currentOnly);

    /**
     * Sets a float1 uniform/attribute parameter (for all feature permutations)
     *
     * @param qualifier The storage qualifier of the parameter, UNIFORM or ATTRIBUTE.
     * @param name
     * @param buffer
     */
    void setFloat1(StorageQualifier qualifier, String name, FloatBuffer buffer);

    void setFloat1(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a float2 uniform/attribute parameter (for all feature permutations)
     *
     * @param qualifier The storage qualifier of the parameter, UNIFORM or ATTRIBUTE.
     * @param name
     * @param f1
     * @param f2
     */
    void setFloat2(StorageQualifier qualifier, String name, float f1, float f2);

    void setFloat2(StorageQualifier qualifier, String name, float f1, float f2, boolean currentOnly);

    /**
     * Sets a float2 uniform/attribute parameter (for all feature permutations)
     *
     * @param qualifier The storage qualifier of the parameter, UNIFORM or ATTRIBUTE.
     * @param name
     * @param value
     */
    void setFloat2(StorageQualifier qualifier, String name, Vector2f value);

    void setFloat2(StorageQualifier qualifier, String name, Vector2f value, boolean currentOnly);

    /**
     * Sets a float2 uniform/attribute parameter (for all feature permutations)
     *
     * @param qualifier The storage qualifier of the parameter, UNIFORM or ATTRIBUTE.
     * @param name
     * @param buffer
     */
    void setFloat2(StorageQualifier qualifier, String name, FloatBuffer buffer);

    void setFloat2(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a float3 uniform/attribute parameter (for all feature permutations)
     *
     * @param qualifier The storage qualifier of the parameter, UNIFORM or ATTRIBUTE.
     * @param name
     * @param f1
     * @param f2
     * @param f3
     */
    void setFloat3(StorageQualifier qualifier, String name, float f1, float f2, float f3);

    void setFloat3(StorageQualifier qualifier, String name, float f1, float f2, float f3, boolean currentOnly);

    /**
     * Sets a float3 uniform/attribute parameter (for all feature permutations)
     *
     * @param qualifier The storage qualifier of the parameter, UNIFORM or ATTRIBUTE.
     * @param name
     * @param value
     */
    void setFloat3(StorageQualifier qualifier, String name, Vector3f value);

    void setFloat3(StorageQualifier qualifier, String name, Vector3f value, boolean currentOnly);

    /**
     * Sets a float3 uniform/attribute parameter (for all feature permutations)
     *
     * @param qualifier The storage qualifier of the parameter, UNIFORM or ATTRIBUTE.
     * @param name
     * @param buffer
     */
    void setFloat3(StorageQualifier qualifier, String name, FloatBuffer buffer);

    void setFloat3(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a float4 uniform/attribute parameter (for all feature permutations)
     *
     * @param qualifier The storage qualifier of the parameter, UNIFORM or ATTRIBUTE.
     * @param name
     * @param f1
     * @param f2
     * @param f3
     * @param f4
     */
    void setFloat4(StorageQualifier qualifier, String name, float f1, float f2, float f3, float f4);

    void setFloat4(StorageQualifier qualifier, String name, float f1, float f2, float f3, float f4, boolean currentOnly);

    /**
     * Sets a float4 uniform/attribute parameter (for all feature permutations)
     *
     * @param qualifier The storage qualifier of the parameter, UNIFORM or ATTRIBUTE.
     * @param name
     * @param value
     */
    void setFloat4(StorageQualifier qualifier, String name, Vector4f value);

    void setFloat4(StorageQualifier qualifier, String name, Vector4f value, boolean currentOnly);

    /**
     * Sets a float4 uniform/attribute parameter (for all feature permutations)
     *
     * @param qualifier The storage qualifier of the parameter, UNIFORM or ATTRIBUTE.
     * @param name
     * @param buffer
     */
    void setFloat4(StorageQualifier qualifier, String name, FloatBuffer buffer);

    void setFloat4(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets an int uniform/attribute parameter (for all feature permutations)
     *
     * @param qualifier The storage qualifier of the parameter, UNIFORM or ATTRIBUTE.
     * @param name
     * @param i
     */
    void setInt(StorageQualifier qualifier, String name, int i);

    void setInt(StorageQualifier qualifier, String name, int i, boolean currentOnly);

    /**
     * Sets a boolean (int 1 or 0) uniform/attribute parameter (for all feature permutations}
     *
     * @param qualifier The storage qualifier of the parameter, UNIFORM or ATTRIBUTE.
     * @param name
     * @param value
     */
    void setBoolean(StorageQualifier qualifier, String name, boolean value);

    void setBoolean(StorageQualifier qualifier, String name, boolean value, boolean currentOnly);

    /**
     * Sets a matrix3 uniform/attribute parameter (for all feature permutations)
     *
     * @param qualifier The storage qualifier of the parameter, UNIFORM or ATTRIBUTE.
     * @param name
     * @param matrix
     */
    void setMatrix3(StorageQualifier qualifier, String name, Matrix3f matrix);

    void setMatrix3(StorageQualifier qualifier, String name, Matrix3f matrix, boolean currentOnly);

    /**
     * Sets a matrix3 uniform/attribute parameter (for all feature permutations)
     *
     * @param qualifier The storage qualifier of the parameter, UNIFORM or ATTRIBUTE.
     * @param name
     * @param buffer
     */
    void setMatrix3(StorageQualifier qualifier, String name, FloatBuffer buffer);

    void setMatrix3(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a matrix4 uniform/attribute parameter (for all feature permutations)
     *
     * @param qualifier The storage qualifier of the parameter, UNIFORM or ATTRIBUTE.
     * @param name
     * @param matrix
     */
    void setMatrix4(StorageQualifier qualifier, String name, Matrix4f matrix);

    void setMatrix4(StorageQualifier qualifier, String name, Matrix4f matrix, boolean currentOnly);

    /**
     * Sets a matrix4 uniform/attribute parameter (for all feature permutations)
     *
     * @param qualifier The storage qualifier of the parameter, UNIFORM or ATTRIBUTE.
     * @param name
     * @param buffer
     */
    void setMatrix4(StorageQualifier qualifier, String name, FloatBuffer buffer);

    void setMatrix4(StorageQualifier qualifier, String name, FloatBuffer buffer, boolean currentOnly);

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

    boolean supportsFeature(ShaderProgramFeature feature);

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
