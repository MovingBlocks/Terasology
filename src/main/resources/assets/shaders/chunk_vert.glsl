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

 #define WAVING_COORDINATE_COUNT 32

varying vec3 normal;
varying vec4 vertexWorldPosRaw;
varying vec4 vertexWorldPos;
varying vec4 vertexPos;
varying vec3 lightDir;
varying vec3 waterNormal;

varying float flickering;
varying float flickeringAlternative;

uniform float blockScale = 1.0;

uniform float time;
uniform float wavingCoordinates[WAVING_COORDINATE_COUNT];
uniform vec2 waterCoordinate;
uniform vec2 lavaCoordinate;
uniform vec3 chunkOffset;

uniform float animated;

void main()
{
	gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_TexCoord[1] = gl_MultiTexCoord1;

	vertexWorldPosRaw = gl_Vertex;

	vertexWorldPos = gl_ModelViewMatrix * vertexWorldPosRaw;
	waterNormal = gl_NormalMatrix * vec3(0,1,0);

	lightDir = gl_LightSource[0].position.xyz;

    normal = gl_NormalMatrix * gl_Normal;
    gl_FrontColor = gl_Color;

#ifdef FLICKERING_LIGHT
	flickering = smoothTriangleWave(timeToTick(time, 1.0)) / 64.0;
	flickeringAlternative = smoothTriangleWave(timeToTick(time, 1.0) + 0.37281) / 64.0;
#else
	flickering = 0.0;
	flickeringAlternative = 0.0f;
#endif

#ifdef ANIMATED_WATER_AND_GRASS
    vec3 vertexChunkPos = vertexWorldPosRaw.xyz + chunkOffset.xyz;

    if (animated > 0.0) {
        // GRASS ANIMATION
        for (int i=0; i < WAVING_COORDINATE_COUNT; i+=2) {
           if (gl_TexCoord[0].x >= wavingCoordinates[i] && gl_TexCoord[0].x < wavingCoordinates[i] + TEXTURE_OFFSET && gl_TexCoord[0].y >= wavingCoordinates[i+1] && gl_TexCoord[0].y < wavingCoordinates[i+1] + TEXTURE_OFFSET) {
               if (gl_TexCoord[0].y < wavingCoordinates[i+1] + TEXTURE_OFFSET / 2.0) {
                   vertexWorldPos.x += (smoothTriangleWave(timeToTick(time, 0.2) + vertexChunkPos.x * 0.1 + vertexChunkPos.z * 0.1) * 2.0 - 1.0) * 0.1 * blockScale;
                   vertexWorldPos.y += (smoothTriangleWave(timeToTick(time, 0.1) + vertexChunkPos.x * -0.5 + vertexChunkPos.z * -0.5) * 2.0 - 1.0) * 0.05 * blockScale;
               }
           }
        }
    }

    if (gl_TexCoord[0].x >= waterCoordinate.x && gl_TexCoord[0].x < waterCoordinate.x + TEXTURE_OFFSET && gl_TexCoord[0].y >= waterCoordinate.y && gl_TexCoord[0].y < waterCoordinate.y + TEXTURE_OFFSET) {
        vertexWorldPos.y += (smoothTriangleWave(timeToTick(time, 0.1) + vertexChunkPos.x * 0.05 + vertexChunkPos.z * 0.05) * 2.0 - 1.0) * 0.1 * blockScale
        + (smoothTriangleWave(timeToTick(time, 0.25)  + vertexChunkPos.x *-0.25 + vertexChunkPos.z * 0.25) * 2.0 - 1.0) * 0.025 * blockScale;
    }
#if 0
    else if (gl_TexCoord[0].x >= lavaCoordinate.x && gl_TexCoord[0].x < lavaCoordinate.x + TEXTURE_OFFSET && gl_TexCoord[0].y >= lavaCoordinate.y && gl_TexCoord[0].y < lavaCoordinate.y + TEXTURE_OFFSET) {
        vertexWorldPos.y += smoothTriangleWave(timeToTick(time, 0.05) + vertexChunkPos.x * 0.1 + vertexChunkPos.z * 0.1) * 0.2 * blockScale;
    }
#endif

#endif

    vertexPos = gl_ProjectionMatrix * vertexWorldPos;
    gl_Position = vertexPos;
}
