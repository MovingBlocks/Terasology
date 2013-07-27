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

varying	vec4 	position;
varying	vec3 	colorYxy;
varying vec3    skyVec;
varying vec3    skyVecR;

uniform float	sunAngle;
uniform	float	turbidity;
uniform vec3    zenith;

const vec4 eyePos = vec4(0.0, 0.0, 0.0, 1.0);

vec3 allWeather(float t, float cosTheta, float cosGamma)
{
	float	gamma      = acos ( cosGamma );
	float	cosGammaSq = cosGamma * cosGamma;
	float	aY =  0.17872 * t - 1.46303;
	float	bY = -0.35540 * t + 0.42749;
	float	cY = -0.02266 * t + 5.32505;
	float	dY =  0.12064 * t - 2.57705;
	float	eY = -0.06696 * t + 0.37027;
	float	ax = -0.01925 * t - 0.25922;
	float	bx = -0.06651 * t + 0.00081;
	float	cx = -0.00041 * t + 0.21247;
	float	dx = -0.06409 * t - 0.89887;
	float	ex = -0.00325 * t + 0.04517;
	float	ay = -0.01669 * t - 0.26078;
	float	by = -0.09495 * t + 0.00921;
	float	cy = -0.00792 * t + 0.21023;
	float	dy = -0.04405 * t - 1.65369;
	float	ey = -0.01092 * t + 0.05291;

	return vec3 ((1.0 + aY * exp(bY/cosTheta)) * (1.0 + cY * exp(dY * gamma) + eY*cosGammaSq),
	              (1.0 + ax * exp(bx/cosTheta)) * (1.0 + cx * exp(dx * gamma) + ex*cosGammaSq),
				  (1.0 + ay * exp(by/cosTheta)) * (1.0 + cy * exp(dy * gamma) + ey*cosGammaSq));
}

vec3 allWeatherSky(float t, float cosTheta, float cosGamma, float cosThetaSun)
{
  float	thetaSun = acos(cosThetaSun);

  vec3	clrYxy = zenith * allWeather(t, cosTheta, cosGamma) / allWeather (t, 1.0, cosThetaSun);
  clrYxy.x *= smoothstep ( 0.0, 0.1, cosThetaSun );

  return clrYxy;
}

void main(void)
{
    float	sa  = sin (sunAngle);
    float	ca  = cos (sunAngle);
    mat3	r   = mat3 (1.0, 0.0, 0.0, 0.0, ca, -sa, 0.0, sa, ca);

    vec3 v          = normalize ((gl_Vertex-eyePos).xyz);
    vec3 l          = sunVec;
    float lv        = dot(l, v);
    skyVecR         = r * v.xyz;
    skyVec          = v.xyz;
    colorYxy        = allWeatherSky(turbidity, max(v.y, 0.0) + 0.05, lv, l.y);
    position        = gl_Vertex;
    gl_Position     = ftransform();
    gl_TexCoord[0]  = gl_MultiTexCoord0;
}