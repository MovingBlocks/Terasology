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

#define BLOCK_HINT_WATER            1
#define BLOCK_HINT_LAVA             2
#define BLOCK_HINT_WAVING           4
#define BLOCK_HINT_WAVING_BLOCK     5

varying vec3 normal;
varying vec4 vertexWorldPosRaw;
varying vec4 vertexWorldPos;
varying vec4 vertexPos;
varying vec3 lightDir;
varying vec3 waterNormal;

varying float flickeringLightOffset;
varying float isUpside;
varying float blockHint;

uniform float blockScale = 1.0;
uniform float time;
uniform vec3 chunkOffset;

uniform float animated;

void main()
{
	gl_TexCoord[0] = gl_MultiTexCoord0;
	blockHint = int(gl_TexCoord[0].z);

    gl_TexCoord[1] = gl_MultiTexCoord1;

	vertexWorldPosRaw = gl_Vertex;

	vertexWorldPos = gl_ModelViewMatrix * vertexWorldPosRaw;
	waterNormal = gl_NormalMatrix * vec3(0,1,0);

	isUpside = gl_Normal.y == 1.0 ? 1.0 : 0.0;

	lightDir = gl_LightSource[0].position.xyz;

    normal = gl_NormalMatrix * gl_Normal;
    gl_FrontColor = gl_Color;

#ifdef FLICKERING_LIGHT
	flickeringLightOffset = smoothTriangleWave(timeToTick(time, 0.5)) / 64.0;
	flickeringLightOffset += smoothTriangleWave(timeToTick(time, 0.25) + 0.3762618) / 32.0;
	flickeringLightOffset += smoothTriangleWave(timeToTick(time, 0.1) + 0.872917) / 16.0;
#else
	flickeringLightOffset = 0.0;
#endif

#ifdef ANIMATED_WATER_AND_GRASS
    vec3 vertexChunkPos = vertexWorldPosRaw.xyz + chunkOffset.xyz;

    if (animated > 0.0) {
        // GRASS ANIMATION
        if ( checkFlag(BLOCK_HINT_WAVING, blockHint) ) {
           if (mod(gl_TexCoord[0].y, TEXTURE_OFFSET) < TEXTURE_OFFSET / 2.0) {
               vertexWorldPos.x += (smoothTriangleWave(timeToTick(time, 0.2) + vertexChunkPos.x * 0.1 + vertexChunkPos.z * 0.1) * 2.0 - 1.0) * 0.1 * blockScale;
               vertexWorldPos.y += (smoothTriangleWave(timeToTick(time, 0.1) + vertexChunkPos.x * -0.5 + vertexChunkPos.z * -0.5) * 2.0 - 1.0) * 0.05 * blockScale;
           }
        } else if ( checkFlag(BLOCK_HINT_WAVING_BLOCK, blockHint) ) {
            vertexWorldPos.x += (smoothTriangleWave(timeToTick(time, 0.1) + vertexChunkPos.x * 0.01 + vertexChunkPos.z * 0.01) * 2.0 - 1.0) * 0.01 * blockScale;
            vertexWorldPos.y += (smoothTriangleWave(timeToTick(time, 0.15) + vertexChunkPos.x * -0.01 + vertexChunkPos.z * -0.01) * 2.0 - 1.0) * 0.05 * blockScale;
            vertexWorldPos.z += (smoothTriangleWave(timeToTick(time, 0.1) + vertexChunkPos.x * -0.01 + vertexChunkPos.z * -0.01) * 2.0 - 1.0) * 0.01 * blockScale;
        }
    }

    if ( checkFlag(BLOCK_HINT_WATER, blockHint) ) {
       // Only animate blocks on sea level
       if (vertexWorldPosRaw.y < 32.5 && vertexWorldPosRaw.y > 31.5) {
            vertexWorldPos.y += (smoothTriangleWave(timeToTick(time, 0.1) + vertexChunkPos.x * 0.05 + vertexChunkPos.z * 0.05) * 2.0 - 1.0) * 0.1 * blockScale
            + (smoothTriangleWave(timeToTick(time, 0.25)  + vertexChunkPos.x *-0.25 + vertexChunkPos.z * 0.25) * 2.0 - 1.0) * 0.025 * blockScale;
        }
    }
#endif

    vertexPos = gl_ProjectionMatrix * vertexWorldPos;
    gl_Position = vertexPos;
}
