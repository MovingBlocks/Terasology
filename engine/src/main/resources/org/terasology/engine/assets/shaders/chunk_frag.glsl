#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

#define WATER_COLOR_SWIMMING 0.8, 1.0, 1.0, 0.975
#define WATER_TINT 0.1, 0.41, 0.627, 1.0

#define WATER_SPEC 1.0

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

uniform sampler2D textureWater;
uniform sampler2D textureWaterReflection;
uniform sampler2D texSceneOpaque;
uniform sampler2D textureWaterNormal;
uniform sampler2D textureWaterNormalAlt;

in vec3 waterNormalViewSpace;
#endif

#if defined (NORMAL_MAPPING)
in vec3 worldSpaceNormal;
uniform sampler2D textureAtlasNormal;
#endif

#if defined (PARALLAX_MAPPING)
uniform vec4 parallaxProperties;
#define parallaxBias parallaxProperties.x
#define parallaxScale parallaxProperties.y

uniform sampler2D textureAtlasHeight;
#endif


#if defined (FLICKERING_LIGHT)
in float flickeringLightOffset;
#endif

in vec3 vertexWorldPos;
in vec4 vertexViewPos;
in vec4 vertexProjPos;
in vec3 sunVecView;

in vec3 normal;

in vec2 v_uv0;
flat in int v_blockHint;
flat in int isUpside;

uniform sampler2D textureAtlas;
uniform sampler2D textureEffects;

uniform float clip;

in float v_sunlight;
in float v_blocklight;
in float v_ambientLight;

//inverse is not available in GLSL 1.20, so calculate it manually
mat2 inverse2(mat2 m) {
    float det = m[0][0] * m[1][1] - m[1][0] * m[0][1];
    return mat2(m[1][1], -m[0][1], -m[1][0], m[0][0]) / det;
}

