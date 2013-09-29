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

uniform sampler2D texSceneOpaque;
uniform sampler2D texSceneOpaqueDepth;
uniform sampler2D texSceneOpaqueNormals;
uniform sampler2D texSceneOpaqueLightBuffer;
uniform sampler2D texSceneTransparent;

#ifdef INSCATTERING
uniform vec4 skyInscatteringSettingsFrag;
#define skyInscatteringStrength skyInscatteringSettingsFrag.y
#define skyInscatteringLength skyInscatteringSettingsFrag.z
#define skyInscatteringThreshold skyInscatteringSettingsFrag.w

uniform sampler2D texSceneSkyBand;
#endif

#ifdef SSAO
uniform sampler2D texSsao;
#endif
#ifdef OUTLINE
uniform sampler2D texEdges;

uniform float outlineDepthThreshold;
uniform float outlineThickness;

# define OUTLINE_COLOR 0.0, 0.0, 0.0
#endif

#if defined (VOLUMETRIC_FOG) || defined (VOLUMETRIC_LIGHTING)
uniform mat4 invViewProjMatrix;
#endif

#if defined (VOLUMETRIC_FOG)
#define VOLUMETRIC_FOG_COLOR 1.0, 1.0, 1.0

uniform vec4 volumetricFogSettings;
#define volFogDensityAtViewer volumetricFogSettings.x
#define volFogGlobalDensity volumetricFogSettings.y
#define volFogHeightFalloff volumetricFogSettings.z
#define volFogDensity volumetricFogSettings.w

uniform vec3 fogWorldPosition;
#endif

#if defined (VOLUMETRIC_LIGHTING)
#define SAMPLES 300.0
#define STARTING_POINT viewingDistance // defines the amount of samples used / the starting point
#define STEP_SIZE STARTING_POINT / SAMPLES

uniform vec4 volumetricLightingSettings;

uniform mat4 lightViewMatrix;
uniform mat4 lightProjMatrix;
uniform mat4 lightViewProjMatrix;

uniform mat4 viewMatrix;
uniform mat4 invViewMatrix;

uniform sampler2D texSceneShadowMap;
uniform vec3 activeCameraToLightSpace;

#if defined (CLOUD_SHADOWS)
uniform sampler2D texSceneClouds;
#endif
#endif

