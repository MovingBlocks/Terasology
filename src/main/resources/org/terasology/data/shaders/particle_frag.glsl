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

uniform bool carryingTorch = false;

varying vec4 vertexWorldPos;

void main(){
    vec4 color = texture2D(textureAtlas, vec2(gl_TexCoord[0].x + texOffsetX , gl_TexCoord[0].y + texOffsetY ));

    float torchlight = 0.0;

    // Apply torchlight
    if (carryingTorch)
        torchlight = calcTorchlight(1.0, vertexWorldPos.xyz);

    color.rgb *= colorOffset.rgb;

    float lightValue = expLightValue(light);
    color.rgb *= clamp(lightValue + torchlight, 0.0, 1.0);

    gl_FragColor = color;
}
