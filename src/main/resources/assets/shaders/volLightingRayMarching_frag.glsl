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

//#define INTERLEAVED_SAMPLING
#if defined (VOLUMETRIC_LIGHTING)

uniform sampler2D texSceneOpaqueDepth;

#define SAMPLES 300.0
#define STARTING_POINT 128.0 // defines the amount of samples used / the starting point
#define STEP_SIZE STARTING_POINT / SAMPLES

uniform vec2 texelSize;

uniform vec4 volumetricLightingSettings;

uniform mat4 invViewProjMatrix;
uniform mat4 lightViewMatrix;
uniform mat4 lightProjMatrix;
uniform mat4 lightViewProjMatrix;

uniform sampler2D texSceneShadowMap;
uniform vec3 activeCameraToLightSpace;

#if defined (CLOUD_SHADOWS)
uniform sampler2D texSceneClouds;
#endif

void main() {
    float depthOpaque = texture2D(texSceneOpaqueDepth, gl_TexCoord[0].xy).r * 2.0 - 1.0;

    // TODO: As costly as in the deferred light geometry pass - frustum ray method would be great here
    vec3 worldPosition = reconstructViewPos(depthOpaque, gl_TexCoord[0].xy, invViewProjMatrix);

    vec2 projectedPos = gl_TexCoord[0].xy;

    // Guess a position for fragments that have been projected to the far plane (e.g. the sky)
    float len = length(worldPosition.xyz);
    worldPosition.xyz = clamp(len, 0.0,  STARTING_POINT) * (worldPosition.xyz / len);

    vec3 lightWorldSpaceVertPos = worldPosition.xyz + activeCameraToLightSpace;

//    float L = volumetricLightingL0 * exp(-STARTING_POINT * volumetricLightingTau);
    float L = 0.0;

    vec4 lightViewSpaceVertPos = lightViewMatrix * vec4(lightWorldSpaceVertPos.x, lightWorldSpaceVertPos.y, lightWorldSpaceVertPos.z, 1.0);
    vec3 viewDir = normalize(-lightViewSpaceVertPos.xyz);
    vec4 viewSpacePosition = lightViewSpaceVertPos;

#if defined (INTERLEAVED_SAMPLING)
    const float samplingPixelArea = 8; // ^2
    const float stepSize = STEP_SIZE * 64.0;
    float startingPoint =
        STARTING_POINT - (mod(gl_TexCoord[0].x / texelSize.x, samplingPixelArea) * (samplingPixelArea - 1) + mod(gl_TexCoord[0].y / texelSize.y, samplingPixelArea)) * STEP_SIZE;
#else
    const float stepSize = STEP_SIZE;
    const float startingPoint = STARTING_POINT;
#endif

    for (float l = startingPoint - stepSize; l >= 0; l -= stepSize) {
        viewSpacePosition.xyz += stepSize * viewDir.xyz;

        vec4 screenSpacePosition = lightProjMatrix * viewSpacePosition;
        screenSpacePosition.xyz /= screenSpacePosition.w;
        screenSpacePosition.xy = screenSpacePosition.xy * vec2(0.5, 0.5) + vec2(0.5, 0.5);

        float sd = texture2D(texSceneShadowMap, screenSpacePosition.xy).x;
        float v = (sd < screenSpacePosition.z) ? 0.0 : volumetricLightingSettings.x;

#if defined (CLOUD_SHADOWS)
        // Modulate volumetric lighting with some fictional cloud shadows
        v *= clamp(1.0 - texture2D(texSceneClouds, screenSpacePosition.xy * 4.0 + timeToTick(time, 0.002)).r * 5.0, 0.0, 1.0);
#endif

        float d = clamp(length(viewSpacePosition.xyz), 0.1, startingPoint);
        float cosTheta = dot(normalize(-viewSpacePosition.xyz), -viewDir);
        float g = volumetricLightingSettings.w;
        float phi = volumetricLightingSettings.z;

        float henyeyGreenstein = (1.0 / (4.0 * PI)) * (1.0 - g) * (1.0 - g);
        henyeyGreenstein /= pow(1.0 + g*g - 2.0 * g * cosTheta, 3.0 / 2.0);

        float intens = henyeyGreenstein * volumetricLightingSettings.y * (v * phi/4.0/PI/d/d) * exp(-d * volumetricLightingSettings.y) * exp(-l * volumetricLightingSettings.y) * stepSize;

        L += intens;
    };

    gl_FragData[0].rgba = vec4(L, L, L, 1.0);
}

#endif
