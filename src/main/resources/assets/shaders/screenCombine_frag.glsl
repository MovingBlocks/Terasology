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

uniform sampler2D texScene;
#ifdef SSAO
uniform sampler2D texSsao;
#endif

void main() {
    vec4 color = texture2D(texScene, gl_TexCoord[0].xy);
#ifdef SSAO
    float ssao = texture2D(texSsao, gl_TexCoord[0].xy).x;
    color *= vec4(ssao, ssao, ssao, 1.0);
#endif

    gl_FragColor = color;
}
