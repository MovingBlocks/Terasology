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
uniform float alpha;

varying vec3 normal;
varying vec4 vertexWorldPos;

void main(){
    vec4 color;

    color = gl_Color;

    // Apply light
    color.rgb *= clamp(light, 0.0, 1.0);

    color.a = alpha;

    gl_FragColor = color;
}
