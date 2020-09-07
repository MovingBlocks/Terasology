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

uniform sampler2D texSceneFinal;

uniform vec2 ocLensCenter;
uniform vec2 ocScreenCenter;
uniform vec2 ocScale;
uniform vec2 ocScaleIn;
uniform vec4 ocHmdWarpParam;

vec2 ocHmdWarp(vec2 in01) {
    vec2 theta = (in01 - ocLensCenter) * ocScaleIn; // Scales to [-1, 1]
    float rSq = theta.x * theta.x + theta.y * theta.y;
    vec2 rVector = theta * (ocHmdWarpParam.x + ocHmdWarpParam.y * rSq + ocHmdWarpParam.z * rSq * rSq + ocHmdWarpParam.w * rSq * rSq * rSq);

    return ocLensCenter + ocScale * rVector;
}


void main() {
    vec2 textCoord = gl_TexCoord[0].xy;
    vec2 tc = ocHmdWarp(textCoord);

    if (!all(equal(clamp(tc, ocScreenCenter - vec2(0.25, 0.5), ocScreenCenter + vec2(0.25, 0.5)), tc))) {
        discard;
    }

    gl_FragData[0].rgba = texture2D(texSceneFinal, tc);
}
