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
varying	vec4 McPosition;
varying	float lv;
uniform	vec4 sunPos;

uniform samplerCube texCubeSky;
uniform samplerCube texCubeStars;

vec4 eyePos = vec4(0.0, 0.0, 0.0, 1.0);
uniform float colorExp = 12.0;

vec3 convertColor() {
    if (colorYxy == vec3(0.0, 0.0, 0.0))
        return vec3(0.0, 0.0, 0.0);

    vec3 clrYxy = vec3(colorYxy);
    clrYxy.x = 1.0 - exp (-clrYxy.x / colorExp);

    float ratio = clrYxy.x / clrYxy.z;

    vec3 XYZ;

    XYZ.x = clrYxy.y * ratio;					// X = x * ratio
    XYZ.y = clrYxy.x;							// Y = Y
    XYZ.z = ratio - XYZ.x - XYZ.y;				// Z = ratio - X - Y

    const vec3 rCoeffs = vec3 (3.240479, -1.53715, -0.49853);
    const vec3 gCoeffs = vec3 (-0.969256, 1.875991, 0.041556);
    const vec3 bCoeffs = vec3 (0.055684, -0.204043, 1.057311);

    return vec3 (dot(rCoeffs, XYZ), dot(gCoeffs, XYZ), dot(bCoeffs, XYZ));
}

void main () {
    vec3 v = normalize ( McPosition.xyz );
    vec3 l;

    if (daylight > 0.0)
       l = normalize (sunPos.xyz);
    else
       l = normalize (-sunPos.xyz);

    float sunHighlight = pow(max(0.0, dot(l, v)), 1024.0) * 16.0;
    float posSunY = 0.0;

    if (sunPos.y > 0.0){
        posSunY = sunPos.y;
    }

    /* ALPHA STARRY NIGHT */
    float alpha  = clamp((0.7 - posSunY) * (1.0 - lv), 0.0, 1.0);

    vec4 skyColor = vec4(clamp(convertColor(), 0.0, 1.0) + sunHighlight, 1.0);
    skyColor += daylight * textureCube (texCubeSky, skyVec);
    skyColor += alpha * textureCube (texCubeStars, skyVecR);

    gl_FragData[0].rgba = skyColor;
}
