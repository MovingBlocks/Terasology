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

#if defined (DYNAMIC_SHADOWS)
uniform vec4 shadowSettingsFrag;
#define shadowIntens shadowSettingsFrag.x
#define shadowMapBias shadowSettingsFrag.y

uniform sampler2D texSceneShadowMap;

uniform mat4 invViewProjMatrix;
uniform mat4 lightViewProjMatrix;

uniform vec3 mainCameraToLightSpace;
#endif


void main() {
    vec4 colorOpaque = texture2D(texSceneOpaque, gl_TexCoord[0].xy);
    float depthOpaque = texture2D(texSceneOpaqueDepth, gl_TexCoord[0].xy).r * 2.0 - 1.0;
    vec4 normalsOpaque = texture2D(texSceneOpaqueNormals, gl_TexCoord[0].xy);
    vec4 colorTransparent = texture2D(texSceneTransparent, gl_TexCoord[0].xy);
    vec4 lightBufferOpaque = texture2D(texSceneOpaqueLightBuffer, gl_TexCoord[0].xy);

    // TODO: Move SSAO and outline stuff to LightBufferPass so it is available for refraction

    // Sky inscattering using down-sampled sky band texture
    vec3 skyInscatteringColor = texture2D(texSceneSkyBand, gl_TexCoord[0].xy).rgb;

    float d = abs(linDepthViewingDistance(depthOpaque));
    float fogValue = clamp(((skyInscatteringLength - d) / (skyInscatteringLength - skyInscatteringThreshold)) * skyInscatteringStrength, 0.0, 1.0);

    // No scattering in the sky please - otherwise we end up with an ugly blurry sky
    if (depthOpaque < 1.0) {
        colorOpaque = mix(colorOpaque, vec4(skyInscatteringColor, 1.0), fogValue);
        colorTransparent = mix(colorTransparent, vec4(skyInscatteringColor, 1.0), fogValue);
    }

#ifdef SSAO
    float ssao = texture2D(texSsao, gl_TexCoord[0].xy).x;
    colorOpaque.rgb *= mix(ssao, 1.0, fogValue);
#endif

#ifdef OUTLINE
    vec3 screenSpaceNormal = normalsOpaque.xyz * 2.0 - 1.0;
    float outlineFadeFactor = (1.0 - fogValue) * (1.0 - abs(screenSpaceNormal.y)); // Use the normal to avoid artifacts on flat wide surfaces

    float outline = step(outlineDepthThreshold, texture2D(texEdges, gl_TexCoord[0].xy).x) * outlineThickness * outlineFadeFactor;
    colorOpaque.rgb = mix(colorOpaque.rgb, vec3(OUTLINE_COLOR), outline);
#endif

#if defined (DYNAMIC_SHADOWS)
    // TODO: As costly as in the deferred light geometry pass - frustum ray method would be great here
    vec3 worldPosition = reconstructViewPos(depthOpaque, gl_TexCoord[0].xy, invViewProjMatrix);
    worldPosition.xyz += mainCameraToLightSpace;

	vec4 lightProjPos = lightViewProjMatrix * vec4(worldPosition.x, worldPosition.y, worldPosition.z, 1.0);

    vec3 lightPosClipSpace = lightProjPos.xyz / lightProjPos.w;
    vec2 shadowMapTexPos = lightPosClipSpace.xy * vec2(0.5) + vec2(0.5);

    float shadowMapDepth = texture2D(texSceneShadowMap, shadowMapTexPos).x;

    float shadowTerm = 1.0;
    if (lightPosClipSpace.z < 1.0 && shadowMapDepth < lightPosClipSpace.z - shadowMapBias) {
        shadowTerm = shadowIntens;
    }

    colorOpaque.rgb *= mix(shadowTerm, 1.0, fogValue);
#endif

    float fade = clamp(1.0 - colorTransparent.a, 0.0, 1.0);
    vec4 color = mix(colorTransparent, colorOpaque, fade);

    gl_FragData[0].rgba = color.rgba;
    gl_FragData[1].rgba = normalsOpaque.rgba;
    gl_FragData[2].rgba = lightBufferOpaque.rgba;
    gl_FragDepth = depthOpaque * 0.5 + 0.5;
}
