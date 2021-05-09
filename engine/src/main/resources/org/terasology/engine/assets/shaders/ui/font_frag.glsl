#version 330 core
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

in vec2 v_relPos;
in vec2 v_uv0;
in vec4 v_color0;

void main(){
    if (v_relPos.x < croppingBoundaries.x || v_relPos.x > croppingBoundaries.y || v_relPos.y < croppingBoundaries.z || v_relPos.y > croppingBoundaries.w) {
        discard;
    }
    vec4 diffColor = texture2D(texture, v_uv0);
    gl_FragData[0].rgba = diffColor * v_color0;
}
