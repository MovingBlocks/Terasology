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

uniform float radius = 16.0;

void main() {
    const vec2 vTexelSize = vec2( 1.0/1024.0, 1.0/1024.0 );

    const vec2 vTaps[12] = vec2[12](
        vec2(-0.326212,-0.40581), vec2(-0.840144,-0.07358),
        vec2(-0.695914,0.457137), vec2(-0.203345,0.620716),
        vec2(0.96234,-0.194983), vec2(0.473434,-0.480026),
        vec2(0.519456,0.767022), vec2(0.185461,-0.893124),
        vec2(0.507431,0.064425), vec2(0.89642,0.412458),
        vec2(-0.32194,-0.932615), vec2(-0.791559,-0.59771)
    );

    vec4 cSampleAccum = vec4(0.0, 0.0, 0.0, 0.0);

    for (int nTapIndex = 0; nTapIndex < 12; nTapIndex++)
    {
        vec2 vTapCoord = gl_TexCoord[0].xy + vTexelSize * vTaps[nTapIndex] * radius;
        cSampleAccum += texture2D(tex, vTapCoord);
    }

    gl_FragData[0].rgba = cSampleAccum / 12.0;
}
