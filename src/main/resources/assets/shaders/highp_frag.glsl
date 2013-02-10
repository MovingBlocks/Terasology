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

uniform sampler2D tex;
uniform float highPassThreshold = 1.25;

void main() {
    vec4 color = texture2D(tex, gl_TexCoord[0].xy);
    float avgBrightness = (color.r + color.g + color.b) / 3.0;

    if (avgBrightness > highPassThreshold)
        gl_FragData[0].rgba = color;
    else
        gl_FragData[0].rgba = vec4(0.0, 0.0, 0.0, 1.0);
}
