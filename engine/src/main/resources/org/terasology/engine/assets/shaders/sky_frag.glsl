#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

in vec4 v_position;
in vec2 v_uv0;
in vec3 v_colorYxy;
in vec3 v_skyVec;

//varying	vec3 colorYxy;
//varying vec3 skyVec;
//varying	vec4 position;

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
    vec3 v = normalize(v_position.xyz);
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
    vec4 cloudsColor = texture(texSky180, v_uv0.xy);
    vec4 cloudsColorNight = texture(texSky90, v_uv0.xy);

    /* DAY AND NIGHT TEXTURES */
    skyColor.rgb = skyDaylightBrightness * daylight * cloudsColor.rgb + (1.0 - daylight) * skyNightBrightness * cloudsColorNight.rgb;
    skyColor.rgb *= mix(convertColorYxy(v_colorYxy, colorExp).rgb, vec3(1.0, 1.0, 1.0), 1.0 - daylight);

    skyColor.rgb += vec3((1.0 - cloudsColor.r) * sunHighlight + (1.0 - cloudsColor.r) * moonHighlight);

    gl_FragData[0].rgba = skyColor.rgba;
}
