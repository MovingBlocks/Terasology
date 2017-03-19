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

void main() {
    vec4 color = texture2D(texture, vec2(gl_TexCoord[0].x , gl_TexCoord[0].y));

    float highlight = calcLambLight(normal, -normalize(vertexViewPos.xyz));

    color.rgb *= clamp(gl_Color.rgb, 0.0, 1.0) * colorOffset.rgb;
    color.rgb *= light * 0.85 + 0.15 * highlight * light;

    color.a = gl_Color.a;

    gl_FragData[0].rgba = color;
}
