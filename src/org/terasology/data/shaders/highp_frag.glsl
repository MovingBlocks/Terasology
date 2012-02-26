/*
 * Copyright 2011 Benjamin Glatzel <benjamin.glatzel@me.com>.
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

uniform sampler2D tex;

void main() {
    vec4 color = texture2D(tex, gl_TexCoord[0].xy);
    float lum = 0.2126 * color.r + 0.7152 * color.g + 0.0722 * color.b;

    if (lum > 0.95)
        gl_FragColor = vec4(1.0);
    else
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
}
