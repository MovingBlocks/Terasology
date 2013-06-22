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

#define DAYLIGHT_AMBIENT_COLOR 1.0, 0.9, 0.9
#define MOONLIGHT_AMBIENT_COLOR 0.5, 0.5, 1.0
#define NIGHT_BRIGHTNESS 0.05
#define WATER_COLOR_SWIMMING 0.8, 1.0, 1.0, 0.975
#define WATER_TINT 0.1, 0.41, 0.627, 1.0

#define WATER_AMB 0.25
#define WATER_SPEC 4.0
#define WATER_DIFF 0.75

#define BLOCK_DIFF 0.75
#define BLOCK_AMB 2.0

#if defined (DYNAMIC_SHADOWS)
uniform vec4 shadowSettingsFrag;
#define shadowIntens shadowSettingsFrag.x
#define shadowMapBias shadowSettingsFrag.y

uniform sampler2D texSceneShadowMap;
varying vec4 vertexLightProjPos;
#endif

varying vec4 vertexWorldPos;
varying vec4 vertexViewPos;
varying vec4 vertexProjPos;
varying vec3 normal;

#ifdef FEATURE_TRANSPARENT_PASS
varying vec3 waterNormalViewSpace;
#endif

varying vec3 sunVecView;

varying float flickeringLightOffset;
varying float blockHint;
varying float isUpside;

varying float distance;

uniform vec4 lightingSettingsFrag;
#define waterSpecExp lightingSettingsFrag.z

uniform vec3 skyInscatteringColor;

uniform vec4 skyInscatteringSettingsFrag;
#define skyInscatteringExponent skyInscatteringSettingsFrag.x
#define skyInscatteringStrength skyInscatteringSettingsFrag.y
#define skyInscatteringLength skyInscatteringSettingsFrag.z
#define skyInscatteringThreshold skyInscatteringSettingsFrag.w

#ifdef FEATURE_TRANSPARENT_PASS
uniform vec4 waterSettingsFrag;
#define waterNormalBias waterSettingsFrag.x
#define waterRefraction waterSettingsFrag.y
#define waterFresnelBias waterSettingsFrag.z
#define waterFresnelPow waterSettingsFrag.w

uniform vec4 alternativeWaterSettingsFrag;
#define waterTint alternativeWaterSettingsFrag.x

uniform sampler2D textureWaterRefraction;
#endif

#ifdef FEATURE_TRANSPARENT_PASS
uniform sampler2D textureWaterNormal;
uniform sampler2D textureWaterReflection;
uniform sampler2D texSceneOpaque;
#endif

uniform sampler2D textureAtlas;
uniform sampler2D textureWater;
uniform sampler2D textureLava;
uniform sampler2D textureEffects;

uniform float clip;

