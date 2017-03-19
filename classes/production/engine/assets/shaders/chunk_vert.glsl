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

#ifdef FEATURE_REFRACTIVE_PASS
varying vec3 waterNormalViewSpace;
#endif

#if defined (ANIMATED_WATER) && defined (FEATURE_REFRACTIVE_PASS)
const vec3 normalDiffOffset = vec3(-1.0, 0.0, 1.0);
const vec2 normalDiffSize = vec2(2.0, 0.0);

uniform float waveIntensFalloff;
uniform float waveSizeFalloff;
uniform float waveSpeedFalloff;
uniform float waveSize;
uniform float waveIntens;
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
    float intens = waveIntens;
    float timeFactor = waveSpeed;
    for (int i=0; i<OCEAN_OCTAVES; ++i) {
        height += (smoothTriangleWave(timeToTick(time, timeFactor) + worldPos.x * waveDirections[i].x
            * size + worldPos.y * waveDirections[i].y * size) * 2.0 - 1.0) * intens;

        size *= waveSizeFalloff;
        intens *= waveIntensFalloff;
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
varying float flickeringLightOffset;
#endif

#if defined (NORMAL_MAPPING)
varying vec3 worldSpaceNormal;
varying mat3 normalMatrix;
#endif

uniform float blockScale = 1.0;
uniform vec3 chunkPositionWorld;

uniform bool animated;

varying vec3 normal;

varying vec3 vertexWorldPos;
varying vec4 vertexViewPos;
varying vec4 vertexProjPos;

varying vec3 sunVecView;

varying float isUpside;
varying float blockHint;

void main()
{
	gl_TexCoord[0] = gl_MultiTexCoord0;
	blockHint = int(gl_TexCoord[0].z);

    gl_TexCoord[1] = gl_MultiTexCoord1;

	vertexViewPos = gl_ModelViewMatrix * gl_Vertex;
	vertexWorldPos = gl_Vertex.xyz + chunkPositionWorld.xyz;

	sunVecView = (gl_ModelViewMatrix * vec4(sunVec.x, sunVec.y, sunVec.z, 0.0)).xyz;

	isUpside = (gl_Normal.y > 0.9) ? 1.0 : 0.0;

#if defined (NORMAL_MAPPING)
    normalMatrix = gl_NormalMatrix;
    worldSpaceNormal = gl_Normal;
#endif

    normal = gl_NormalMatrix * gl_Normal;

    gl_FrontColor = gl_Color;

#ifdef FLICKERING_LIGHT
	flickeringLightOffset = smoothTriangleWave(timeToTick(time, 0.5)) / 16.0;
	flickeringLightOffset += smoothTriangleWave(timeToTick(time, 0.25) + 0.3762618) / 8.0;
	flickeringLightOffset += smoothTriangleWave(timeToTick(time, 0.1) + 0.872917) / 4.0;
#endif

#ifdef ANIMATED_GRASS
    if (animated) {
        // GRASS ANIMATION
        if ( checkFlag(BLOCK_HINT_WAVING, blockHint) ) {
           // Only animate the upper two vertices
           if (mod(gl_TexCoord[0].y, TEXTURE_OFFSET) < TEXTURE_OFFSET / 2.0) {
               vertexViewPos.x += (smoothTriangleWave(timeToTick(time, 0.2) + vertexWorldPos.x * 0.1 + vertexWorldPos.z * 0.1) * 2.0 - 1.0) * 0.1 * blockScale;
               vertexViewPos.y += (smoothTriangleWave(timeToTick(time, 0.1) + vertexWorldPos.x * -0.5 + vertexWorldPos.z * -0.5) * 2.0 - 1.0) * 0.05 * blockScale;
           }
        } else if ( checkFlag(BLOCK_HINT_WAVING_BLOCK, blockHint) ) {
            vertexViewPos.x += (smoothTriangleWave(timeToTick(time, 0.1) + vertexWorldPos.x * 0.01 + vertexWorldPos.z * 0.01) * 2.0 - 1.0) * 0.01 * blockScale;
            vertexViewPos.y += (smoothTriangleWave(timeToTick(time, 0.15) + vertexWorldPos.x * -0.01 + vertexWorldPos.z * -0.01) * 2.0 - 1.0) * 0.05 * blockScale;
            vertexViewPos.z += (smoothTriangleWave(timeToTick(time, 0.1) + vertexWorldPos.x * -0.01 + vertexWorldPos.z * -0.01) * 2.0 - 1.0) * 0.01 * blockScale;
        }
    }
#endif

#if defined (FEATURE_REFRACTIVE_PASS)
#if defined (ANIMATED_WATER)
    if (checkFlag(BLOCK_HINT_WATER_SURFACE, blockHint) && isUpside > 0.99) {
        vec4 normalAndOffset = calcWaterNormalAndOffset(vertexWorldPos.xz);

        waterNormalViewSpace = gl_NormalMatrix * normalAndOffset.xyz;
        vertexViewPos.y += normalAndOffset.w + waterOffsetY;
    }
#else
    waterNormalViewSpace = gl_NormalMatrix * vec3(0.0, 1.0, 0.0);
#endif
#endif

    vertexProjPos = gl_ProjectionMatrix * vertexViewPos;
    gl_Position = vertexProjPos;
}
