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

uniform float light = 1.0;
uniform float texOffsetX = 0.0;
uniform float texOffsetY = 0.0;

uniform vec3 colorOffset = vec3(1.0, 1.0, 1.0);

varying vec3 normal;
varying vec4 vertexViewPos;

void main(){
    vec4 color = texture2D(textureAtlas, vec2(gl_TexCoord[0].x + texOffsetX , gl_TexCoord[0].y + texOffsetY ));

    if (color.a < 0.5)
        discard;

    float torchlight = 0.0;

    // Apply torchlight
    if (carryingTorch > 0.99) {
        torchlight = calcTorchlight(1.0, vertexViewPos.xyz);
    }

    color.rgb *= colorOffset.rgb;
    color.rgb *= light + torchlight;

    gl_FragData[0].rgba = color;
    gl_FragData[1].rgba = vec4(normal.x / 2.0 + 0.5, normal.y / 2.0 + 0.5, normal.z / 2.0 + 0.5, 0.0f);
}
