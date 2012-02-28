/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

uniform sampler2D textureAtlas;
uniform sampler2D textureWaterNormal;
uniform sampler2D textureLava;
uniform sampler2D textureEffects;

uniform float time;
uniform float daylight = 1.0;
uniform bool swimming;
uniform bool carryingTorch;

varying vec4 vertexWorldPosRaw;
varying vec4 vertexWorldPos;
varying vec3 eyeVec;
varying vec3 lightDir;
varying vec3 normal;

uniform vec3 chunkOffset;
uniform vec2 waterCoordinate;
uniform vec2 lavaCoordinate;
uniform vec2 grassCoordinate;

void main(){
    vec4 texCoord = gl_TexCoord[0];

    vec3 normalizedVPos = -normalize(vertexWorldPos.xyz);
    vec3 normalWater;
    bool isWater = false;

    vec3 finalLightDir = lightDir;

    if (daylight <= 0.0)
        finalLightDir *= -1.0;

    vec4 color;

    /* APPLY WATER TEXTURE */
    if (texCoord.x >= waterCoordinate.x && texCoord.x < waterCoordinate.x + TEXTURE_OFFSET && texCoord.y >= waterCoordinate.y && texCoord.y < waterCoordinate.y + TEXTURE_OFFSET) {
        vec2 waterOffset = vec2(vertexWorldPosRaw.x + timeToTick(time, 0.1), vertexWorldPosRaw.z) / 16.0;
        normalWater = (texture2D(textureWaterNormal, waterOffset) * 2.0 - 1.0).xyz;

        color = vec4(83.0 / 255.0, 209.0 / 255.0, 236.0 / 255.0, 0.5);
        isWater = true;
    /* APPLY LAVA TEXTURE */
    } else if (texCoord.x >= lavaCoordinate.x && texCoord.x < lavaCoordinate.x + TEXTURE_OFFSET && texCoord.y >= lavaCoordinate.y && texCoord.y < lavaCoordinate.y + TEXTURE_OFFSET) {
        texCoord.x = mod(texCoord.x, TEXTURE_OFFSET) * (1.0 / TEXTURE_OFFSET);
        texCoord.y = mod(texCoord.y, TEXTURE_OFFSET) / (128.0 / (1.0 / TEXTURE_OFFSET));
        texCoord.y += mod(timeToTick(time, 0.1),127.0) * (1.0/128.0);

        color = texture2D(textureLava, texCoord.xy);
    /* APPLY DEFAULT TEXTURE FROM ATLAS */
    } else {
        color = texture2D(textureAtlas, texCoord.xy);
    }

    /* CONVERT SRGB TO LINEAR COLOR SPACE */
    color = srgbToLinear(color);

    if (color.a < 0.1)
        discard;

    /* APPLY OVERALL BIOME COLOR OFFSET */
    if (!(texCoord.x >= grassCoordinate.x && texCoord.x < grassCoordinate.x + TEXTURE_OFFSET && texCoord.y >= grassCoordinate.y && texCoord.y < grassCoordinate.y + TEXTURE_OFFSET)) {
        color.rgb *= gl_Color.rgb;
        color.a *= gl_Color.a;
    /* MASK GRASS AND APPLY BIOME COLOR */
    } else {
        vec4 maskColor = texture2D(textureEffects, vec2(10.0 * TEXTURE_OFFSET + mod(texCoord.x,TEXTURE_OFFSET), mod(texCoord.y,TEXTURE_OFFSET)));

        if (maskColor.a != 0.0) color.rgb *= gl_Color.rgb;
    }

    // Calculate daylight lighting value
    float daylightValue = expLightValue(gl_TexCoord[1].x);
    float daylightScaledValue = daylight * daylightValue;

    // Calculate blocklight lighting value
    float blocklightDayIntensity = 1.0 - daylightScaledValue;
    float blocklightValue = expLightValue(gl_TexCoord[1].y);

    float occlusionValue = gl_TexCoord[1].z;

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
            torchlight = calcTorchlight(calcLambLight(normalWater, normalizedVPos) * 0.1
            + 0.9 * calcSpecLightWithOffset(normal, normalizedVPos, normalize(eyeVec), 64.0, normalWater), vertexWorldPos.xyz);
        else
            torchlight = calcTorchlight(calcLambLight(normal, normalizedVPos) * 0.9
            + 0.1 * calcSpecLight(normal, normalizedVPos, normalize(eyeVec), 32.0), vertexWorldPos.xyz);
    }

    vec3 daylightColorValue;

    /* CREATE THE DAYLIGHT LIGHTING MIX */
    if (isWater) {
        /* WATER NEEDS DIFFUSE AND SPECULAR LIGHT */
        daylightColorValue = vec3(diffuseLighting);
        daylightColorValue += calcSpecLightWithOffset(normal, finalLightDir, normalize(eyeVec), 64.0, normalWater);
    } else {
        /* DEFAULT LIGHTING ONLY CONSIST OF DIFFUSE AND AMBIENT LIGHT */
        daylightColorValue = vec3(1.0 + diffuseLighting * 0.25);
    }

    /* SUNLIGHT BECOMES MOONLIGHT */
    vec3 ambientTint = mix(vec3(0.8, 0.8, 1.0), vec3(1.0, 0.99, 0.99), daylight);
    daylightColorValue.xyz *= ambientTint;

    // Scale the lighting according to the daylight and daylight block values and add moonlight during the nights
    daylightColorValue.xyz *= daylightScaledValue + (0.25 * (1.0 - daylight) * daylightValue);

    // Calculate the final block light brightness
    float blockBrightness = (blocklightValue * 0.8 + diffuseLighting * blocklightValue * 0.2);
    blockBrightness += (1.0 - blockBrightness) * torchlight - (sin(timeToTick(time, 0.5) + 1.0) / 16.0) * blocklightValue;
    blockBrightness *= blocklightDayIntensity * 0.75;

    // Calculate the final blocklight color value and add a slight reddish tint to it
    vec3 blocklightColorValue = vec3(blockBrightness) * vec3(1.0, 0.95, 0.94);

    // Apply the final lighting mix
    color.xyz *= (daylightColorValue + blocklightColorValue) * occlusionValue;
    gl_FragColor = linearToSrgb(color);
}
