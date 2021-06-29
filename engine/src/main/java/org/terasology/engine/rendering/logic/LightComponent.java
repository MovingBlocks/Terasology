// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.logic;

import org.joml.Vector3f;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.network.ReplicationCheck;
import org.terasology.reflection.metadata.FieldMetadata;

/**
 * Add this component to an entity for it to transmit light from its location.
 * By default the component is configured to act similarly to a placed torch block.
 */
// TODO: Split into multiple components? Point, Directional?
public final class LightComponent implements VisualComponent<LightComponent>, ReplicationCheck {

    public enum LightType {
        POINT,
        DIRECTIONAL
    }

    @Replicate
    public Vector3f lightColorDiffuse = new Vector3f(1.0f, 1.0f, 1.0f);
    @Replicate
    public Vector3f lightColorAmbient = new Vector3f(1.0f, 1.0f, 1.0f);

    @Replicate
    public float lightDiffuseIntensity = 1.0f;
    @Replicate
    public float lightAmbientIntensity = 1.0f;

    /**
     * This helps control how focused the specular light is.
     * A smaller number will make a wider cone of light.
     * A larger number will make a narrower cone of light.
     */
    @Replicate
    public float lightSpecularPower = 80.0f;
    /**
     * Light attenuation range used in the calculation of how light fades from the light source as it gets farther away.
     * It is use in the following calculation:
     * <p>
     * attenuation = 1 / (lightDist/lightAttenuationRange + 1)^2
     * <p>
     * Where lightDist is how far the point in the world is from the light source.
     */
    @Replicate
    public float lightAttenuationRange = 10.0f;
    /**
     * After light travels the lightAttenuationRange, linearly fade to 0 light over this falloff distance.
     */
    @Replicate
    public float lightAttenuationFalloff = 1.25f;

    /**
     * The rendering distance for light components (0.0f == Always render the light)
     */
    @Replicate
    public float lightRenderingDistance;

    @Replicate
    public LightType lightType = LightType.POINT;

    public boolean simulateFading;

    @Override
    public void copy(LightComponent other) {
        this.lightColorDiffuse = new Vector3f(other.lightColorDiffuse);
        this.lightColorAmbient = new Vector3f(other.lightColorAmbient);
        this.lightDiffuseIntensity = other.lightDiffuseIntensity;
        this.lightAmbientIntensity = other.lightAmbientIntensity;
        this.lightSpecularPower = other.lightSpecularPower;
        this.lightAttenuationRange = other.lightAttenuationRange;
        this.lightAttenuationFalloff = other.lightAttenuationFalloff;
        this.lightRenderingDistance = other.lightRenderingDistance;
        this.lightType = other.lightType;
        this.simulateFading = other.simulateFading;
    }

    @Override
    public boolean shouldReplicate(FieldMetadata<?, ?> field, boolean initial, boolean toOwner) {
        switch (field.getName()) {
            case "lightDiffuseIntensity":
            case "lightAmbientIntensity":
                return initial || !simulateFading;
        }
        return true;
    }
}
