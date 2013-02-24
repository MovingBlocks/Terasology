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

uniform sampler2D texture;
uniform vec4 colorOffset;

varying vec3 normal;
varying vec4 vertexViewPos;

uniform float light = 1.0;

void main(){
    vec4 color = texture2D(texture, vec2(gl_TexCoord[0].x , gl_TexCoord[0].y));

    float torchlight = 0.0;
    float highlight = calcLambLight(normal, -normalize(vertexViewPos.xyz));

    if (carryingTorch > 0.99)
        torchlight = calcTorchlight(highlight, vertexViewPos.xyz);

    color.rgb *= clamp(gl_Color.rgb, 0.0, 1.0) * colorOffset.rgb;

    float lightValue = expLightValue(light);
    color.rgb *= lightValue * 0.85 + 0.15 * highlight * lightValue + torchlight;

    color.a = gl_Color.a;

    gl_FragData[0].rgba = color;
    gl_FragData[1].rgba = vec4(normal.x / 2.0 + 0.5, normal.y / 2.0 + 0.5, normal.z / 2.0 + 0.5, 0.0f);
}
