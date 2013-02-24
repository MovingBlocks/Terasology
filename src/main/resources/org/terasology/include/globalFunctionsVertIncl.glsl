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

#define OCEAN_OCTAVES 16

uniform bool swimming;
uniform float carryingTorch;
uniform float viewingDistance;
uniform float daylight;
uniform float tick;
uniform float time;

uniform vec3 sunVec;
uniform vec3 cameraDirection;

float timeToTick(float time, float speed) {
    return time * 4000.0 * speed;
}

// Crytek fast sin/cos approximations

float smoothCurve( float x ) {
  return x * x * ( 3.0 - 2.0 * x );
}
float triangleWave( float x ) {
  return abs( fract( x + 0.5 ) * 2.0 - 1.0 );
}
float smoothTriangleWave( float x ) {
  return smoothCurve( triangleWave( x ) ) * 2.0 - 1.0 ;
}

bool checkFlag (int flag, float val) {
    return val > float(flag) - 0.5 && val < float(flag) + 0.5;
}
