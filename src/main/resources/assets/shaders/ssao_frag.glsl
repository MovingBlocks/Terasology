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

uniform float   ssaoStrength;
uniform float   ssaoTotalStrength;
uniform float   ssaoFalloff;
uniform float   ssaoRad;

uniform vec2    noiseSize = vec2(64.0, 64.0);
uniform vec2    renderTargetSize = vec2(1280.0, 720.0);

uniform sampler2D texNormals;
uniform sampler2D texNoise;
uniform sampler2D texDepth;

const int     ssaoSamples = 16;
const float   ssaoInvSamples = 1.0 / 16.0;

vec3 sphereSamples[16] = vec3[](
    vec3(0.53812504, 0.18565957, -0.43192),vec3(0.13790712, 0.24864247, 0.44301823),vec3(0.33715037, 0.56794053, -0.005789503),
    vec3(-0.6999805, -0.04511441, -0.0019965635),vec3(0.06896307, -0.15983082, -0.85477847),vec3(0.056099437, 0.006954967, -0.1843352),
    vec3(-0.014653638, 0.14027752, 0.0762037),vec3(0.010019933, -0.1924225, -0.034443386),vec3(-0.35775623, -0.5301969, -0.43581226),
    vec3(-0.3169221, 0.106360726, 0.015860917),vec3(0.010350345, -0.58698344, 0.0046293875),vec3(-0.08972908, -0.49408212, 0.3287904),
    vec3(0.7119986, -0.0154690035, -0.09183723),vec3(-0.053382345, 0.059675813, -0.5411899),vec3(0.035267662, -0.063188605, 0.54602677),
    vec3(-0.47761092, 0.2847911, -0.0271716)
    );

void main() {
    float currentDepth = texture2D(texDepth, gl_TexCoord[0].xy).x;
    vec3 screenSpacePosition = vec3(gl_TexCoord[0].x, gl_TexCoord[0].y, currentDepth);

    vec3 screenNormal = normalize(texture2D(texNormals, gl_TexCoord[0].xy).xyz * 2.0 - 1.0);
    vec3 randomNormal = normalize(texture2D(texNoise, renderTargetSize * gl_TexCoord[0].xy / noiseSize).xyz * 2.0 - 1.0);

    float bl = 0.0;
    float radD = ssaoRad / screenSpacePosition.z;

    vec3 ray, se, occNorm;
    float occluderDepth, depthDifference, normDiff, occDepth;

    for (int i=0; i<ssaoSamples;++i) {
        ray = radD*reflect(sphereSamples[i],randomNormal);
        se = screenSpacePosition.xyz + sign(dot(ray,screenNormal))*ray;

        occNorm = normalize(texture2D(texNormals,se.xy).xyz * 2.0 - 1.0);

        occDepth = texture2D(texDepth, se.xy).x;
        depthDifference = currentDepth-occDepth;

        normDiff = (1.0-dot(occNorm,screenNormal));

        bl += step(ssaoFalloff, depthDifference) * normDiff * (1.0 - smoothstep(ssaoFalloff, ssaoStrength, depthDifference));
    }

    float ao = 1.0-ssaoTotalStrength*bl*ssaoInvSamples;
    gl_FragData[0].rgba = vec4(ao);
}
