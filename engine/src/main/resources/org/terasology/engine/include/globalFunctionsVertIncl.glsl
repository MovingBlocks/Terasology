// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

float timeToTick(float time, float speed) {
    return time * 4000.0 * speed;
}

float smoothCurve(float x) {
  return x * x * (3.0 - 2.0 * x);
}

float triangleWave(float x) {
  return abs(fract(x + 0.5) * 2.0 - 1.0);
}

float smoothTriangleWave(float x) {
  return smoothCurve(triangleWave(x)) * 2.0 - 1.0;
}

