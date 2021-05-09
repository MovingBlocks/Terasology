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

varying vec4 vertexProjPos;

uniform vec3 lightViewPos;

uniform sampler2D texSceneOpaqueDepth;
uniform sampler2D texSceneOpaqueNormals;
#if defined (FEATURE_LIGHT_DIRECTIONAL)
uniform sampler2D texSceneOpaqueLightBuffer;
#endif

uniform vec3 lightColorDiffuse = vec3(1.0, 0.0, 0.0);
uniform vec3 lightColorAmbient = vec3(1.0, 0.0, 0.0);

uniform vec3 lightProperties;
#define lightAmbientIntensity lightProperties.x
#define lightDiffuseIntensity lightProperties.y
#define lightSpecularPower lightProperties.z

uniform vec4 lightExtendedProperties;
#define lightAttenuationRange lightExtendedProperties.x
#define lightAttenuationFalloff lightExtendedProperties.y

uniform mat4 invProjMatrix;

#if defined (DYNAMIC_SHADOWS)
#if defined (CLOUD_SHADOWS)
uniform sampler2D texSceneClouds;
#endif

#define SHADOW_MAP_BIAS 0.01

#if defined (FEATURE_LIGHT_DIRECTIONAL)
uniform sampler2D texSceneShadowMap;
#endif

uniform mat4 lightViewProjMatrix;

uniform vec3 activeCameraToLightSpace;

uniform mat4 invViewProjMatrix;
#endif