void main() {

// Active for worldReflectionNode only.
#if defined FEATURE_USE_FORWARD_LIGHTING
    if (vertexWorldPos.y < clip) {
        discard;
    }
#endif

    vec2 texCoord = v_uv0.xy;

    vec3 normalizedViewPos = -normalize(vertexViewPos.xyz);
    vec2 projectedPos = projectVertexToTexCoord(vertexProjPos);
    vec3 normalOpaque = normal;
    float shininess = 0.0;

#if defined (NORMAL_MAPPING) || defined (PARALLAX_MAPPING)
    // TODO: Calculates the tangent frame on the fly - this is absurdly costly... But storing
    // the tangent for each vertex in the chunk VBO might be not the best idea either.
    // The only reason dFdx and dFdy are used here is that it happens to be possible to relate
    // both view and UV coordinates to screen-space coordinated. The specific relationship between
    // screen coordinates and view coordinates is irrelevant.
    mat2x3 screenToView = mat2x3(dFdx(vertexViewPos.xyz), dFdy(vertexViewPos.xyz));
    mat2   screenToUv   = mat2  (dFdx(v_uv0.xy), dFdy(v_uv0.xy)) / TEXTURE_OFFSET;
    mat2 uvToScreen = inverse2(screenToUv);
    mat2x3 uvToView = screenToView * uvToScreen;

    #if defined (PARALLAX_MAPPING)
        vec2 viewDirectionUvProjection = -normalizedViewPos * uvToView;

        float height = parallaxScale * texture2D(textureAtlasHeight, texCoord).r - parallaxBias;
        // Ideally this should be divided by dot(normal, normalizedViewPos), as the offset for texCoord
        // is the component parallel to the surface of a vector along the view's forward axis,
        // the other component being a vector perpendicular to the surface and having magnitude "height".
        // In practice the current way looks better at low angles, as the height-map can't make the triangle
        // protrude beyond its boundaries like displacement mapping would.
        texCoord += height * viewDirectionUvProjection * TEXTURE_OFFSET;

        //Crudely prevent the parallax from extending to other textures in the same atlas.
        vec2 texCorner = floor(v_uv0.xy/TEXTURE_OFFSET)*TEXTURE_OFFSET;
        vec2 texSize = vec2(1,1)*TEXTURE_OFFSET*0.9999; //Remain strictly this side of the edge of the texture.
        texCoord = clamp(texCoord, texCorner, texCorner + texSize);
    #endif
    #if defined (NORMAL_MAPPING)
        // Normalised but not orthonormalised. It should be orthogonal anyway (except for some non-rectangular
        // block shapes like torches), but it's not obvious what's the best thing to do when it isn't.
        mat3 uvnSpaceToViewSpace = mat3(normalize(uvToView[0]), normalize(uvToView[1]), normal);
        normalOpaque = normalize(texture2D(textureAtlasNormal, texCoord).xyz * 2.0 - 1.0);
        normalOpaque = normalize(uvnSpaceToViewSpace * normalOpaque);

        shininess = texture2D(textureAtlasNormal, texCoord).w;
    #endif
#endif

#ifdef FEATURE_REFRACTIVE_PASS
    vec2 normalWaterOffset;
    vec3 normalWater = waterNormalViewSpace;
    bool isWater = false;
    bool isOceanWater = false;

    if (BLOCK_HINT_WATER_SURFACE == v_blockHint && isUpside == 1) {
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

#if defined (FEATURE_REFRACTIVE_PASS) || defined (FEATURE_USE_FORWARD_LIGHTING)
    vec3 sunVecViewAdjusted = sunVecView;

    /* DAYLIGHT BECOMES... MOONLIGHT! */
    // Now featuring linear interpolation to make the transition smoother... :-)
    if (daylight < 0.1) {
        sunVecViewAdjusted = mix(sunVecViewAdjusted * -1.0, sunVecViewAdjusted, daylight / 0.1);
    }
#endif

    vec4 color = vec4(0.0, 0.0, 0.0, 1.0);

#if !defined (FEATURE_REFRACTIVE_PASS)
    color = texture2D(textureAtlas, texCoord.xy);

    #if defined FEATURE_ALPHA_REJECT
        if (color.a < 0.1) {
            discard;
        }
    #endif
#endif

    // Calculate daylight lighting value
    float daylightValue = v_sunlight;
    // Calculate blocklight lighting value
    float blocklightValue = v_blocklight;
    // ...and finally the occlusion value
    float occlusionValue = expOccValue(v_ambientLight);

    float blocklightColorBrightness = calcBlocklightColorBrightness(blocklightValue
    #if defined (FLICKERING_LIGHT)
            , flickeringLightOffset
    #endif
    );
    vec3 blocklightColorValue = calcBlocklightColor(blocklightColorBrightness);

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
        float specularHighlight = WATER_SPEC * calcDayAndNightLightingFactor(daylightValue, daylight) * calcSpecLightNormalized(normalWater, sunVecViewAdjusted, normalizedViewPos, waterSpecExp);
        color.xyz += vec3(specularHighlight, specularHighlight, specularHighlight);

        vec4 reflectionColor = vec4(texture2D(textureWaterReflection, projectedPos + normalWaterOffset.xy * waterRefraction).xyz, 1.0);
        vec4 refractionColor = vec4(texture2D(texSceneOpaque, projectedPos + normalWaterOffset.xy * waterRefraction).xyz, 1.0);

        vec4 litWaterTint = vec4(WATER_TINT) * vec4(combinedLightValue.x, combinedLightValue.y, combinedLightValue.z, 1.0);

        /* FRESNEL */
        if (!swimming) {
            float f = fresnel(dot(normalWater, normalizedViewPos), waterFresnelBias, waterFresnelPow);
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
    gl_FragData[2].rgba = vec4(blocklightColorBrightness, daylightValue, 0.0, 0.0);
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
    // Encode shininess value into the normal alpha channel
    gl_FragData[1].a = shininess;
#endif

#if !defined (FEATURE_REFRACTIVE_PASS) && !defined (FEATURE_USE_FORWARD_LIGHTING)
    gl_FragData[1].rgb = vec3(normalOpaque.x / 2.0 + 0.5, normalOpaque.y / 2.0 + 0.5, normalOpaque.z / 2.0 + 0.5);
#elif defined (FEATURE_REFRACTIVE_PASS)
    gl_FragData[1].rgb = vec3(normalWater.x / 2.0 + 0.5, normalWater.y / 2.0 + 0.5, normalWater.z / 2.0 + 0.5);
#endif
}
