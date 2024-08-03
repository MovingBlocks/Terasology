// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.engine.rendering.assets.material;

import org.joml.Matrix3fc;
import org.joml.Matrix4fc;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.terasology.engine.rendering.assets.shader.ShaderProgramFeature;
import org.terasology.engine.rendering.assets.texture.Texture;
import org.terasology.engine.rendering.cameras.Camera;
import org.terasology.gestalt.assets.Asset;
import org.terasology.gestalt.assets.AssetType;
import org.terasology.gestalt.assets.DisposableResource;
import org.terasology.gestalt.assets.ResourceUrn;

import java.nio.FloatBuffer;

public abstract class Material extends Asset<MaterialData> {

    protected Material(ResourceUrn urn, AssetType<?, MaterialData> assetType, DisposableResource resource) {
        super(urn, assetType);
        setDisposableResource(resource);
    }

    protected Material(ResourceUrn urn, AssetType<?, MaterialData> assetType) {
        super(urn, assetType);
    }

    /**
     * @return Whether this material is currently in a renderable state (all textures loaded)
     */
    public abstract boolean isRenderable();

    /**
     * recompile shader and rebind uniforms
     */
    public abstract void recompile();

    /**
     * Enables the material for rendering
     */
    public abstract void enable();

    /**
     * Sets (float) uniform parameter (for all feature permutations)
     *
     * @param name name of uniform for (float)
     * @param f vec1
     */
    public abstract void setFloat(String name, float f);

    /**
     * Sets float uniform parameter (for all feature permutations)
     *
     * @param name name of uniform for (float)
     * @param f value
     * @param currentOnly determines if written to all permutations
     */
    public abstract void setFloat(String name, float f, boolean currentOnly);

    /**
     * Sets a float1 uniform parameter (for all feature permutations)
     *
     * @param name name of uniform for (float)
     * @param buffer write 1 entry from {@link FloatBuffer} into uniform
     */
    public abstract void setFloat1(String name, FloatBuffer buffer);