void main(){
// Only necessary for opaque objects
#if !defined (FEATURE_TRANSPARENT_PASS)
	if (clip > 0.001 && vertexWorldPos.y < clip) {
        discard;
	}
#endif

    vec2 texCoord = gl_TexCoord[0].xy;

    vec3 normalizedVPos = -normalize(vertexViewPos.xyz);

#ifdef FEATURE_TRANSPARENT_PASS
    vec2 normalOffset;
    vec3 normalWater = waterNormalViewSpace;
    bool isWater = false;
    bool isOceanWater = false;

    if (checkFlag(BLOCK_HINT_WATER, blockHint)) {
        if (vertexWorldPos.y < 32.5 && vertexWorldPos.y > 31.5 && isUpside > 0.99) {
            vec2 waterOffset = vec2(vertexWorldPos.x + timeToTick(time, 0.1), vertexWorldPos.z + timeToTick(time, 0.1)) / 8.0;
            vec2 waterOffset2 = vec2(vertexWorldPos.x + timeToTick(time, 0.1), vertexWorldPos.z - timeToTick(time, 0.1)) / 16.0;

            normalOffset = (texture2D(textureWaterNormal, waterOffset).xyz * 2.0 - 1.0).xy;
            normalOffset += (texture2D(textureWaterNormal, waterOffset2).xyz * 2.0 - 1.0).xy;
            normalOffset *= 0.5 * (1.0 / vertexViewPos.z * waterNormalBias);

            normalWater.xy += normalOffset;
            normalWater = normalize(normalWater);

            isOceanWater = true;
        }

        isWater = true;
    }
#endif

    vec3 sunVecViewAdjusted = sunVecView;

    /* DAYLIGHT BECOMES... MOONLIGHT! */
    // Now featuring linear interpolation to make the transition smoother... :-)
    if (daylight < 0.1)
        sunVecViewAdjusted = mix(sunVecViewAdjusted * -1.0, sunVecViewAdjusted, daylight / 0.1);

    vec4 color = vec4(0.0, 0.0, 0.0, 1.0);

#if !defined (FEATURE_TRANSPARENT_PASS)
    if (checkFlag(BLOCK_HINT_LAVA, blockHint)) {
        texCoord.x = mod(texCoord.x, TEXTURE_OFFSET) * (1.0 / TEXTURE_OFFSET);
        texCoord.y = mod(texCoord.y, TEXTURE_OFFSET) / (128.0 / (1.0 / TEXTURE_OFFSET));
        texCoord.y += mod(timeToTick(time, -0.1), 127.0) * (1.0/128.0);

        color = texture2D(textureLava, texCoord.xy);
    /* APPLY DEFAULT TEXTURE FROM ATLAS */
    } else {
        color = texture2D(textureAtlas, texCoord.xy);

#if defined FEATURE_ALPHA_REJECT
        if (color.a < 0.5) {
            discard;
        }
#endif
    }

    /* APPLY OVERALL BIOME COLOR OFFSET */
    if (!checkFlag(BLOCK_HINT_GRASS, blockHint)) {
        if (gl_Color.r < 0.99 && gl_Color.g < 0.99 && gl_Color.b < 0.99) {
            if (color.g > 0.5) {
                color.rgb = vec3(color.g) * gl_Color.rgb;
            } else {
                color.rgb *= gl_Color.rgb;
            }

            color.a *= gl_Color.a;
        }
    /* MASK GRASS AND APPLY BIOME COLOR */
    } else {
        vec4 maskColor = texture2D(textureEffects, vec2(10.0 * TEXTURE_OFFSET_EFFECTS + mod(texCoord.x, TEXTURE_OFFSET_EFFECTS), mod(texCoord.y, TEXTURE_OFFSET_EFFECTS)));

        // Only use one channel so the color won't be altered
        if (maskColor.a != 0.0) color.rgb = vec3(color.g) * gl_Color.rgb;
    }
#endif

    // Calculate daylight lighting value
    float daylightValue = gl_TexCoord[1].x;
    float daylightScaledValue = daylight * daylightValue;

    // Calculate blocklight lighting value
    float blocklightDayIntensity = 1.0 - daylightScaledValue;
    float blocklightValue = gl_TexCoord[1].y;

    float occlusionValue = expOccValue(gl_TexCoord[1].z);
    float diffuseLighting;

#ifdef FEATURE_TRANSPARENT_PASS
    if (isOceanWater) {
        diffuseLighting = calcLambLight(normalWater, sunVecViewAdjusted);
    } else
#endif
    {
        diffuseLighting = calcLambLight(normal, sunVecViewAdjusted);
    }

#if defined (DYNAMIC_SHADOWS)
    float shadowTerm = 1.0;

    vec3 vertexLightPosClipSpace = vertexLightProjPos.xyz / vertexLightProjPos.w;
    vec2 shadowMapTexPos = vertexLightPosClipSpace.xy * vec2(0.5, 0.5) + vec2(0.5);
    float shadowMapDepth = texture2D(texSceneShadowMap, shadowMapTexPos).x;

    if (shadowMapDepth < vertexLightPosClipSpace.z - shadowMapBias) {
        shadowTerm = shadowIntens;
    }
#endif

    vec3 daylightColorValue;

    /* CREATE THE DAYLIGHT LIGHTING MIX */
#ifdef FEATURE_TRANSPARENT_PASS
    if (isOceanWater) {
        /* WATER NEEDS DIFFUSE AND SPECULAR LIGHT */
        daylightColorValue = vec3(diffuseLighting * WATER_DIFF + WATER_AMB);
        color.xyz += calcSpecLight(normalWater, sunVecViewAdjusted, normalizedVPos, waterSpecExp) * WATER_SPEC;
    } else
#endif
    {
        daylightColorValue = vec3(BLOCK_AMB + diffuseLighting * BLOCK_DIFF);
    }

    vec3 ambientTint = mix(vec3(MOONLIGHT_AMBIENT_COLOR), vec3(DAYLIGHT_AMBIENT_COLOR), daylight);
    daylightColorValue.xyz *= ambientTint;

    // Scale the lighting according to the daylight and daylight block values and add moonlight during the nights
    daylightColorValue.xyz *= daylightScaledValue + (NIGHT_BRIGHTNESS * (1.0 - daylight) * expLightValue(daylightValue));

    // Calculate the final block light brightness
    float blockBrightness = expLightValue(blocklightValue);
    blockBrightness -= flickeringLightOffset * blocklightValue;

    // Calculate the final blocklight color value and add a slight reddish tint to it
    vec3 blocklightColorValue = vec3(blockBrightness) * vec3(1.0, 0.95, 0.94);

    vec3 finalLightValue = max(daylightColorValue, blocklightColorValue);
#ifdef FEATURE_TRANSPARENT_PASS
    // Apply the final lighting mix
    color.xyz *= finalLightValue * occlusionValue;

    vec2 projectedPos = projectVertexToTexCoord(vertexProjPos);

    // Apply reflection and refraction AFTER the lighting has been applied (otherwise bright areas below water become dark)
    // The water tint has still to be adjusted adjusted though...
     if (isWater && isOceanWater) {
            vec4 reflectionColor = vec4(texture2D(textureWaterReflection, projectedPos + normalOffset.xy * waterRefraction).xyz, 1.0);
            vec4 refractionColor = vec4(texture2D(texSceneOpaque, projectedPos + normalOffset.xy * waterRefraction).xyz, 1.0);

            vec4 litWaterTint = vec4(WATER_TINT) * vec4(finalLightValue.x, finalLightValue.y, finalLightValue.z, 1.0);

            /* FRESNEL */
            if (!swimming) {
                float f = fresnel(dot(normalWater, normalizedVPos), waterFresnelBias, waterFresnelPow);
                color += mix(refractionColor * (1.0 - waterTint) +  waterTint * litWaterTint,
                    reflectionColor * (1.0 - waterTint) + waterTint * litWaterTint, f);
            } else {
                color += refractionColor * (1.0 - waterTint) +  waterTint * litWaterTint;
            }
     } else if (isWater) {
            texCoord.x = mod(texCoord.x, TEXTURE_OFFSET) * (1.0 / TEXTURE_OFFSET);
            texCoord.y = mod(texCoord.y, TEXTURE_OFFSET) / (128.0 / (1.0 / TEXTURE_OFFSET));
            texCoord.y += mod(timeToTick(time, -0.1), 127.0) * (1.0/128.0);

            vec4 albedoColor = texture2D(textureWater, texCoord.xy).rgba;
            albedoColor.rgb *= finalLightValue;

            vec3 refractionColor = texture2D(texSceneOpaque, projectedPos + albedoColor.rg * 0.05).rgb;

            color.rgb += mix(refractionColor, albedoColor.rgb, albedoColor.a);
    } else {
        vec3 refractionColor = texture2D(texSceneOpaque, projectedPos).rgb;
        vec4 albedoColor = texture2D(textureAtlas, texCoord.xy);
        albedoColor.rgb *= finalLightValue;

        // TODO: Add support for actual refraction here
        color.rgb += mix(refractionColor, albedoColor.rgb, albedoColor.a);
    }
#else
    gl_FragData[2].rgba = vec4(finalLightValue.r, finalLightValue.g, finalLightValue.b, 0.0);
#endif

#if defined (DYNAMIC_SHADOWS)
    color.xyz *= shadowTerm;
#endif

    float finalFogStart = skyInscatteringThreshold * viewingDistance;
    float fogValue = clamp((distance - finalFogStart) / (skyInscatteringLength * viewingDistance + finalFogStart), 0.0, skyInscatteringStrength);
    vec3 finalInscatteringColor = convertColorYxy(skyInscatteringColor, skyInscatteringExponent);
    color = mix(color, vec4(finalInscatteringColor, 1.0), fogValue);

#if defined (FEATURE_TRANSPARENT_PASS)
    gl_FragData[0].rgba = color;
#else
    gl_FragData[0].rgb = color.rgb;
    // Encode occlusion value into the alpha channel
    gl_FragData[0].a = occlusionValue;
#endif

    gl_FragData[1].rgb = vec3(normal.x / 2.0 + 0.5, normal.y / 2.0 + 0.5, normal.z / 2.0 + 0.5);

    // Primitive objects ids... Will be extended later on
#ifdef FEATURE_TRANSPARENT_PASS
    if (isOceanWater) {
        gl_FragData[1].a = 1.0f;
    }
    else
#endif
    {
        gl_FragData[1].a = 0.0f;
    }
}
