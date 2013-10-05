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

uniform sampler2D texSky180;
uniform sampler2D texSky90;

uniform float colorExp;
uniform vec3 zenith;

uniform vec4 skySettings;
#define sunExponent skySettings.x
#define moonExponent skySettings.y

#define skyDaylightBrightness skySettings.z
#define skyNightBrightness skySettings.w

const vec4 eyePos = vec4(0.0, 0.0, 0.0, 1.0);

#define HIGHLIGHT_BLEND_START 0.1
#define SUN_HIGHLIGHT_INTENSITY_FACTOR 1.0
#define MOON_HIGHLIGHT_INTENSITY_FACTOR 1.0

void main () {
    vec3 v = normalize(position.xyz);
    vec3 l = normalize(sunVec.xyz);

    float lDotV = dot(l, v);
    float negLDotV = dot(-l, v);

    float sunHighlight = 0.0;
    if (lDotV >= 0.0 && l.y >= 0.0) {
       sunHighlight = pow(lDotV, sunExponent) * SUN_HIGHLIGHT_INTENSITY_FACTOR;
    }
    if (l.y < HIGHLIGHT_BLEND_START && l.y >= 0.0) {
       sunHighlight *= 1.0 - (HIGHLIGHT_BLEND_START - l.y) / HIGHLIGHT_BLEND_START;
    }

    float moonHighlight = 0.0;
    if (negLDotV >= 0.0 && -l.y >= 0.0) {
       moonHighlight = pow(negLDotV, moonExponent) * MOON_HIGHLIGHT_INTENSITY_FACTOR;
    }
    if (-l.y < HIGHLIGHT_BLEND_START && -l.y >= 0.0) {
       moonHighlight *= 1.0 - (HIGHLIGHT_BLEND_START + l.y) / HIGHLIGHT_BLEND_START;
    }

    vec4 skyColor = vec4(0.0, 0.0, 0.0, 1.0);

    /* PROCEDURAL SKY COLOR */
    vec4 cloudsColor = texture2D(texSky180, gl_TexCoord[0].xy);
    vec4 cloudsColorNight = texture2D(texSky90, gl_TexCoord[0].xy);

    /* DAY AND NIGHT TEXTURES */
    skyColor.rgb = skyDaylightBrightness * daylight * cloudsColor.rgb + (1.0 - daylight) * skyNightBrightness * cloudsColorNight.rgb;
    skyColor.rgb *= mix(convertColorYxy(colorYxy, colorExp).rgb, vec3(1.0, 1.0, 1.0), 1.0 - daylight);

    skyColor.rgb += vec3((1.0 - cloudsColor.r) * sunHighlight + (1.0 - cloudsColor.r) * moonHighlight);

    gl_FragData[0].rgba = skyColor.rgba;
}