    /**
     *
     * @param name name of uniform for (float)
     * @param buffer write 1 entry from {@link FloatBuffer} into uniform
     * @param currentOnly determines if written to all permutations
     */
    public abstract void setFloat1(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a float2 uniform parameter (for all feature permutations)
     *
     * @param name name of uniform for (float2)
     * @param f1 the x component
     * @param f2 the y component
     */
    public abstract void setFloat2(String name, float f1, float f2);

    /**
     * Sets a float2 uniform parameter
     *
     * @param name name of uniform for (float2)
     * @param f1 the x component
     * @param f2 the y component
     * @param currentOnly determines if written to all permutations
     */
    public abstract void setFloat2(String name, float f1, float f2, boolean currentOnly);


    /**
     * Sets a float2 uniform parameter (for all feature permutations)
     *
     * @param name name of uniform for (float2)
     * @param value the {@link Vector2fc} to write into uniform
     */
    public abstract void setFloat2(String name, Vector2fc value);

    /**
     * Sets a float2 uniform parameter (for all feature permutations)
     *
     * @param name name of uniform for (float2)
     * @param value the {@link Vector2fc} to write into uniform
     * @param currentOnly determines if written to all permutations
     */
    public abstract void setFloat2(String name, Vector2fc value, boolean currentOnly);


    /**
     * Sets a float2 uniform parameter (for all feature permutations)
     *
     * @param name name of uniform for (float2)
     * @param buffer write 2 entries from {@link FloatBuffer} into uniform
     */
    public abstract void setFloat2(String name, FloatBuffer buffer);

    /**
     * Sets a float2 uniform parameter
     *
     * @param name name of uniform for (float2)
     * @param buffer {@link FloatBuffer} of size 2 to write into uniform
     * @param currentOnly determines if written to all permutations
     */
    public abstract void setFloat2(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a float3 uniform parameter (for all feature permutations)
     *
     * @param name  name of uniform for (float3)
     * @param f1 the x component
     * @param f2 the y component
     * @param f3 the z component
     */
    public abstract void setFloat3(String name, float f1, float f2, float f3);

    /**
     *  Sets a float3 uniform parameter
     *
     * @param name  name of uniform for (float3)
     * @param f1 the x component
     * @param f2 the y component
     * @param f3 the z component
     * @param currentOnly determines if written to all permutations
     */
    public abstract void setFloat3(String name, float f1, float f2, float f3, boolean currentOnly);


    /**
     * Sets a float3 uniform parameter (for all feature permutations)
     *
     * @param name name of uniform for (float3)
     * @param value the {@link Vector3fc} to write into uniform
     */
    public abstract void setFloat3(String name, Vector3fc value);


    /**
     * Sets a float3 uniform parameter
     *
     * @param name name of uniform for (float3)
     * @param value the {@link Vector3fc} to write into uniform
     * @param currentOnly determines if written to all permutations
     */
    public abstract void setFloat3(String name, Vector3fc value, boolean currentOnly);


    /**
     * Sets a float3 uniform parameter (for all feature permutations)
     *
     * @param name name of uniform for (float3)
     * @param buffer write 3 entries from {@link FloatBuffer} into uniform
     */
    public abstract void setFloat3(String name, FloatBuffer buffer);

    /**
     * Sets a float3 uniform parameter
     *
     * @param name name of uniform for (float3)
     * @param buffer write 3 entries from {@link FloatBuffer} into uniform
     * @param currentOnly determines if written to all permutations
     */
    public abstract void setFloat3(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a float4 uniform parameter (for all feature permutations)
     *
     * @param name  name of uniform for (float4)
     * @param f1 the x component
     * @param f2 the y component
     * @param f3 the z component
     * @param f4 the w component
     */
    public abstract void setFloat4(String name, float f1, float f2, float f3, float f4);

    /**
     * Sets a float4 uniform parameter
     *
     * @param name name of uniform for (float4)
     * @param f1 the x component
     * @param f2 the y component
     * @param f3 the z component
     * @param f4 the w component
     * @param currentOnly determines if written to all permutations
     */
    public abstract void setFloat4(String name, float f1, float f2, float f3, float f4, boolean currentOnly);

    /**
     * Sets a float4 uniform parameter
     *
     * @param name name of uniform for (float4)
     * @param value the {@link Vector4fc} to write into uniform
     */
    public abstract void setFloat4(String name, Vector4fc value);

    /**
     * Sets a float4 uniform parameter
     *
     * @param name name of uniform for (float4)
     * @param value the {@link Vector4fc} to write into uniform
     * @param currentOnly determines if written to all permutations
     */
    public abstract void setFloat4(String name, Vector4fc value, boolean currentOnly);


    /**
     * Sets a float4 uniform parameter (for all feature permutations)
     *
     * @param name name of uniform for (float4)
     * @param buffer write 4 entries from {@link FloatBuffer} into uniform
     */
    public abstract void setFloat4(String name, FloatBuffer buffer);

    /**
     *
     * @param name name of uniform for (float4)
     * @param buffer
     * @param currentOnly determines if written to all permutations
     */
    public abstract void setFloat4(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets an int uniform parameter (for all feature permutations)
     *
     * @param name name of uniform for (int)
     * @param i value
     */
    public abstract void setInt(String name, int i);

    /**
     *
     * @param name name of uniform for (int)
     * @param i Sets a int uniform parameter [0..1]
     * @param currentOnly determines if written to all permutations
     */
    public abstract void setInt(String name, int i, boolean currentOnly);

    /**
     * Sets a boolean (int 1 or 0) uniform parameter (for all feature permutations}
     *
     * @param name name of uniform for (int)
     * @param value  Sets a int uniform parameter [0..1]
     */
    public abstract void setBoolean(String name, boolean value);

    /**
     *
     * @param name uniform parameter
     * @param value name of uniform for (int)
     * @param currentOnly determines if written to all permutations
     */
    public abstract void setBoolean(String name, boolean value, boolean currentOnly);

    /**
     * Sets a matrix3 uniform parameter (for all feature permutations)
     *
     * @param name uniform parameter (mat3)
     * @param matrix the {@link Matrix3fc} to write into uniform
     */
    public abstract void setMatrix3(String name, Matrix3fc matrix);


    /**
     * Sets a matrix3 uniform parameter (for all feature permutations)
     *
     * @param name uniform parameter (mat3)
     * @param matrix  write 9 entries from {@link FloatBuffer} into uniform
     */
    public abstract void setMatrix3(String name, Matrix3fc matrix, boolean currentOnly);


    /**
     * Sets a matrix3 uniform parameter (for all feature permutations)
     *
     * @param name uniform parameter (mat3)
     * @param buffer  write 9 entries from {@link FloatBuffer} into uniform
     */
    public abstract void setMatrix3(String name, FloatBuffer buffer);

    /**
     *
     * @param name uniform parameter (mat3)
     * @param buffer  write 9 entries from {@link FloatBuffer} into uniform
     * @param currentOnly determines if written to all permutations
     */
    public abstract void setMatrix3(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a matrix4 uniform parameter (for all feature permutations)
     *
     * @param name uniform parameter (mat4)
     * @param matrix  write 16 entries from {@link FloatBuffer} into uniform
     */
    public abstract void setMatrix4(String name, Matrix4fc matrix);

    /**
     * Sets a matrix4 uniform parameter (for all feature permutations)
     *
     * @param name uniform parameter (mat4)
     * @param matrix the {@link Matrix4fc} to write into uniform
     */
    public abstract void setMatrix4(String name, Matrix4fc matrix, boolean currentOnly);

    /**
     * Sets a matrix3 uniform parameter (for all feature permutations)
     *
     * @param name uniform parameter (mat4)
     * @param buffer write 16 entries from {@link FloatBuffer} into uniform
     */
    public abstract void setMatrix4(String name, FloatBuffer buffer);

    /**
     *
     * @param name uniform parameter (mat4)
     * @param buffer write 16 entries from {@link FloatBuffer} into uniform
     * @param currentOnly determines if written to all permutations
     */
    public abstract void setMatrix4(String name, FloatBuffer buffer, boolean currentOnly);

    /**
     * Sets a texture parameter
     *
     * @param name uniform parameter (sampler2D)
     * @param texture
     */
    public abstract void setTexture(String name, Texture texture);

    /**
     * Sets up this material for the given camera to the current material
     *
     * @param camera camera to write into material
     */
    public abstract void setCamera(Camera camera);

    /**
     * Checks if a feature of this material, if available
     *
     * @param feature program feature
     * @return is supported
     */
    public abstract boolean supportsFeature(ShaderProgramFeature feature);

    /**
     * Activates a feature of this material, if available
     *
     * @param feature program feature
     */
    public abstract void activateFeature(ShaderProgramFeature feature);

    /**
     * Deactivates a feature of this material if active
     *
     * @param feature program feature
     */
    public abstract void deactivateFeature(ShaderProgramFeature feature);

    /**
     * Deactivates a set of features of this material
     *
     * @param features program feature
     */
    public abstract void deactivateFeatures(ShaderProgramFeature... features);

    public abstract void bindTextures();
}
