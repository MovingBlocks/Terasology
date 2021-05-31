#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

#ifdef FEATURE_REFRACTIVE_PASS
out vec3 waterNormalViewSpace;
#endif

#if defined (ANIMATED_WATER) && defined (FEATURE_REFRACTIVE_PASS)
const vec3 normalDiffOffset = vec3(-1.0, 0.0, 1.0);
const vec2 normalDiffSize = vec2(2.0, 0.0);

uniform float waveIntensityFalloff;
uniform float waveSizeFalloff;
uniform float waveSpeedFalloff;
uniform float waveSize;
uniform float waveIntensity;
uniform float waveSpeed;
uniform float waterOffsetY;
uniform float waveOverallScale;

const vec2[] waveDirections = vec2[](
    vec2(-0.613392, 0.617481),
    vec2(0.170019, -0.040254),
    vec2(-0.299417, 0.791925),
    vec2(0.645680, 0.493210),
    vec2(-0.651784, 0.717887),
    vec2(0.421003, 0.027070),
    vec2(-0.817194, -0.271096),
    vec2(-0.705374, -0.668203),
    vec2(0.977050, -0.108615),
    vec2(0.063326, 0.142369),
    vec2(0.203528, 0.214331),
    vec2(-0.667531, 0.326090),
    vec2(-0.098422, -0.295755),
    vec2(-0.885922, 0.215369),
    vec2(0.566637, 0.605213),
    vec2(0.039766, -0.396100)
);

float calcWaterHeightAtOffset(vec2 worldPos) {
    float height = 0.0;

    float size = waveSize;
    float intens = waveIntensity;
    float timeFactor = waveSpeed;
    for (int i=0; i<OCEAN_OCTAVES; ++i) {
        height += (smoothTriangleWave(timeToTick(time, timeFactor) + worldPos.x * waveDirections[i].x
            * size + worldPos.y * waveDirections[i].y * size) * 2.0 - 1.0) * intens;

        size *= waveSizeFalloff;
        intens *= waveIntensityFalloff;
        timeFactor *= waveSpeedFalloff;
    }

    return (height / float(OCEAN_OCTAVES)) * waveOverallScale;
}

vec4 calcWaterNormalAndOffset(vec2 worldPosRaw) {
    float s11 = calcWaterHeightAtOffset(worldPosRaw.xy);
    float s01 = calcWaterHeightAtOffset(worldPosRaw.xy + normalDiffOffset.xy);
    float s21 = calcWaterHeightAtOffset(worldPosRaw.xy + normalDiffOffset.zy);
    float s10 = calcWaterHeightAtOffset(worldPosRaw.xy + normalDiffOffset.yx);
    float s12 = calcWaterHeightAtOffset(worldPosRaw.xy + normalDiffOffset.yz);

    vec3 va = normalize(vec3(normalDiffSize.x, s21-s01, normalDiffSize.y));
    vec3 vb = normalize(vec3(normalDiffSize.y, s10-s12, -normalDiffSize.x));

    return vec4(cross(va,vb), s11);
}
#endif

#if defined (FLICKERING_LIGHT)
out float flickeringLightOffset;
#endif

#if defined (NORMAL_MAPPING)
out vec3 worldSpaceNormal;
#endif

uniform float blockScale = 1.0;
uniform vec3 chunkPositionWorld;

// waving blocks
uniform bool animated;

uniform mat4 modelViewMatrix;
uniform mat4 projectionMatrix;
uniform mat3 normalMatrix;

out vec3 normal;

out vec3 vertexWorldPos;
out vec4 vertexViewPos;
out vec4 vertexProjPos;

out vec3 sunVecView;

out vec2 v_uv0;
out float v_sunlight;
out float v_blocklight;
out float v_ambientLight;
flat out int isUpside;
flat out int v_blockHint;

layout (location = 0) in vec3 in_vert;
layout (location = 1) in vec3 in_normal;
layout (location = 2) in vec2 in_uv0;

layout (location = 3) in int in_flags;
layout (location = 4) in float in_frames;

layout (location = 5) in float in_sunlight;
layout (location = 6) in float in_blocklight;
layout (location = 7) in float in_ambientlight;

