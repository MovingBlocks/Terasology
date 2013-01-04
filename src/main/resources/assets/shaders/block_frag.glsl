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

uniform float light;
uniform vec3 colorOffset;
uniform bool textured;
uniform bool carryingTorch;

varying vec3 normal;
varying vec4 vertexWorldPos;

void main(){
    vec4 color;

    if (textured) {
        color = texture2D(textureAtlas, vec2(gl_TexCoord[0].x , gl_TexCoord[0].y));
        color.rgb *= gl_Color.rgb;
    } else {
        color = gl_Color;
    }

    float torchlight = 0.0;

    // Apply torchlight
    if (carryingTorch) {
        torchlight = calcTorchlight(calcLambLight(normal, -normalize(vertexWorldPos.xyz)), vertexWorldPos.xyz);
    }

    // Apply light
    color.rgb *= clamp(light + torchlight, 0.0, 1.0);

    if (textured) {
        color.rgb *= colorOffset.rgb;
        gl_FragColor = color;
    } else {
        gl_FragColor = color;
    }
}
