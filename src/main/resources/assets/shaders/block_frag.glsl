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

uniform sampler2D textureAtlas;

uniform vec3 colorOffset = vec3(1.0, 1.0, 1.0);

uniform bool textured = false;

uniform float blockLight = 1.0;
uniform float sunlight = 1.0;

uniform float alpha = 1.0;

varying vec3 normal;

void main(){
    vec4 color;

    if (textured) {
        color = gl_Color * texture2D(textureAtlas, gl_TexCoord[0].xy);
    } else {
        color = gl_Color;
    }

    color.a *= alpha;

    if (color.a < 0.1) {
        discard;
    }

    color.rgb *= colorOffset.rgb;

    gl_FragData[0].rgba = color;
    gl_FragData[1].rgba = vec4(normal.x / 2.0 + 0.5, normal.y / 2.0 + 0.5, normal.z / 2.0 + 0.5, sunlight);
    gl_FragData[2].rgba = vec4(blockLight, blockLight, blockLight, 0.0);
}
