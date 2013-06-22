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

uniform float sunExponent;
uniform float moonExponent;

const vec4 eyePos = vec4(0.0, 0.0, 0.0, 1.0);

#define HIGHLIGHT_BLEND_START 0.1
#define SUN_HIGHLIGHT_INTENSITY_FACTOR 4.0
#define MOON_HIGHLIGHT_INTENSITY_FACTOR 1.0

void main () {
    vec3 v = normalize (position.xyz);
    vec3 l = normalize (sunVec.xyz);

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

    float blendNight = clamp((0.707 - sunVec.y) * (1.0 - lDotV), 0.0, 1.0);

    vec4 skyColor = vec4(0.0, 0.0, 0.0, 1.0);

    /* PROCEDURAL SKY COLOR */
    vec4 cloudsColor = texture2D(texSky180, gl_TexCoord[0].xy);
    vec4 cloudsColorNight =  texture2D(texSky90, gl_TexCoord[0].xy);

    /* DAY AND NIGHT TEXTURES */
    skyColor.rgb = daylight * cloudsColor.rgb + blendNight * cloudsColorNight.rgb;
    skyColor.rgb *= mix(convertColorYxy(colorYxy, colorExp).rgb * 1.5, vec3(0.1, 0.1, 0.1), blendNight);

    skyColor.rgb += vec3((1.0 - cloudsColor.r) * sunHighlight + (1.0 - cloudsColor.r) * moonHighlight);

    gl_FragData[0].rgba = skyColor.rgba;
    gl_FragData[1].rgba = vec4(0.0, 0.0, 0.0, 1.0);

    // The sky is always fully lit
    gl_FragData[2].rgba = vec4(1.0, 1.0, 1.0, 0.0);
}
