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

uniform vec4 croppingBoundaries;
uniform sampler2D texture;
varying vec3 normal;
varying vec2 relPos;

void main(){
    if (relPos.x < croppingBoundaries.x || relPos.x > croppingBoundaries.y || relPos.y < croppingBoundaries.z || relPos.y > croppingBoundaries.w) {
        discard;
    }

    vec4 color = texture2D(texture, vec2(gl_TexCoord[0].x , gl_TexCoord[0].y));

    float light = min(1.0,
                  0.3 * max(0.0, dot(normal, vec3(0, -1, 0))) + 1.0 * max(0.0, dot(normal, vec3(0,0,1))));

    color.rgb = color.rgb * light;

    gl_FragData[0].rgba = color * gl_Color;
}
