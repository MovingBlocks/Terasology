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

#define WATER_COLOR_SWIMMING 0.8, 1.0, 1.0, 0.975
#define WATER_TINT 0.1, 0.41, 0.627, 1.0

#define WATER_SPEC 1.0

#ifdef FEATURE_REFRACTIVE_PASS
varying vec3 waterNormalViewSpace;
#endif

#ifdef FEATURE_REFRACTIVE_PASS
uniform vec4 waterSettingsFrag;
#define waterNormalBias waterSettingsFrag.x
#define waterRefraction waterSettingsFrag.y
#define waterFresnelBias waterSettingsFrag.z
#define waterFresnelPow waterSettingsFrag.w

uniform vec4 alternativeWaterSettingsFrag;
#define waterTint alternativeWaterSettingsFrag.x
uniform vec4 lightingSettingsFrag;
#define waterSpecExp lightingSettingsFrag.z

uniform sampler2D textureWaterRefraction;
uniform sampler2D textureWaterReflection;
uniform sampler2D texSceneOpaque;
uniform sampler2D textureWaterNormal;
uniform sampler2D textureWaterNormalAlt;
#endif

#if defined (NORMAL_MAPPING)
varying vec3 worldSpaceNormal;
varying mat3 normalMatrix;

uniform sampler2D textureAtlasNormal;

#if defined (PARALLAX_MAPPING)
uniform vec4 parallaxProperties;
#define parallaxBias parallaxProperties.x
#define parallaxScale parallaxProperties.y

uniform sampler2D textureAtlasHeight;
#endif
#endif


#if defined (FLICKERING_LIGHT)
varying float flickeringLightOffset;
#endif

varying vec3 vertexWorldPos;
varying vec4 vertexViewPos;
varying vec4 vertexProjPos;
varying vec3 sunVecView;

varying vec3 normal;

varying float blockHint;
varying float isUpside;

uniform sampler2D textureWater;
uniform sampler2D textureLava;

uniform sampler2D textureAtlas;
uniform sampler2D textureEffects;

uniform float clip;

