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

uniform sampler2D diffuse;

// TODO: Add normal mapping support
//uniform sampler2D normalMap;

uniform float blockLight = 1.0;
uniform float sunlight = 1.0;

uniform vec3 colorOffset;
uniform bool textured;

varying vec3 normal;

void main(){
    vec4 color;

    if (textured) {
        color = texture2D(diffuse, vec2(gl_TexCoord[0].x , gl_TexCoord[0].y));
        color.rgb *= colorOffset.rgb;
        gl_FragData[0].rgba = color;
    } else {
        color = vec4(colorOffset.r, colorOffset.g, colorOffset.b, 1.0);
        gl_FragData[0].rgba = color;
    }

    gl_FragData[1].rgba = vec4(normal.x / 2.0 + 0.5, normal.y / 2.0 + 0.5, normal.z / 2.0 + 0.5, sunlight);
    gl_FragData[2].rgba = vec4(blockLight, blockLight, blockLight, 0.0);
}
