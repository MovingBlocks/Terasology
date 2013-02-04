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

#define Z_NEAR 0.1
#define BLUR_START 0.6
#define BLUR_LENGTH 0.05

#define MOTION_BLUR_SAMPLES 8

#define A 0.15
#define B 0.50
#define C 0.10
#define D 0.20
#define E 0.02
#define F 0.30
#define W 11.2

uniform bool swimming;
uniform float viewingDistance;
uniform float daylight;
uniform float tick;
uniform float time;

float linDepth(float depth) {
    return (2.0 * Z_NEAR) / (viewingDistance + Z_NEAR - depth * (viewingDistance - Z_NEAR));
}

vec3 uncharted2Tonemap(vec3 x) {
	return ((x*(A*x+C*B)+D*E)/(x*(A*x+B)+D*F))-E/F;
}

float tonemapReinhard(float brightMax, float exposure) {
    return exposure * (exposure/(brightMax * brightMax) + 1.0) / (exposure + 1.0);
}

float calcLambLight(vec3 normal, vec3 lightVec) {
    return dot(normal,lightVec);
}

float calcSpecLightWithOffset(vec3 normal, vec3 lightVec, vec3 eyeVec, float exp, vec3 offset) {
    vec3 halfWay = normalize(eyeVec+lightVec+vec3(offset.x, offset.y, 0.0));
    return pow(clamp(dot(halfWay, normal), 0.0, 1.0), exp);
}

float calcSpecLight(vec3 normal, vec3 lightVec, vec3 eyeVec, float exp) {
    vec3 halfWay = normalize(eyeVec+lightVec);
    return pow(clamp(dot(halfWay, normal), 0.0, 1.0), exp);
}

float calcTorchlight(float light, vec3 lightPos) {
    return light * clamp(1.0 - (length(lightPos) / 8.0), 0.0, 1.0);
}

vec4 linearToSrgb(vec4 color) {
     return vec4(sqrt(color.rgb), color.a);
}

vec4 srgbToLinear(vec4 color) {
    return vec4(color.rgb * color.rgb, color.a);
}

float expLightValue(float light) {
	float lightScaled = (1.0 - light) * 16.0;
	return pow(0.76, lightScaled);
}

float expOccValue(float light) {
    return light * light;
}

float timeToTick(float time, float speed) {
    return time * 4000.0 * speed;
}

float fresnel(float nDotL, float fresnelBias, float fresnelPow) {
  float facing = (1.0 - nDotL);
  return max(fresnelBias + (1.0 - fresnelBias) * pow(facing, fresnelPow), 0.0);
}

bool checkFlag (int flag, float val) {
    return val > float(flag) - 0.5 && val < float(flag) + 0.5;
}