void main() {

// Only necessary for opaque objects
#if !defined (FEATURE_REFRACTIVE_PASS)
	if (clip > 0.001 && vertexWorldPos.y < clip) {
        discard;
	}
#endif

    vec2 texCoord = gl_TexCoord[0].xy;

    vec3 normalizedVPos = -normalize(vertexViewPos.xyz);
    vec2 projectedPos = projectVertexToTexCoord(vertexProjPos);
    vec3 normalOpaque = normal;

#if defined (NORMAL_MAPPING)
#if defined (PARALLAX_MAPPING)
    // TODO: Calculates the tangent frame on the fly - this is absurdly costly... But storing
    // the tangent for each vertex in the chunk VBO might be not the best idea either.
    vec3 dp1 = dFdx(vertexProjPos.xyz);
    vec3 dp2 = dFdy(vertexProjPos.xyz);
    vec2 duv1 = dFdx(gl_TexCoord[0].xy);
    vec2 duv2 = dFdy(gl_TexCoord[0].xy);

    vec3 dp2perp = cross(dp2, normal);
    vec3 dp1perp = cross(normal, dp1);
    vec3 tangent = dp2perp * duv1.x + dp1perp * duv2.x;
    vec3 binormal = dp2perp * duv1.y + dp1perp * duv2.y;

    float invMax = inversesqrt(max(dot(tangent,tangent), dot(binormal,binormal)));
    mat3 tbn = mat3(tangent * invMax, binormal * invMax, normal);

    vec3 eyeTangentSpace = tbn * vertexViewPos.xyz;

    float height =  parallaxScale * texture2D(textureAtlasHeight, texCoord).r - parallaxBias;
	texCoord += height * normalize(eyeTangentSpace).xy * TEXTURE_OFFSET;
#endif

    normalOpaque = (texture2D(textureAtlasNormal, texCoord).xyz * 2.0 - 1.0);

    // Simplified tangent basis - because we can! Voxels and blocks are great
    normalOpaque.xyz = vec3(worldSpaceNormal.x, normalOpaque.x, normalOpaque.y) * abs(worldSpaceNormal.xxx)
        + vec3(normalOpaque.y, worldSpaceNormal.y, normalOpaque.x) * abs(worldSpaceNormal.yyy)
        + vec3(normalOpaque.x, normalOpaque.y, worldSpaceNormal.z) * abs(worldSpaceNormal.zzz);

    // Costly, but necessary...
    normalOpaque = normalMatrix * normalOpaque.xyz;
#endif

#ifdef FEATURE_REFRACTIVE_PASS
    vec2 normalWaterOffset;
    vec3 normalWater = waterNormalViewSpace;
    bool isWater = false;
    bool isOceanWater = false;

    if (checkFlag(BLOCK_HINT_WATER_SURFACE, blockHint) && isUpside > 0.99) {
        vec2 scaledVertexWorldPos = vertexWorldPos.xz / 32.0;

        vec2 waterOffset = vec2(scaledVertexWorldPos.x + timeToTick(time, 0.0075), scaledVertexWorldPos.y + timeToTick(time, 0.0075));
        vec2 waterOffset2 = vec2(scaledVertexWorldPos.x + timeToTick(time, 0.005), scaledVertexWorldPos.y - timeToTick(time, 0.005));

        normalWaterOffset = (texture2D(textureWaterNormal, waterOffset).xyz * 2.0 - 1.0).xy;
        normalWaterOffset += (texture2D(textureWaterNormalAlt, waterOffset2).xyz * 2.0 - 1.0).xy;
        normalWaterOffset *= 0.5 * (1.0 / vertexViewPos.z * waterNormalBias);

        normalWater.xy += normalWaterOffset;
        normalWater = normalize(normalWater);

        isOceanWater = true;
        isWater = true;
    }
#endif

    vec3 sunVecViewAdjusted = sunVecView;

    /* DAYLIGHT BECOMES... MOONLIGHT! */
    // Now featuring linear interpolation to make the transition smoother... :-)
    if (daylight < 0.1) {
        sunVecViewAdjusted = mix(sunVecViewAdjusted * -1.0, sunVecViewAdjusted, daylight / 0.1);
     }

    vec4 color = vec4(0.0, 0.0, 0.0, 1.0);

#if !defined (FEATURE_REFRACTIVE_PASS)
    if (checkFlag(BLOCK_HINT_LAVA, blockHint)) {
        texCoord.x = mod(texCoord.x, TEXTURE_OFFSET) * (1.0 / TEXTURE_OFFSET);
        texCoord.y = mod(texCoord.y, TEXTURE_OFFSET) / (128.0 / (1.0 / TEXTURE_OFFSET));
        texCoord.y += mod(timeToTick(time, -0.1), 127.0) * (1.0/128.0);

        color = texture2D(textureLava, texCoord.xy);
    /* APPLY DEFAULT TEXTURE FROM ATLAS */
    } else {
        color = texture2D(textureAtlas, texCoord.xy);

#if defined FEATURE_ALPHA_REJECT
        if (color.a < 0.1) {
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
    // Calculate blocklight lighting value
    float blocklightValue = gl_TexCoord[1].y;
    // ...and finally the occlusion value
    float occlusionValue = expOccValue(gl_TexCoord[1].z);

    vec3 blocklightColorValue = calcBlocklightColor(blocklightValue
#if defined (FLICKERING_LIGHT)
        , flickeringLightOffset
#endif
    );

#if defined (FEATURE_REFRACTIVE_PASS)
    vec3 daylightColorValue;

    if (isOceanWater) {
        daylightColorValue = calcSunlightColorWater(daylightValue, normalWater, sunVecViewAdjusted);
    } else {
        daylightColorValue = calcSunlightColorOpaque(daylightValue, normal, sunVecViewAdjusted);
    }

    vec3 combinedLightValue = max(daylightColorValue, blocklightColorValue);
#elif defined (FEATURE_USE_FORWARD_LIGHTING)
    vec3 daylightColorValue = calcSunlightColorOpaque(daylightValue, normal, sunVecViewAdjusted);
    vec3 combinedLightValue = max(daylightColorValue, blocklightColorValue);
#endif

#if defined (FEATURE_REFRACTIVE_PASS)
    // Apply the final lighting mix
    color.xyz *= combinedLightValue * occlusionValue;

    // Apply reflection and refraction AFTER the lighting has been applied (otherwise bright areas below water become dark)
    // The water tint has still to be adjusted adjusted though...
     if (isWater && isOceanWater) {
            float specularHighlight = WATER_SPEC * calcDayAndNightLightingFactor(daylightValue, daylight) * calcSpecLightNormalized(normalWater, sunVecViewAdjusted, normalizedVPos, waterSpecExp);
            color.xyz += vec3(specularHighlight, specularHighlight, specularHighlight);

            vec4 reflectionColor = vec4(texture2D(textureWaterReflection, projectedPos + normalWaterOffset.xy * waterRefraction).xyz, 1.0);
            vec4 refractionColor = vec4(texture2D(texSceneOpaque, projectedPos + normalWaterOffset.xy * waterRefraction).xyz, 1.0);

            vec4 litWaterTint = vec4(WATER_TINT) * vec4(combinedLightValue.x, combinedLightValue.y, combinedLightValue.z, 1.0);

            /* FRESNEL */
            if (!swimming) {
                float f = fresnel(dot(normalWater, normalizedVPos), waterFresnelBias, waterFresnelPow);
                color += mix(refractionColor * (1.0 - waterTint) +  waterTint * litWaterTint,
                    reflectionColor * (1.0 - waterTint) + waterTint * litWaterTint, f);
            } else {
                color += refractionColor * (1.0 - waterTint) +  waterTint * litWaterTint;
            }

            color.a = 1.0;
     } else if (isWater) {
            texCoord.x = mod(texCoord.x, TEXTURE_OFFSET) * (1.0 / TEXTURE_OFFSET);
            texCoord.y = mod(texCoord.y, TEXTURE_OFFSET) / (128.0 / (1.0 / TEXTURE_OFFSET));
            texCoord.y += mod(timeToTick(time, -0.1), 127.0) * (1.0/128.0);

            vec4 albedoColor = texture2D(textureWater, texCoord.xy).rgba;
            albedoColor.rgb *= combinedLightValue;

            vec3 refractionColor = texture2D(texSceneOpaque, projectedPos + albedoColor.rg * 0.05).rgb;

            color.rgb += mix(refractionColor, albedoColor.rgb, albedoColor.a);
            color.a = 1.0;
    } else {
        vec3 refractionColor = texture2D(texSceneOpaque, projectedPos).rgb;
        vec4 albedoColor = texture2D(textureAtlas, texCoord.xy);
        albedoColor.rgb *= combinedLightValue;

        // TODO: Add support for actual refraction here
        color.rgb += mix(refractionColor, albedoColor.rgb, albedoColor.a);
        color.a = 1.0;
    }
#elif defined (FEATURE_USE_FORWARD_LIGHTING)
    // Apply the final lighting mix
    color.xyz *= combinedLightValue * occlusionValue;
#else
    gl_FragData[2].rgba = vec4(blocklightColorValue.r, blocklightColorValue.g, blocklightColorValue.b, 0.0);
#endif

#if defined (FEATURE_REFRACTIVE_PASS) || defined (FEATURE_USE_FORWARD_LIGHTING)
    gl_FragData[0].rgba = color.rgba;
#if defined (FEATURE_REFRACTIVE_PASS)
    // Encode "reflection" intensity into normal alpha
    gl_FragData[1].a = 1.0;
#endif
#else
    gl_FragData[0].rgb = color.rgb;
    // Encode occlusion value into the alpha channel
    gl_FragData[0].a = occlusionValue;
    // Encode daylight value into the normal alpha channel
    gl_FragData[1].a = daylightValue;
#endif

#if !defined (FEATURE_REFRACTIVE_PASS) && !defined (FEATURE_USE_FORWARD_LIGHTING)
    gl_FragData[1].rgb = vec3(normalOpaque.x / 2.0 + 0.5, normalOpaque.y / 2.0 + 0.5, normalOpaque.z / 2.0 + 0.5);
#elif defined (FEATURE_REFRACTIVE_PASS)
    gl_FragData[1].rgb = vec3(normalWater.x / 2.0 + 0.5, normalWater.y / 2.0 + 0.5, normalWater.z / 2.0 + 0.5);
#endif
}
