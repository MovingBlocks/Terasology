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
uniform vec2 texOffset = vec2(0.0, 0.0);
uniform vec2 texScale = vec2(1.0, 1.0);

uniform vec4 colorOffset = vec4(1.0, 1.0, 1.0, 1.0);

varying vec3 normal;
varying vec4 vertexViewPos;

void main() {
    vec4 color = texture2D(textureAtlas, gl_TexCoord[0].xy * texScale.xy + texOffset.xy);

#if defined (FEATURE_ALPHA_REJECT)
    if (color.a < 0.1) {
        discard;
    }
#endif

    // Particles are currently renderer using forward rendering
    color.rgb *= light;

    gl_FragData[0].a = color.a * colorOffset.a;
    gl_FragData[0].rgb = color.rgb * colorOffset.rgb * gl_FragData[0].a;

    // No normals available
    gl_FragData[1].rgba = vec4(0.0, 0.0, 0.0, 0.0);
}
