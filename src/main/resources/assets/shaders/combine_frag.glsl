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

uniform vec4 skyInscatteringSettingsFrag;
#define skyInscatteringStrength skyInscatteringSettingsFrag.y
#define skyInscatteringLength skyInscatteringSettingsFrag.z
#define skyInscatteringThreshold skyInscatteringSettingsFrag.w

uniform sampler2D texSceneSkyBand;

#ifdef SSAO
uniform sampler2D texSsao;
#endif
#ifdef OUTLINE
uniform sampler2D texEdges;

uniform float outlineDepthThreshold;
uniform float outlineThickness;

# define OUTLINE_COLOR 0.0, 0.0, 0.0
#endif

#if defined (DYNAMIC_SHADOWS) || defined (VOLUMETRIC_FOG)
uniform mat4 invViewProjMatrix;
#endif

#if defined (DYNAMIC_SHADOWS)
uniform vec4 shadowSettingsFrag;
#define shadowIntens shadowSettingsFrag.x
#define shadowMapBias shadowSettingsFrag.y

uniform sampler2D texSceneShadowMap;
uniform mat4 lightViewProjMatrix;

uniform vec3 activeCameraToLightSpace;
#endif

#if defined (VOLUMETRIC_FOG)
#define VOLUMETRIC_FOG_COLOR 1.0, 1.0, 1.0

uniform vec4 volumetricFogSettings;
#define volFogDensityAtViewer volumetricFogSettings.x
#define volFogGlobalDensity volumetricFogSettings.y
#define volFogHeightFalloff volumetricFogSettings.z

uniform vec3 fogWorldPosition;
#endif

void main() {
    vec4 colorOpaque = texture2D(texSceneOpaque, gl_TexCoord[0].xy);
    float depthOpaque = texture2D(texSceneOpaqueDepth, gl_TexCoord[0].xy).r * 2.0 - 1.0;
    vec4 normalsOpaque = texture2D(texSceneOpaqueNormals, gl_TexCoord[0].xy);
    vec4 colorTransparent = texture2D(texSceneTransparent, gl_TexCoord[0].xy);
    vec4 lightBufferOpaque = texture2D(texSceneOpaqueLightBuffer, gl_TexCoord[0].xy);

#if defined (DYNAMIC_SHADOWS) || defined (VOLUMETRIC_FOG)
    // TODO: As costly as in the deferred light geometry pass - frustum ray method would be great here
    vec3 worldPosition = reconstructViewPos(depthOpaque, gl_TexCoord[0].xy, invViewProjMatrix);
#endif

#ifdef SSAO
    float ssao = texture2D(texSsao, gl_TexCoord[0].xy).x;

    if (depthOpaque < 1.0) { // Don't bleed in the sky
        colorOpaque.rgb *= ssao;
    }
#endif

// TODO: Move SSAO, shadow and outline stuff to LightBufferPass so it is available for refraction

#ifdef OUTLINE
    vec3 screenSpaceNormal = normalsOpaque.xyz * 2.0 - 1.0;
    float outlineFadeFactor = (1.0 - abs(screenSpaceNormal.y)); // Use the normal to avoid artifacts on flat wide surfaces

    float outline = step(outlineDepthThreshold, texture2D(texEdges, gl_TexCoord[0].xy).x) * outlineThickness * outlineFadeFactor;
    colorOpaque.rgb = mix(colorOpaque.rgb, vec3(OUTLINE_COLOR), outline);
#endif

#if defined (DYNAMIC_SHADOWS)
    vec3 lightWorldPosition = worldPosition.xyz + activeCameraToLightSpace;

	vec4 lightProjPos = lightViewProjMatrix * vec4(lightWorldPosition.x, lightWorldPosition.y, lightWorldPosition.z, 1.0);

    vec3 lightPosClipSpace = lightProjPos.xyz / lightProjPos.w;
    vec2 shadowMapTexPos = lightPosClipSpace.xy * vec2(0.5) + vec2(0.5);

    float shadowTerm = 1.0;

    if (depthOpaque < 1.0) {
#if defined (DYNAMIC_SHADOWS_PCF)
        shadowTerm = calcPcfShadowTerm(texSceneShadowMap, lightPosClipSpace.z, shadowMapTexPos, shadowIntens, shadowMapBias);
#else
        float shadowMapDepth = texture2D(texSceneShadowMap, shadowMapTexPos).x;
        if (shadowMapDepth + shadowMapBias < lightPosClipSpace.z) {
            shadowTerm = shadowIntens;
        }
#endif
        colorOpaque.rgb *= shadowTerm;
#endif
    }

    // Sky inscattering using down-sampled sky band texture
    vec3 skyInscatteringColor = texture2D(texSceneSkyBand, gl_TexCoord[0].xy).rgb;

    float d = abs(linDepthViewingDistance(depthOpaque));
    float fogValue = clamp(((skyInscatteringLength - d) / (skyInscatteringLength - skyInscatteringThreshold)) * skyInscatteringStrength, 0.0, 1.0);

    // No scattering in the sky please - otherwise we end up with an ugly blurry sky
    if (depthOpaque < 1.0) {
        colorOpaque.rgb = mix(colorOpaque.rgb, skyInscatteringColor, fogValue);
        colorTransparent.rgb = mix(colorTransparent.rgb, skyInscatteringColor, fogValue);
    }

#if defined (VOLUMETRIC_FOG)
    // Use lightValueAtPlayerPos to avoid volumetric fog in caves
    float volumetricFogValue = sunlightValueAtPlayerPos * calcVolumetricFog(worldPosition - fogWorldPosition, volFogDensityAtViewer, volFogGlobalDensity, volFogHeightFalloff);

    vec3 volFogColor = skyInscatteringColor * vec3(VOLUMETRIC_FOG_COLOR);
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
