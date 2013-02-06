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

varying	vec3 colorYxy;
varying vec3 skyVec;
varying vec3 skyVecR;
varying	vec4 position;

varying	float lv;

uniform samplerCube texCubeSky;
uniform samplerCube texCubeStars;

uniform	vec4 sunPos;
uniform float colorExp;
uniform vec3 zenith;

const vec4 eyePos = vec4(0.0, 0.0, 0.0, 1.0);

void main () {
    vec3 v = normalize (position.xyz);
    vec3 l = normalize (sunPos.xyz);

    float lDotV = max(0.0, dot(l, v));
    float sunHighlight = pow(lDotV, 1024.0) * 16.0;
    float posSunY = 0.0;

    if (sunPos.y > 0.0){
        posSunY = sunPos.y;
    }

    float alpha = clamp((0.7 - posSunY) * (1.0 - lv), 0.0, 1.0);

    vec4 cloudsColor = textureCube(texCubeSky, skyVec);
    sunHighlight *= clamp(1.0 - (cloudsColor.r + cloudsColor.g + cloudsColor.b), 0.0, 1.0);

    vec4 skyColor = vec4(convertColorYxy(colorYxy, colorExp) + sunHighlight, 1.0);
    skyColor.rgb += daylight * cloudsColor.rgb;
    skyColor.rgb += alpha * textureCube(texCubeStars, skyVecR);

    gl_FragData[0].rgb = skyColor;
    gl_FragData[1].rgba = vec4(0, 0, 0, 0);
}
