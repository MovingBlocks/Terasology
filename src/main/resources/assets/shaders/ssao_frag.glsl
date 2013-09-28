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

uniform vec4 ssaoSettings;
#define ssaoStrength ssaoSettings.x
#define ssaoRad ssaoSettings.y

uniform vec2 texelSize;
uniform vec2 noiseTexelSize;

uniform sampler2D texNormals;
uniform sampler2D texNoise;
uniform sampler2D texDepth;

uniform mat4 invProjMatrix;
uniform mat4 projMatrix;

uniform vec3 ssaoSamples[SSAO_KERNEL_ELEMENTS];

void main() {
    float currentDepth = texture2D(texDepth, gl_TexCoord[0].xy).x * 2.0 - 1.0;

    // Exclude the sky...
    if (epsilonEqualsOne(currentDepth)) {
        gl_FragData[0].rgba = vec4(1.0);
        return;
    }

    vec3 normal = texture2D(texNormals, gl_TexCoord[0].xy).xyz * 2.0 - 1.0;

    vec2 noiseScale = noiseTexelSize / texelSize;
    vec3 randomVec = texture2D(texNoise, gl_TexCoord[0].xy * noiseScale).xyz * 2.0 - 1.0;

    // TODO: This is costly... See below
    vec3 viewSpacePos = reconstructViewPos(currentDepth, gl_TexCoord[0].xy, invProjMatrix);

    vec3 tangent = normalize(randomVec - normal * dot(randomVec, normal));
    vec3 bitangent = cross(normal, tangent);
    mat3 tbn = mat3(tangent, bitangent, normal);

    float occlusion = 0.0;
    float sampleDepth = 0.0;
    float rangeCheck = 0.0;
    vec3 samplePosition = vec3(0.0, 0.0, 0.0);
    float samplesTaken = 0.0;

    for (int i=0; i<SSAO_KERNEL_ELEMENTS; ++i) {
        samplePosition = (tbn * ssaoSamples[i]) * ssaoRad + viewSpacePos;

        vec4 offset = vec4(samplePosition.x, samplePosition.y, samplePosition.z, 1.0);
        offset = projMatrix * offset;
        offset.xy /= offset.w;
        offset.xy = offset.xy * vec2(0.5) + vec2(0.5);

        // TODO: Holy... frustum ray and linearized depth - please!
        sampleDepth = reconstructViewPos(texture2D(texDepth, offset.xy).r * 2.0 - 1.0, gl_TexCoord[0].xy, invProjMatrix).z;

        rangeCheck = smoothstep(0.0, 1.0, ssaoRad / abs(viewSpacePos.z - sampleDepth));
        occlusion += step(samplePosition.z, sampleDepth) * rangeCheck;

        samplesTaken += 1.0;
    }

    occlusion = 1.0 - occlusion / samplesTaken;

    gl_FragData[0].rgba = vec4(pow(occlusion, ssaoStrength));
}
