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
#define NIGHT_BRIGHTNESS 0.1
#define WATER_COLOR_SWIMMING 0.8, 1.0, 1.0, 0.975
#define WATER_TINT 0.1, 0.41, 0.627, 1.0

#define TORCH_WATER_SPEC 8.0
#define TORCH_WATER_DIFF 0.7
#define TORCH_BLOCK_SPEC 0.7
#define TORCH_BLOCK_DIFF 1.0

#define WATER_AMB 0.25
#define WATER_SPEC 4.0
#define WATER_DIFF 0.75

#define BLOCK_DIFF 0.75
#define BLOCK_AMB 1.0

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
#define torchSpecExp lightingSettingsFrag.x
#define torchWaterSpecExp lightingSettingsFrag.y
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
    vec3 normalWater = waterNormalViewSpace;
    bool isOceanWater = false;
    bool isWater = false;
#endif

    vec3 sunVecViewAdjusted = sunVecView;

    /* DAYLIGHT BECOMES... MOONLIGHT! */
    // Now featuring linear interpolation to make the transition smoother... :-)
    if (daylight < 0.1)
        sunVecViewAdjusted = mix(sunVecViewAdjusted * -1.0, sunVecViewAdjusted, daylight / 0.1);

    vec4 color = vec4(0.0, 0.0, 0.0, 1.0);

#ifdef FEATURE_TRANSPARENT_PASS
    if ( checkFlag(BLOCK_HINT_WATER, blockHint) ) {
        if (!swimming) {
            if ( vertexWorldPos.y < 32.5 && vertexWorldPos.y > 31.5 && isUpside > 0.99) {
                vec2 waterOffset = vec2(vertexWorldPos.x + timeToTick(time, 0.1), vertexWorldPos.z + timeToTick(time, 0.1)) / 8.0;
                vec2 waterOffset2 = vec2(vertexWorldPos.x + timeToTick(time, 0.1), vertexWorldPos.z - timeToTick(time, 0.1)) / 16.0;

                vec2 normalOffset = (texture2D(textureWaterNormal, waterOffset).xyz * 2.0 - 1.0).xy;
                normalOffset += (texture2D(textureWaterNormal, waterOffset2).xyz * 2.0 - 1.0).xy;
                normalOffset *= 0.5 * (1.0 / vertexViewPos.z * waterNormalBias);

                normalWater.xy += normalOffset;
                normalWater = normalize(normalWater);

                vec2 projectedPos = 0.5 * (vertexProjPos.xy/vertexProjPos.w) + vec2(0.5);

                vec4 reflectionColor = vec4(texture2D(textureWaterReflection, projectedPos + normalOffset.xy * waterRefraction).xyz, 1.0);
                vec4 refractionColor = vec4(texture2D(texSceneOpaque, projectedPos + normalOffset.xy * waterRefraction).xyz, 1.0);

                /* FRESNEL */
                float f = fresnel(dot(normalWater, normalizedVPos), waterFresnelBias, waterFresnelPow);
                color = mix(refractionColor * (1.0 - waterTint) +  waterTint * vec4(WATER_TINT), reflectionColor * (1.0 - waterTint) + waterTint * vec4(WATER_TINT), f);

                isOceanWater = true;
                isWater = true;
            } else {
                texCoord.x = mod(texCoord.x, TEXTURE_OFFSET) * (1.0 / TEXTURE_OFFSET);
                texCoord.y = mod(texCoord.y, TEXTURE_OFFSET) / (128.0 / (1.0 / TEXTURE_OFFSET));
                texCoord.y += mod(timeToTick(time, 0.1), 127.0) * (1.0/128.0);

                color = vec4(texture2D(textureWater, texCoord.xy).xyz, 1.0);
            }
        } else {
            color = vec4(texture2D(textureAtlas, texCoord.xy).xyz, 1.0) * vec4(WATER_COLOR_SWIMMING);
        }
    /* LAVA */
    } else
#endif
    if ( checkFlag(BLOCK_HINT_LAVA, blockHint) ) {
        texCoord.x = mod(texCoord.x, TEXTURE_OFFSET) * (1.0 / TEXTURE_OFFSET);
        texCoord.y = mod(texCoord.y, TEXTURE_OFFSET) / (128.0 / (1.0 / TEXTURE_OFFSET));
        texCoord.y += mod(timeToTick(time, 0.1), 127.0) * (1.0/128.0);

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
        vec4 maskColor = texture2D(textureEffects, vec2(10.0 * TEXTURE_OFFSET + mod(texCoord.x,TEXTURE_OFFSET), mod(texCoord.y,TEXTURE_OFFSET)));

        // Only use one channel so the color won't be altered
        if (maskColor.a != 0.0) color.rgb = vec3(color.g) * gl_Color.rgb;
    }

    // Calculate daylight lighting value
    float daylightValue = gl_TexCoord[1].x;
    float daylightScaledValue = daylight * daylightValue;

    // Calculate blocklight lighting value
    float blocklightDayIntensity = 1.0 - daylightScaledValue;
    float blocklightValue = gl_TexCoord[1].y;

    float occlusionValue = expOccValue(gl_TexCoord[1].z);
    float diffuseLighting;

#ifdef FEATURE_TRANSPARENT_PASS
    if (isWater) {
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

    float torchlight = 0.0;

    /* CALCULATE TORCHLIGHT */
    if (carryingTorch > 0.99) {
#ifdef FEATURE_TRANSPARENT_PASS
        if (isWater)
            torchlight = calcTorchlight(calcLambLight(normalWater, normalizedVPos) * TORCH_WATER_DIFF
            + TORCH_WATER_SPEC * calcSpecLight(normal, normalizedVPos, normalizedVPos, torchWaterSpecExp), vertexViewPos.xyz);
        else
#endif
            torchlight = calcTorchlight(calcLambLight(normal, normalizedVPos) * TORCH_BLOCK_DIFF
            + TORCH_BLOCK_SPEC * calcSpecLight(normal, normalizedVPos, normalizedVPos, torchSpecExp), vertexViewPos.xyz);
    }

    vec3 daylightColorValue;

    /* CREATE THE DAYLIGHT LIGHTING MIX */
#ifdef FEATURE_TRANSPARENT_PASS
    if (isWater) {
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

    torchlight -= flickeringLightOffset * torchlight;

    blockBrightness += (1.0 - blockBrightness) * torchlight;
    blockBrightness -= flickeringLightOffset * blocklightValue;

    // Calculate the final blocklight color value and add a slight reddish tint to it
    vec3 blocklightColorValue = vec3(blockBrightness) * vec3(1.0, 0.95, 0.94);

    // Apply the final lighting mix
    color.xyz *= max(daylightColorValue, blocklightColorValue) * occlusionValue;

#if defined (DYNAMIC_SHADOWS)
    color.xyz *= shadowTerm;
#endif

    float finalFogStart = skyInscatteringThreshold * viewingDistance;
    float fogValue = clamp((distance - finalFogStart) / (skyInscatteringLength * viewingDistance + finalFogStart), 0.0, skyInscatteringStrength);
    vec3 finalInscatteringColor = convertColorYxy(skyInscatteringColor, skyInscatteringExponent);
    color = mix(color, vec4(finalInscatteringColor, 1.0), fogValue);

    gl_FragData[0].rgba = color;
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