void main() {

    v_uv0 = in_uv0;
    v_sunlight = in_sunlight;
    v_blocklight = in_blocklight;
    v_ambientLight = in_ambientlight;
    v_blockHint = in_flags;
    vertexViewPos = modelViewMatrix * vec4(in_vert, 1.0);
    vertexWorldPos = in_vert + chunkPositionWorld.xyz;

    if (in_frames > 0) {
        float globalFrameIndex = floor(time * 6 *60*60*24/48); // 6Hz at default world time scale
        float frameIndex = mod(globalFrameIndex, in_frames);
        float frame_x = in_uv0.x + (frameIndex * TEXTURE_OFFSET);
        v_uv0.y = in_uv0.y + floor(frame_x) * TEXTURE_OFFSET;
        v_uv0.x = mod(frame_x, 1);
    }

    sunVecView = (modelViewMatrix * vec4(sunVec.x, sunVec.y, sunVec.z, 0.0)).xyz;

    isUpside = in_normal.y > 0.9 ? 1 : 0;

#if defined (NORMAL_MAPPING)
    worldSpaceNormal = in_normal;
#endif
    normal = normalMatrix * in_normal;


#ifdef FLICKERING_LIGHT
    flickeringLightOffset = smoothTriangleWave(timeToTick(time, 0.5)) / 16.0;
    flickeringLightOffset += smoothTriangleWave(timeToTick(time, 0.25) + 0.3762618) / 8.0;
    flickeringLightOffset += smoothTriangleWave(timeToTick(time, 0.1) + 0.872917) / 4.0;
#endif

#ifdef ANIMATED_GRASS
    if (animated) {
        // GRASS ANIMATION
        if (v_blockHint == BLOCK_HINT_WAVING) {
           // Only animate the upper two vertices
           if (mod(v_uv0.y, TEXTURE_OFFSET) < TEXTURE_OFFSET / 2.0) {
               vertexViewPos.x += (smoothTriangleWave(timeToTick(time, 0.2) + vertexWorldPos.x * 0.1 + vertexWorldPos.z * 0.1) * 2.0 - 1.0) * 0.1 * blockScale;
               vertexViewPos.y += (smoothTriangleWave(timeToTick(time, 0.1) + vertexWorldPos.x * -0.5 + vertexWorldPos.z * -0.5) * 2.0 - 1.0) * 0.05 * blockScale;
           }
        } else if (v_blockHint == BLOCK_HINT_WAVING_BLOCK) {
            vertexViewPos.x += (smoothTriangleWave(timeToTick(time, 0.1) + vertexWorldPos.x * 0.01 + vertexWorldPos.z * 0.01) * 2.0 - 1.0) * 0.01 * blockScale;
            vertexViewPos.y += (smoothTriangleWave(timeToTick(time, 0.15) + vertexWorldPos.x * -0.01 + vertexWorldPos.z * -0.01) * 2.0 - 1.0) * 0.05 * blockScale;
            vertexViewPos.z += (smoothTriangleWave(timeToTick(time, 0.1) + vertexWorldPos.x * -0.01 + vertexWorldPos.z * -0.01) * 2.0 - 1.0) * 0.01 * blockScale;
        }
    }
#endif

#if defined (FEATURE_REFRACTIVE_PASS)
    #if defined (ANIMATED_WATER)
        if (v_blockHint == BLOCK_HINT_WATER_SURFACE && isUpside == 1) {
            vec4 normalAndOffset = calcWaterNormalAndOffset(vertexWorldPos.xz);

            waterNormalViewSpace = normalMatrix * normalAndOffset.xyz;
            vertexViewPos += modelViewMatrix[1] * (normalAndOffset.w + waterOffsetY);
        }
    #else
        waterNormalViewSpace = normalMatrix * vec3(0.0, 1.0, 0.0);
    #endif
#endif

    vertexProjPos = projectionMatrix * vertexViewPos;
    gl_Position = vertexProjPos;

#if defined (FEATURE_ALPHA_REJECT)
    //TODO: find the right methods to determine which vertices have normals facing away from the viewpoint, and only alter those
    //if (normal.x * vertexViewPos.x < 0 || normal.y * vertexViewPos.y < 0 || normal.z * vertexViewPos.z < 0) {
        gl_Position.z -= 0.001;
    //}
#endif
}
