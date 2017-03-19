/*
 * Copyright 2013 Moving Blocks
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

uniform vec2 offset;
uniform vec4 color;
uniform vec2 scale;
uniform vec2 texOffset;
uniform vec2 texSize;
uniform vec4 croppingBoundaries;

varying vec2 relPos;

void main()
{
    vec4 pos = gl_Vertex;
    pos.xy *= scale;
    pos.xy += offset;
    relPos = pos.xy;
	gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * pos;
    gl_TexCoord[0] = gl_TextureMatrix[0] * gl_MultiTexCoord0;
    gl_TexCoord[0].xy = texOffset + gl_TexCoord[0].xy * (texSize) ;
    gl_FrontColor = color;
}
