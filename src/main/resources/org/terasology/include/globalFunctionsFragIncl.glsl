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

#define A 0.22
#define B 0.30
#define C 0.10
#define D 0.20
#define E 0.01
#define F 0.30
#define W 11.2

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
    return light * clamp(1.0 - (length(lightPos) / 16.0), 0.0, 1.0);
}

vec4 linearToSrgb(vec4 color) {
     return vec4(pow(color.rgb, vec3(1.0 / GAMMA)), color.a);
}

vec4 srgbToLinear(vec4 color) {
    return vec4(pow(color.rgb, vec3(GAMMA)), color.a);
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