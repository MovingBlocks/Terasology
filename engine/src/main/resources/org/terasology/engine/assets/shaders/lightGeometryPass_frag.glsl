#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

in vec2 v_uv0;
in vec4 v_vertexProjPos;

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


#if defined (DYNAMIC_SHADOWS)
    #if defined (CLOUD_SHADOWS)
    uniform sampler2D texSceneClouds;
    #endif

    #define SHADOW_MAP_BIAS 0.0001

    #if defined (FEATURE_LIGHT_DIRECTIONAL)
    uniform sampler2D texSceneShadowMap;
    #endif

    uniform vec3 activeCameraToLightSpace;
    uniform mat4 lightMatrix;
    uniform mat4 invViewProjMatrix;
#endif

uniform mat4 invProjMatrix;

void main() {

#if defined (FEATURE_LIGHT_POINT)
    vec2 projectedPos = projectVertexToTexCoord(v_vertexProjPos);
#elif defined (FEATURE_LIGHT_DIRECTIONAL)
    vec2 projectedPos = v_uv0.xy;
#else
    vec2 projectedPos = vec2(0.0);
#endif

    vec4 normalBuffer = texture2D(texSceneOpaqueNormals, projectedPos.xy).rgba;
    vec3 normal = normalize(normalBuffer.xyz * 2.0 - 1.0);
    float shininess = normalBuffer.a;
    highp float depth = texture2D(texSceneOpaqueDepth, projectedPos.xy).r * 2.0 - 1.0;

    vec3 lightDir;
    // TODO: Costly - would be nice to use Crytek's view frustum ray method at this point
    vec3 viewSpacePos = reconstructViewPos(depth, projectedPos, invProjMatrix);

#if defined (FEATURE_LIGHT_POINT)
    lightDir = lightViewPos.xyz - viewSpacePos;
#elif defined (FEATURE_LIGHT_DIRECTIONAL)
    lightDir = lightViewPos.xyz;
#endif


#if defined (DYNAMIC_SHADOWS) && defined (FEATURE_LIGHT_DIRECTIONAL)
    // TODO: Uhhh... Doing this twice here :/ Frustum ray would be better!
    vec3 worldPosition = reconstructViewPos(depth, v_uv0.xy, invViewProjMatrix);
    vec3 lightWorldPosition = worldPosition.xyz + activeCameraToLightSpace;

    vec4 shadowMapTexPos = lightMatrix * vec4(lightWorldPosition.x, lightWorldPosition.y, lightWorldPosition.z, 1.0);
    highp float shadowTerm = 0.0;
    highp float bias = max(SHADOW_MAP_BIAS * (1.0 - dot(normal, lightDir)), SHADOW_MAP_BIAS);

    #if defined (DYNAMIC_SHADOWS_PCF)
        vec2 texelSize = 1.0 / textureSize(texSceneShadowMap, 0);
        for(int x = -1; x <= 1; ++x) {
            for(int y = -1; y <= 1; ++y) {
                vec2 shadowPos = shadowMapTexPos.xy + vec2(x, y) * texelSize;
                highp float pcfDepth = texture(texSceneShadowMap, shadowPos).r;
                shadowTerm += (shadowMapTexPos.z + bias > pcfDepth) ? 0.0 : 1.0;
            }
        }
        shadowTerm /= 9.0;
    #else
        highp float pcfDepth = texture(texSceneShadowMap, shadowMapTexPos.xy).r;
        shadowTerm = (shadowMapTexPos.z + bias > pcfDepth) ? 0.0 : 1.0;
    #endif

    #if defined (CLOUD_SHADOWS) && !defined (VOLUMETRIC_LIGHTING)
        // TODO: Add shader parameters for this...
        // Get the preconfigured value from the randomized texture, sampling a value from it to determine how much cloud shadow there will be.
        // Clamp the value so that clouds do not turn the surface black.
        float cloudOcclusion = clamp(texture2D(texSceneClouds, (worldPosition.xz + cameraPosition.xz) * 0.005 + timeToTick(time, 0.002)).r * 10,0.6,1);

        // Combine the cloud shadow with the dynamic shadows
        shadowTerm *= rescaleRange(cloudOcclusion,0,1,0,shadowTerm);
    #endif
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
