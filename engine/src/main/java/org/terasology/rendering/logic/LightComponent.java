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

import org.terasology.reflection.metadata.FieldMetadata;
import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.Replicate;
import org.terasology.network.ReplicationCheck;

/**
 */
// TODO: Split into multiple components? Point, Directional?
public final class LightComponent implements Component, ReplicationCheck {

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
    public float lightSpecularIntensity = 0.1f;
    @Replicate
    public float lightAmbientIntensity = 1.0f;

    @Replicate
    public float lightSpecularPower = 1.0f;
    @Replicate
    public float lightAttenuationRange = 16.0f;
    @Replicate
    public float lightAttenuationFalloff = 1.25f;

    // The rendering distance for light components (0.0f == Always render the light)
    @Replicate
    public float lightRenderingDistance;

    @Replicate
    public LightType lightType = LightType.POINT;

    public boolean simulateFading;


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
