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
#define BLOCK_HINT_WAVING    4

#define DAYLIGHT_AMBIENT_COLOR 0.95, 0.92, 0.91
#define MOONLIGHT_AMBIENT_COLOR 0.8, 0.8, 1.0
#define NIGHT_BRIGHTNESS 0.05
#define WATER_COLOR_SWIMMING 0.8, 1.0, 1.0, 0.975
#define WATER_COLOR 0.8, 1.0, 1.0, 0.75
#define REFLECTION_COLOR 1.0, 1.0, 1.0, 1.0

#define TORCH_WATER_SPEC 8.0
#define TORCH_WATER_DIFF 0.7
#define TORCH_BLOCK_SPEC 0.7
#define TORCH_BLOCK_DIFF 1.0
#define WATER_SPEC 2.0
#define WATER_DIFF 1.0
#define BLOCK_DIFF 0.6
#define BLOCK_AMB 1.0

#define WATER_REFRACTION 0.1

uniform sampler2D textureAtlas;
uniform sampler2D textureWaterNormal;
uniform sampler2D textureLava;
uniform sampler2D textureEffects;
uniform sampler2D textureWaterReflection;

uniform float time;
uniform float daylight = 1.0;

uniform float clipHeight = 0.0;

uniform bool carryingTorch;
uniform bool swimming;

varying vec4 vertexWorldPosRaw;
varying vec4 vertexWorldPos;
varying vec4 vertexPos;
varying vec3 lightDir;
varying vec3 normal;
varying vec3 waterNormal;

varying float flickeringLightOffset;
varying int blockHint;
varying float isUpside;

void main(){
	if (clipHeight > 0.0 && vertexWorldPosRaw.y < clipHeight && !swimming) {
        discard;
	}

    vec2 texCoord = gl_TexCoord[0].xy;

    vec3 normalizedVPos = -normalize(vertexWorldPos.xyz);
    vec3 normalWater;
    bool isWater = false;

    vec3 finalLightDir = lightDir;

    /* DAYLIGHT BECOMES... MOONLIGHT! */
    /* Now featuring linear interpolation to make the transition smoother... :-) */
    if (daylight < 0.1)
        finalLightDir = mix(finalLightDir * -1.0, finalLightDir, daylight / 0.1);

    vec4 color = vec4(0.0, 0.0, 0.0, 1.0);

    /* WATER */
    if ( blockHint == BLOCK_HINT_WATER ) {
        vec2 waterOffset = vec2(vertexWorldPosRaw.x + timeToTick(time, 0.1), vertexWorldPosRaw.z + timeToTick(time, 0.1)) / 8.0;
        normalWater.xyz = texture2D(textureWaterNormal, waterOffset).xyz * 2.0 - 1.0;

       // Enable reflection only when not swimming and for blocks on sea level
        if (!swimming && isUpside > 0.99 && vertexWorldPosRaw.y < 32.5 && vertexWorldPosRaw.y > 31.5) {
            vec2 projectedPos = 0.5 * (vertexPos.st/vertexPos.q) + vec2(0.5);

            // Fresnel
            float refractionFactor = WATER_REFRACTION * clamp(1.0 - length(vertexWorldPos.xyz) / 50.0, 0.25, 1.0);
            vec4 reflectionColor = vec4(texture2D(textureWaterReflection, projectedPos + normalWater.xy * refractionFactor).xyz, 1.0);
            float f = fresnel(max(dot(normalizedVPos, waterNormal), 0.0), 0.1, 5.0);
            color = mix(reflectionColor * vec4(WATER_COLOR), reflectionColor * vec4(REFLECTION_COLOR), f);
        } else if (!swimming && (vertexWorldPosRaw.y >= 32.5 || vertexWorldPosRaw.y <= 31.5)) {
            color = vec4(WATER_COLOR);
        } else {
            color = vec4(WATER_COLOR_SWIMMING);
        }

        isWater = true;
    /* LAVA */
    } else if ( blockHint == BLOCK_HINT_LAVA ) {
        texCoord.x = mod(texCoord.x, TEXTURE_OFFSET) * (1.0 / TEXTURE_OFFSET);
        texCoord.y = mod(texCoord.y, TEXTURE_OFFSET) / (128.0 / (1.0 / TEXTURE_OFFSET));
        texCoord.y += mod(timeToTick(time, 0.1), 127.0) * (1.0/128.0);

        color = texture2D(textureLava, texCoord.xy);
    /* APPLY DEFAULT TEXTURE FROM ATLAS */
    } else {
        color = texture2D(textureAtlas, texCoord.xy);
    }

    if (color.a < 0.5) {
        discard;
    }

    /* APPLY OVERALL BIOME COLOR OFFSET */
    if ( blockHint != BLOCK_HINT_GRASS ) {
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
        diffuseLighting = calcLambLight(normalWater, normalizedVPos);
    } else {
        diffuseLighting = calcLambLight(normal, finalLightDir);
    }

    float torchlight = 0.0;

    /* CALCULATE TORCHLIGHT */
    if (carryingTorch) {
        if (isWater)
            torchlight = calcTorchlight(calcLambLight(normalWater, normalizedVPos) * TORCH_WATER_DIFF
            + TORCH_WATER_SPEC * calcSpecLightWithOffset(normal, normalizedVPos, normalizedVPos, 16.0, normalWater), vertexWorldPos.xyz);
        else
            torchlight = calcTorchlight(calcLambLight(normal, normalizedVPos) * TORCH_BLOCK_DIFF
            + TORCH_BLOCK_SPEC * calcSpecLight(normal, normalizedVPos, normalizedVPos, 32.0), vertexWorldPos.xyz);
    }

    vec3 daylightColorValue;

    /* CREATE THE DAYLIGHT LIGHTING MIX */
    if (isWater) {
        /* WATER NEEDS DIFFUSE AND SPECULAR LIGHT */
        daylightColorValue = vec3(diffuseLighting) * WATER_DIFF;
        daylightColorValue += calcSpecLightWithOffset(normal, finalLightDir, normalizedVPos, 64.0, normalWater) * WATER_SPEC;
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
    float blockBrightness = (expLightValue(blocklightValue) + diffuseLighting * blocklightValue * BLOCK_DIFF);

    torchlight -= flickeringLightOffset * torchlight;

    blockBrightness += (1.0 - blockBrightness) * torchlight;
    blockBrightness -= flickeringLightOffset * blocklightValue;
    blockBrightness *= blocklightDayIntensity;

    // Calculate the final blocklight color value and add a slight reddish tint to it
    vec3 blocklightColorValue = vec3(blockBrightness) * vec3(1.0, 0.95, 0.94);

    // Apply the final lighting mix
    color.xyz *= (daylightColorValue + blocklightColorValue) * occlusionValue;
    gl_FragColor = color;
}