void main() {
    vec4 colorOpaque = texture2D(texSceneOpaque, gl_TexCoord[0].xy);
    float depthOpaque = texture2D(texSceneOpaqueDepth, gl_TexCoord[0].xy).r * 2.0 - 1.0;
    vec4 normalsOpaque = texture2D(texSceneOpaqueNormals, gl_TexCoord[0].xy);
    vec4 colorTransparent = texture2D(texSceneTransparent, gl_TexCoord[0].xy);
    vec4 lightBufferOpaque = texture2D(texSceneOpaqueLightBuffer, gl_TexCoord[0].xy);

#if defined (VOLUMETRIC_FOG) || defined (VOLUMETRIC_LIGHTING)
    // TODO: As costly as in the deferred light geometry pass - frustum ray method would be great here
    vec3 worldPosition = reconstructViewPos(depthOpaque, gl_TexCoord[0].xy, invViewProjMatrix);
#endif

#ifdef SSAO
    float ssao = texture2D(texSsao, gl_TexCoord[0].xy).x;
    colorOpaque.rgb *= ssao;
#endif

#ifdef OUTLINE
    vec3 screenSpaceNormal = normalsOpaque.xyz * 2.0 - 1.0;
    float outlineFadeFactor = (1.0 - abs(screenSpaceNormal.y)); // Use the normal to avoid artifacts on flat wide surfaces

    float outline = step(outlineDepthThreshold, texture2D(texEdges, gl_TexCoord[0].xy).x) * outlineThickness * outlineFadeFactor;
    colorOpaque.rgb = mix(colorOpaque.rgb, vec3(OUTLINE_COLOR), outline);
#endif

#if defined (INSCATTERING)
    // Sky inscattering using down-sampled sky band texture
    vec3 skyInscatteringColor = texture2D(texSceneSkyBand, gl_TexCoord[0].xy).rgb;

    float d = abs(linDepthViewingDistance(depthOpaque));
    float fogValue = clamp(((skyInscatteringLength - d) / (skyInscatteringLength - skyInscatteringThreshold)) * skyInscatteringStrength, 0.0, 1.0);

    // No scattering in the sky please - otherwise we end up with an ugly blurry sky
    if (!epsilonEqualsOne(depthOpaque)) {
        colorOpaque.rgb = mix(colorOpaque.rgb, skyInscatteringColor, fogValue);
        colorTransparent.rgb = mix(colorTransparent.rgb, skyInscatteringColor, fogValue);
    }
#endif

#if defined (VOLUMETRIC_LIGHTING)
    vec2 projectedPos = gl_TexCoord[0].xy;

    // Guess a position for fragments that have been projected to the far plane (e.g. the sky)
    float len = length(worldPosition.xyz);
    worldPosition.xyz = clamp(len, 0.0,  viewingDistance) * (worldPosition.xyz / len);

    vec3 lightWorldSpaceVertPos = worldPosition.xyz + activeCameraToLightSpace;

//    float L = volumetricLightingL0 * exp(-STARTING_POINT * volumetricLightingTau);
    float L = 0.0;

    vec4 lightViewSpaceVertPos = lightViewMatrix * vec4(lightWorldSpaceVertPos.x, lightWorldSpaceVertPos.y, lightWorldSpaceVertPos.z, 1.0);
    vec3 viewDir = normalize(-lightViewSpaceVertPos.xyz);
    vec4 viewSpacePosition = lightViewSpaceVertPos;

    for (float l = STARTING_POINT - STEP_SIZE; l >= 0; l -= STEP_SIZE) {
        viewSpacePosition.xyz += vec3(STEP_SIZE, STEP_SIZE, STEP_SIZE) * viewDir.xyz;

        vec4 screenSpacePosition = lightProjMatrix * viewSpacePosition;
        screenSpacePosition.xyz /= screenSpacePosition.w;
        screenSpacePosition.xy = screenSpacePosition.xy * vec2(0.5, 0.5) + vec2(0.5, 0.5);

        float sd = texture2D(texSceneShadowMap, screenSpacePosition.xy).x;
        float v = (sd < screenSpacePosition.z) ? 0.0 : volumetricLightingSettings.x;

#if defined (CLOUD_SHADOWS)
        // Modulate volumetric lighting with some fictional cloud shadows
        v *= clamp(1.0 - texture2D(texSceneClouds, screenSpacePosition.xy * 0.5 + timeToTick(time, 0.002)).r * 5.0, 0.0, 1.0);
#endif

        float d = clamp(length(viewSpacePosition.xyz), 10.0, viewingDistance);
        float cosTheta = dot(normalize(-viewSpacePosition.xyz), -viewDir);
        float g = volumetricLightingSettings.w;
        float phi = volumetricLightingSettings.z;

        float henyeyGreenstein = (1.0 / (4.0 * PI)) * (1.0 - g) * (1.0 - g);
        henyeyGreenstein /= pow(1.0 + g*g - 2.0 * g * cosTheta, 3.0 / 2.0);

        float intens = henyeyGreenstein * volumetricLightingSettings.y * (v * phi/4.0/PI/d/d) * exp(-d * volumetricLightingSettings.y) * exp(-l * volumetricLightingSettings.y) * STEP_SIZE;

        L += intens;
    };

    colorOpaque.rgb += vec3(L, L, L);
    colorTransparent.rgb += vec3(L, L, L);
#endif

#if defined (VOLUMETRIC_FOG)
    // Use lightValueAtPlayerPos to avoid volumetric fog in caves
    float volumetricFogValue = volFogDensity * sunlightValueAtPlayerPos *
        calcVolumetricFog(worldPosition - fogWorldPosition, volFogDensityAtViewer, volFogGlobalDensity, volFogHeightFalloff);

    vec3 volFogColor =
#if defined (INSCATTERING)
        skyInscatteringColor *
#endif
        vec3(VOLUMETRIC_FOG_COLOR);

    colorOpaque.rgb = mix(colorOpaque.rgb, volFogColor, volumetricFogValue);
    colorTransparent.rgb = mix(colorTransparent.rgb, volFogColor, volumetricFogValue);
#endif

    float fade = clamp(1.0 - colorTransparent.a, 0.0, 1.0);
    vec4 color = mix(colorTransparent, colorOpaque, fade);

    gl_FragData[0].rgba = color.rgba;
    gl_FragData[1].rgba = normalsOpaque.rgba;
    gl_FragData[2].rgba = lightBufferOpaque.rgba;
    gl_FragDepth = depthOpaque * 0.5 + 0.5;
}