void main() {

#if defined (FEATURE_LIGHT_POINT)
    vec2 projectedPos = projectVertexToTexCoord(vertexProjPos);
#elif defined (FEATURE_LIGHT_DIRECTIONAL)
    vec2 projectedPos = gl_TexCoord[0].xy;
#else
    vec2 projectedPos = vec2(0.0);
#endif

    vec4 normalBuffer = texture2D(texSceneOpaqueNormals, projectedPos.xy).rgba;
    vec3 normal = normalize(normalBuffer.xyz * 2.0 - 1.0);
    float shininess = normalBuffer.a;
    vec4 depthBuffer = texture2D(texSceneOpaqueDepth, projectedPos.xy).rgba;
    float depth = depthBuffer.r * 2.0 - 1.0;

#if defined (DYNAMIC_SHADOWS) && defined (FEATURE_LIGHT_DIRECTIONAL)
    // TODO: Uhhh... Doing this twice here :/ Frustum ray would be better!
    vec3 worldPosition = reconstructViewPos(depth, gl_TexCoord[0].xy, invViewProjMatrix);
    vec3 lightWorldPosition = worldPosition.xyz + activeCameraToLightSpace;

    vec4 lightProjVertPos = lightViewProjMatrix * vec4(lightWorldPosition.x, lightWorldPosition.y, lightWorldPosition.z, 1.0);

    lightProjVertPos.xyz /= lightProjVertPos.w;
    vec2 shadowMapTexPos = lightProjVertPos.xy * vec2(0.5) + vec2(0.5);

    float shadowTerm = 1.0;

    if (!epsilonEqualsOne(depth)) {
#if defined (DYNAMIC_SHADOWS_PCF)
        shadowTerm = calcPcfShadowTerm(texSceneShadowMap, lightProjVertPos.z, shadowMapTexPos, 0.0, SHADOW_MAP_BIAS);
#else
        float shadowMapDepth = texture2D(texSceneShadowMap, shadowMapTexPos).x;
        if (shadowMapDepth + SHADOW_MAP_BIAS < lightProjVertPos.z) {
            shadowTerm = 0.0;
        }
#endif

#if defined (CLOUD_SHADOWS) && !defined (VOLUMETRIC_LIGHTING)
        // TODO: Add shader parameters for this...
        // Get the preconfigured value from the randomized texture, sampling a value from it to determine how much cloud shadow there will be.
        // Clamp the value so that clouds do not turn the surface black.
        float cloudOcclusion = clamp(texture2D(texSceneClouds, (worldPosition.xz + cameraPosition.xz) * 0.005 + timeToTick(time, 0.002)).r * 10,0.6,1);

        // Combine the cloud shadow with the dynamic shadows
        shadowTerm *= rescaleRange(cloudOcclusion,0,1,0,shadowTerm);
#endif
   }
#endif

    // TODO: Costly - would be nice to use Crytek's view frustum ray method at this point
    vec3 viewSpacePos = reconstructViewPos(depth, projectedPos, invProjMatrix);

    vec3 lightDir;
#if defined (FEATURE_LIGHT_POINT)
    lightDir = lightViewPos.xyz - viewSpacePos;
#elif defined (FEATURE_LIGHT_DIRECTIONAL)
    lightDir = lightViewPos.xyz;
#endif

    vec3 eyeVec = -normalize(viewSpacePos.xyz).xyz;

    float lightDist = length(lightDir);
    vec3 lightDirNorm = lightDir / lightDist;

    float ambTerm = lightAmbientIntensity;
    float lambTerm = clamp(max(0.0, dot(normal, lightDirNorm)), 0, 1);

    // Apply "back light"
    // TODO: Make intensity a shader parameter
    const float backLightIntens = 0.5;
    lambTerm += max(0.0, dot(normal, -lightDirNorm)) * backLightIntens;

    float specTerm  = calcSpecLightNormalized(normal, lightDirNorm, eyeVec, lightSpecularPower);

#if defined (DYNAMIC_SHADOWS) && defined (FEATURE_LIGHT_DIRECTIONAL)
    // ensure that the shadow does not make the surface completely black
    shadowTerm = clamp(shadowTerm, 0.5, 1.0);

    lambTerm *= shadowTerm;
    specTerm *= shadowTerm;
    // TODO: Add this as a shader parameter...
    ambTerm *= shadowTerm;
#endif

    float specular = shininess * specTerm;

#if defined (FEATURE_LIGHT_POINT)
    vec3 color = ambTerm * lightColorAmbient;
    color *= lightColorDiffuse * lightDiffuseIntensity * lambTerm;
#elif defined (FEATURE_LIGHT_DIRECTIONAL)
    vec4 lightBuffer = texture2D(texSceneOpaqueLightBuffer, projectedPos.xy);
    float sunlightIntensity = lightBuffer.y;
    vec3 color = calcSunlightColorDeferred(sunlightIntensity, lambTerm, ambTerm, lightDiffuseIntensity, lightColorAmbient, lightColorDiffuse);
#else
    vec3 color = vec3(1.0, 0.0, 1.0);
#endif

#if defined (FEATURE_LIGHT_POINT)

    // calculate basic attenuation
    // realistic attenuation ref: https://imdoingitwrong.wordpress.com/2011/01/31/light-attenuation/
    float denom = lightDist/lightAttenuationRange + 1;
    float attenuation = 1.0/(denom*denom);

    // Force the light to gradually come to a complete stop at the attenuation range + falloff distance.
    float lightDistPastRange = max(lightDist - lightAttenuationRange, 0.0);
    float falloffTerm = 1.0 - min(lightDistPastRange / lightAttenuationFalloff, 1.0);
    attenuation *= falloffTerm;

    attenuation = max(attenuation,0);

    specular *= attenuation * max(dot(lightDir/ lightDist, normal), 0);
    color *= attenuation * max(dot(lightDir/ lightDist, normal), 0);
#endif

// TODO A 3D wizard should take a look at this. Configurable for the moment to make better comparisons possible.
#if defined (CLAMP_LIGHTING)
    gl_FragData[0].rgba = clamp(vec4(color.r, color.g, color.b, specular), 0.0, 1.0);
#else
    gl_FragData[0].rgba = vec4(color.r, color.g, color.b, specular);
#endif
}
