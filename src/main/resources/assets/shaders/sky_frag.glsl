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

uniform sampler2D texSky180;
uniform sampler2D texSky90;

uniform	vec4 sunPos;
uniform float colorExp;
uniform vec3 zenith;

uniform float sunExponent;
uniform float moonExponent;

const vec4 eyePos = vec4(0.0, 0.0, 0.0, 1.0);

void main () {
    vec3 v = normalize (position.xyz);
    vec3 l = normalize (sunPos.xyz);

    float lDotV = max(0.0, dot(l, v));
    float negLDotV = max(0.0, dot(-l, v));

    float sunHighlight = pow(lDotV, sunExponent) * 2.0;
    float moonHighlight = pow(negLDotV, moonExponent) * 1.0;

    float blendNight = clamp((0.7 - sunPos.y) * (1.0 - lv), 0.0, 1.0);

    vec4 cloudsColor = texture2D(texSky180, gl_TexCoord[0].xy);
    vec4 skyColor = vec4(convertColorYxy(colorYxy, colorExp) + (1.0 - cloudsColor.r) * sunHighlight + (1.0 - cloudsColor.r) * moonHighlight, 1.0);
    skyColor.rgb += (daylight * cloudsColor.rgb + blendNight * texture2D(texSky90, gl_TexCoord[0].xy).rgb) / vec3(2.0);

    gl_FragData[0].rgba = skyColor.rgba;
    gl_FragData[1].rgba = vec4(0.0, 0.0, 0.0, 1.0);
}
