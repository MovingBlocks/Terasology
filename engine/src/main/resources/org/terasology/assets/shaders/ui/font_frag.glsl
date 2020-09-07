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

varying vec2 relPos;

void main(){
    if (relPos.x < croppingBoundaries.x || relPos.x > croppingBoundaries.y || relPos.y < croppingBoundaries.z || relPos.y > croppingBoundaries.w) {
        discard;
    }
    vec4 diffColor = texture2D(texture, gl_TexCoord[0].xy);

    gl_FragData[0].rgba = diffColor * gl_Color;
}
