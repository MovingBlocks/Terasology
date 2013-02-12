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

#define BLOCK_HINT_WATER     1
#define BLOCK_HINT_LAVA      2
#define BLOCK_HINT_GRASS     3

#define DAYLIGHT_AMBIENT_COLOR 0.95, 0.92, 0.91
#define MOONLIGHT_AMBIENT_COLOR 0.8, 0.8, 1.0
#define NIGHT_BRIGHTNESS 0.05
#define WATER_COLOR_SWIMMING 0.8, 1.0, 1.0, 0.975
#define WATER_COLOR 0.75, 0.8, 1.0, 1.0

#define TORCH_WATER_SPEC 8.0
#define TORCH_WATER_DIFF 0.7
#define TORCH_BLOCK_SPEC 0.7
#define TORCH_BLOCK_DIFF 1.0
#define WATER_AMB 0.1
#define WATER_SPEC 2.0
#define WATER_DIFF 1.0
#define BLOCK_DIFF 0.75
#define BLOCK_AMB 1.0

varying vec4 vertexWorldPos;
varying vec4 vertexViewPos;
varying vec4 vertexProjPos;
varying vec3 normal;
varying vec3 waterNormalWorldSpace;

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

uniform vec4 waterSettingsFrag;
#define waterNormalBias waterSettingsFrag.x
#define waterRefraction waterSettingsFrag.y
#ifdef REFRACTIVE_WATER
#define waterFresnelBias waterSettingsFrag.z
#define waterFresnelPow waterSettingsFrag.w

uniform sampler2D textureWaterRefraction;
#endif

uniform sampler2D textureAtlas;
uniform sampler2D textureWaterNormal;
uniform sampler2D textureLava;
uniform sampler2D textureEffects;
uniform sampler2D textureWaterReflection;

uniform float clipHeightPos = 0.0;
uniform float clipHeightNeg = 0.0;

