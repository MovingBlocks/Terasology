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
package org.terasology.components;

import org.terasology.entitySystem.Component;

import javax.vecmath.Vector3f;

/**
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 */
public class LightComponent implements Component {

    public enum LightType {
        POINT,
        DIRECTIONAL
    }

    public Vector3f lightColorDiffuse = new Vector3f(1.0f, 1.0f, 1.0f);
    public Vector3f lightColorAmbient = new Vector3f(1.0f, 1.0f, 1.0f);

    public float lightDiffuseIntensity = 2.0f;
    public float lightSpecularIntensity = 0.1f;
    public float lightAmbientIntensity = 1.0f;

    public float lightSpecularPower = 4.0f;
    public float lightAttenuationRange = 16.0f;
    public float lightAttenuationFalloff = 0.5f;

    // The rendering distance for light components (0.0f == Always render the light)
    public float lightRenderingDistance = 0.0f;

    public LightType lightType = LightType.POINT;
}
