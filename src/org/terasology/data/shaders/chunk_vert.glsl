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

varying vec3 normal;
varying vec4 vertexWorldPosRaw;
varying vec4 vertexWorldPos;
varying vec3 eyeVec;
varying vec3 lightDir;

varying float flickering;

uniform float time;
uniform float wavingCoordinates[32];
uniform vec2 waterCoordinate;
uniform vec2 lavaCoordinate;
uniform vec3 chunkOffset;

void main()
{
	gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_TexCoord[1] = gl_MultiTexCoord1;

	vertexWorldPosRaw = gl_Vertex;

	vertexWorldPos = gl_ModelViewMatrix * vertexWorldPosRaw;

	lightDir = gl_LightSource[0].position.xyz;
	eyeVec = -vertexWorldPos.xyz;

    normal = gl_NormalMatrix * gl_Normal;
    gl_FrontColor = gl_Color;

	float distance = length(vertexWorldPos);

#ifdef FLICKERING_LIGHT
	flickering = (sin(timeToTick(time, 1.0) + 1.0) / 32.0);
#else
	flickering = 0.0;
#endif

#ifdef ANIMATED_WATER_AND_GRASS
    vec3 vertexChunkPos = vertexWorldPosRaw.xyz + chunkOffset.xyz;

    if (distance < 64.0) {
        // GRASS ANIMATION
        for (int i=0; i < 32; i+=2) {
           if (gl_TexCoord[0].x >= wavingCoordinates[i] && gl_TexCoord[0].x < wavingCoordinates[i] + TEXTURE_OFFSET && gl_TexCoord[0].y >= wavingCoordinates[i+1] && gl_TexCoord[0].y < wavingCoordinates[i+1] + TEXTURE_OFFSET) {
               if (gl_TexCoord[0].y < wavingCoordinates[i+1] + TEXTURE_OFFSET / 2.0) {
                   vertexWorldPos.x += sin(timeToTick(time, 1.0) + vertexChunkPos.x) * 0.3;
                   vertexWorldPos.y += sin(timeToTick(time, 0.5) + vertexChunkPos.x) * 0.1;
               }
           }
        }
    }

    if (gl_TexCoord[0].x >= waterCoordinate.x && gl_TexCoord[0].x < waterCoordinate.x + TEXTURE_OFFSET && gl_TexCoord[0].y >= waterCoordinate.y && gl_TexCoord[0].y < waterCoordinate.y + TEXTURE_OFFSET) {
        vertexWorldPos.y += sin(timeToTick(time, 1.0) + vertexChunkPos.x +  + vertexChunkPos.z) * sin(timeToTick(time, 0.5) + vertexChunkPos.x  + vertexChunkPos.z + 16.0) * 0.1;
    } else if (gl_TexCoord[0].x >= lavaCoordinate.x && gl_TexCoord[0].x < lavaCoordinate.x + TEXTURE_OFFSET && gl_TexCoord[0].y >= lavaCoordinate.y && gl_TexCoord[0].y < lavaCoordinate.y + TEXTURE_OFFSET) {
        vertexWorldPos.y += sin(timeToTick(time, 0.5) + vertexChunkPos.x + vertexChunkPos.z) * 0.1;
    }
#endif

    gl_Position = gl_ProjectionMatrix * vertexWorldPos;
}
