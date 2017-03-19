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
uniform sampler2D texSceneReflectiveRefractive;

#if defined (LOCAL_REFLECTIONS)
uniform sampler2D texSceneReflectiveRefractiveNormals;

uniform mat4 invProjMatrix;
uniform mat4 projMatrix;
#endif

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

#define OUTLINE_COLOR 0.0, 0.0, 0.0
#endif

#ifdef VOLUMETRIC_FOG
#define VOLUMETRIC_FOG_COLOR 1.0, 1.0, 1.0

uniform mat4 invViewProjMatrix;
uniform vec4 volumetricFogSettings;
#define volFogDensityAtViewer volumetricFogSettings.x
#define volFogGlobalDensity volumetricFogSettings.y
#define volFogHeightFalloff volumetricFogSettings.z
#define volFogDensity volumetricFogSettings.w

uniform vec3 fogWorldPosition;
#endif

void main() {
    vec4 colorOpaque = texture2D(texSceneOpaque, gl_TexCoord[0].xy);
    float depthOpaque = texture2D(texSceneOpaqueDepth, gl_TexCoord[0].xy).r * 2.0 - 1.0;
    vec4 normalOpaque = texture2D(texSceneOpaqueNormals, gl_TexCoord[0].xy);
    vec4 colorTransparent = texture2D(texSceneReflectiveRefractive, gl_TexCoord[0].xy);
    vec4 lightBufferOpaque = texture2D(texSceneOpaqueLightBuffer, gl_TexCoord[0].xy);

#ifdef VOLUMETRIC_FOG
    // TODO: As costly as in the deferred light geometry pass - frustum ray method would be great here
    vec3 worldPosition = reconstructViewPos(depthOpaque, gl_TexCoord[0].xy, invViewProjMatrix);
#endif

#if defined (LOCAL_REFLECTIONS)
    vec3 worldPositionViewSpace = reconstructViewPos(depthOpaque, gl_TexCoord[0].xy, invProjMatrix);

    vec4 transparentNormalColorValue = texture2D(texSceneReflectiveRefractiveNormals, gl_TexCoord[0].xy).xyzw;
    vec3 reflectionNormal = transparentNormalColorValue.xyz * 2.0 - 1.0;
    vec3 viewingDirection = normalize(worldPositionViewSpace.xyz);

    vec3 reflectionDirection = reflect(viewingDirection.xyz, reflectionNormal.xyz);

    // TODO: Move this some place else
#define SAMPLES_LOCAL_REFLECTION 64
#define RAY_MARCHING_DISTANCE 128.0
#define SAMPLE_STEP_SIZE (RAY_MARCHING_DISTANCE / SAMPLES_LOCAL_REFLECTION)
#define ANGLE_THRESHOLD 0.5
#define ANGLE_FADE_INTERVAL 0.1
#define EDGE_THRESHOLD 0.95
#define EDGE_FADE_INTERVAL 0.05

    vec3 viewSpaceRayPosition = worldPositionViewSpace;
    for (int i=0; i<SAMPLES_LOCAL_REFLECTION; ++i) {
        viewSpaceRayPosition += reflectionDirection * SAMPLE_STEP_SIZE;

        vec4 screenSpaceRayPosition = projMatrix * vec4(viewSpaceRayPosition.x, viewSpaceRayPosition.y, viewSpaceRayPosition.z, 1.0);
        screenSpaceRayPosition.xyz /= screenSpaceRayPosition.w;

        // Nahh... We don't want to touch anything outside of the screen
        // TODO: Maybe fade at the screen edges?
        if (abs(screenSpaceRayPosition.x) > 1.0 || abs(screenSpaceRayPosition.y) > 1.0) {
            break;
        }

        float newSampledDepth = texture2D(texSceneOpaqueDepth, screenSpaceRayPosition.xy * 0.5 + 0.5).r * 2.0 - 1.0;

        if (newSampledDepth < screenSpaceRayPosition.z) {
            float reflectionFadeFactor = transparentNormalColorValue.a;

            // TODO: Make this an option
            // Fades the reflection if the reflection vector is too steep
            if (reflectionDirection.y > ANGLE_THRESHOLD) {
                reflectionFadeFactor *= clamp(1.0 - (reflectionDirection.y - ANGLE_THRESHOLD) / ANGLE_FADE_INTERVAL, 0.0, 1.0);
            }

            // Fade out at the edges
            // TODO: Make this an option
            float rayLength = length(screenSpaceRayPosition.xy);
            if (rayLength > EDGE_THRESHOLD) {
                reflectionFadeFactor *= clamp(1.0 - (rayLength - EDGE_THRESHOLD) / EDGE_FADE_INTERVAL, 0.0, 1.0);
            }

            // TODO: Find a better way to do this... Using the previous frame buffer caused too much lag though
            vec4 tempColTransparent = texture2D(texSceneReflectiveRefractive, screenSpaceRayPosition.xy * 0.5 + 0.5).rgba;
            vec3 tempColOpaque = texture2D(texSceneOpaque, screenSpaceRayPosition.xy * 0.5 + 0.5).rgb;

            float fade = clamp(1.0 - tempColTransparent.a, 0.0, 1.0);
            vec3 reflectionColor = mix(tempColTransparent.rgb, tempColOpaque.rgb, fade);
            colorTransparent.rgb = mix(colorTransparent.rgb, reflectionColor, reflectionFadeFactor);
            break;
        }
    }
#endif

#ifdef SSAO
    float ssao = texture2D(texSsao, gl_TexCoord[0].xy).x;
    colorOpaque.rgb *= ssao;
#endif

#ifdef OUTLINE
    vec3 screenSpaceNormal = normalOpaque.xyz * 2.0 - 1.0;
    float outlineFadeFactor = (1.0 - abs(screenSpaceNormal.y)); // Use the normal to avoid artifacts on flat wide surfaces

    float outline = step(outlineDepthThreshold, texture2D(texEdges, gl_TexCoord[0].xy).x) * outlineThickness * outlineFadeFactor;
    colorOpaque.rgb = mix(colorOpaque.rgb, vec3(OUTLINE_COLOR), outline);
#endif

#ifdef INSCATTERING
    // No scattering in the sky please - otherwise we end up with an ugly blurry sky
    if (!epsilonEqualsOne(depthOpaque)) {
        // Sky inscattering using down-sampled sky band texture
        vec3 skyInscatteringColor = texture2D(texSceneSkyBand, gl_TexCoord[0].xy).rgb;

        float d = abs(linDepthViewingDistance(depthOpaque));

        float fogValue = clamp((1.0 - (skyInscatteringLength - d) / clamp(skyInscatteringLength - skyInscatteringThreshold, 0.0, 1.0)) * skyInscatteringStrength, 0.0, 1.0);

        colorOpaque.rgb = mix(colorOpaque.rgb, skyInscatteringColor, fogValue);
        colorTransparent.rgb = mix(colorTransparent.rgb, skyInscatteringColor, fogValue);
    }
#endif

#ifdef VOLUMETRIC_FOG
    // Use lightValueAtPlayerPos to avoid volumetric fog in caves
    float volumetricFogValue = volFogDensity *
        calcVolumetricFog(worldPosition - fogWorldPosition, volFogDensityAtViewer, volFogGlobalDensity, volFogHeightFalloff);

    vec3 volFogColor = vec3(VOLUMETRIC_FOG_COLOR);

    colorOpaque.rgb = mix(colorOpaque.rgb, volFogColor, volumetricFogValue);
    colorTransparent.rgb = mix(colorTransparent.rgb, volFogColor, volumetricFogValue);
#endif

    float fade = clamp(1.0 - colorTransparent.a, 0.0, 1.0);
    vec4 color = mix(colorTransparent, colorOpaque, fade);

    gl_FragData[0].rgba = color.rgba;
    gl_FragData[1].rgba = normalOpaque.rgba;
    gl_FragData[2].rgba = lightBufferOpaque.rgba;
    gl_FragDepth = depthOpaque * 0.5 + 0.5;
}