void main(){
	if (clipHeightPos > 0.001 && vertexWorldPos.y < clipHeightPos) {
        discard;
	}

    if (clipHeightNeg > 0.001 && vertexWorldPos.y > clipHeightNeg) {
        discard;
    }

    vec2 texCoord = gl_TexCoord[0].xy;

    vec3 normalizedVPos = -normalize(vertexViewPos.xyz);
    vec3 normalWater = waterNormalWorldSpace;
    bool isWater = false;

    vec3 sunVecViewAdjusted = sunVecView;

    /* DAYLIGHT BECOMES... MOONLIGHT! */
    // Now featuring linear interpolation to make the transition smoother... :-)
    if (daylight < 0.1)
        sunVecViewAdjusted = mix(sunVecViewAdjusted * -1.0, sunVecViewAdjusted, daylight / 0.1);

    vec4 color = vec4(0.0, 0.0, 0.0, 1.0);

    /* WATER */
    if ( checkFlag(BLOCK_HINT_WATER, blockHint) ) {
        vec2 waterOffset = vec2(vertexWorldPos.x + timeToTick(time, 0.05), vertexWorldPos.z + timeToTick(time, 0.05)) / 16.0;
        vec2 waterOffset2 = vec2(vertexWorldPos.x - timeToTick(time, 0.05), vertexWorldPos.z + timeToTick(time, 0.05)) / 16.0;
        normalWater.xy += (texture2D(textureWaterNormal, waterOffset).xyz * 2.0 - 1.0).xy * waterNormalBias;
        normalWater.xy += (texture2D(textureWaterNormal, waterOffset2).xyz * 2.0 - 1.0).xy * waterNormalBias;
        normalWater = normalize(normalWater);

       // Enable reflection only when not swimming and for blocks on sea level
        if (!swimming && isUpside > 0.99) {
            if ( vertexWorldPos.y < 32.5 && vertexWorldPos.y > 31.5) {
                vec2 projectedPos = 0.5 * (vertexProjPos.st/vertexProjPos.q) + vec2(0.5);

                float refractionFactor = waterRefraction * clamp(1.0 - length(vertexViewPos.xyz) / 50.0, 0.25, 1.0);
                vec4 reflectionColor = vec4(texture2D(textureWaterReflection, projectedPos + normalWater.xy * refractionFactor).xyz, 1.0);

#ifdef REFRACTIVE_WATER
                /* FRESNEL */
                vec4 refractionColor = vec4(texture2D(textureWaterRefraction, projectedPos + normalWater.xy * refractionFactor).xyz, 1.0);
                float f = fresnel(dot(waterNormalWorldSpace, normalizedVPos), waterFresnelBias, waterFresnelPow);
                color = mix(refractionColor * vec4(WATER_COLOR), reflectionColor * vec4(WATER_COLOR), f);
#else
                color = reflectionColor  * vec4(WATER_COLOR);
#endif
            } else {
                color = vec4(WATER_COLOR);
            }
        } else {
            color = vec4(WATER_COLOR_SWIMMING);
        }

        isWater = true;
    /* LAVA */
    } else if ( checkFlag(BLOCK_HINT_LAVA, blockHint) ) {
        texCoord.x = mod(texCoord.x, TEXTURE_OFFSET) * (1.0 / TEXTURE_OFFSET);
        texCoord.y = mod(texCoord.y, TEXTURE_OFFSET) / (128.0 / (1.0 / TEXTURE_OFFSET));
        texCoord.y += mod(timeToTick(time, 0.1), 127.0) * (1.0/128.0);

        color = texture2D(textureLava, texCoord.xy);
    /* APPLY DEFAULT TEXTURE FROM ATLAS */
    } else {
        color = texture2D(textureAtlas, texCoord.xy);

        if (color.a < 0.1) {
            discard;
        }
    }

    /* APPLY OVERALL BIOME COLOR OFFSET */
    if ( !checkFlag(BLOCK_HINT_GRASS, blockHint) ) {
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

    if (isWater) {
        diffuseLighting = calcLambLight(waterNormalWorldSpace, sunVecViewAdjusted);
    } else {
        diffuseLighting = calcLambLight(normal, sunVecViewAdjusted);
    }

    float torchlight = 0.0;

    /* CALCULATE TORCHLIGHT */
    if (carryingTorch) {
        if (isWater)
            torchlight = calcTorchlight(calcLambLight(normalWater, normalizedVPos) * TORCH_WATER_DIFF
            + TORCH_WATER_SPEC * calcSpecLight(normal, normalizedVPos, normalizedVPos, torchWaterSpecExp), vertexViewPos.xyz);
        else
            torchlight = calcTorchlight(calcLambLight(normal, normalizedVPos) * TORCH_BLOCK_DIFF
            + TORCH_BLOCK_SPEC * calcSpecLight(normal, normalizedVPos, normalizedVPos, torchSpecExp), vertexViewPos.xyz);
    }

    vec3 daylightColorValue;

    /* CREATE THE DAYLIGHT LIGHTING MIX */
    if (isWater) {
        /* WATER NEEDS DIFFUSE AND SPECULAR LIGHT */
        daylightColorValue = vec3(diffuseLighting) * WATER_DIFF;
        daylightColorValue += calcSpecLight(normalWater, sunVecViewAdjusted, normalizedVPos, waterSpecExp) * WATER_SPEC;
    } else {
        /* DEFAULT LIGHTING ONLY CONSIST OF DIFFUSE AND AMBIENT LIGHT */
        daylightColorValue = vec3(BLOCK_AMB + diffuseLighting * BLOCK_DIFF);
    }

    /* SUNLIGHT BECOMES MOONLIGHT */
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

    if (isWater) {
        color.xyz += vec4(WATER_COLOR) * WATER_AMB;
    }

    // Apply the final lighting mix
    color.xyz *= max(daylightColorValue, blocklightColorValue) * occlusionValue;

    vec3 finalInscatteringColor = convertColorYxy(skyInscatteringColor, skyInscatteringExponent);
    float fogValue = skyInscatteringStrength - clamp((viewingDistance - distance) / (viewingDistance - (1.0 - skyInscatteringLength) * viewingDistance), 0.0, skyInscatteringStrength);
    color = mix(color, vec4(finalInscatteringColor, 1.0), fogValue);

    gl_FragData[0].rgba = color;
    gl_FragData[1].rgba = vec4(normal.x / 2.0 + 0.5, normal.y / 2.0 + 0.5, normal.z / 2.0 + 0.5, 0.0f);
}
