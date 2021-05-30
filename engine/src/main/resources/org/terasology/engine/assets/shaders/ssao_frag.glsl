#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

in vec2 v_uv0;

uniform vec4 ssaoSettings;
#define ssaoStrength ssaoSettings.x
#define ssaoRadius ssaoSettings.y

uniform vec2 texelSize;
uniform vec2 noiseTexelSize;

uniform sampler2D texNormals;
uniform sampler2D texNoise;
uniform sampler2D texDepth;

uniform mat4 invProjMatrix;
uniform mat4 projMatrix;

uniform vec3 ssaoSamples[SSAO_KERNEL_ELEMENTS];

void main() {
    float currentDepth = texture2D(texDepth, v_uv0.xy).x * 2.0 - 1.0;

    // Exclude the sky...
    if (epsilonEqualsOne(currentDepth)) {
        gl_FragData[0].rgba = vec4(1.0);
        return;
    }

    vec3 normal = texture2D(texNormals, v_uv0.xy).xyz * 2.0 - 1.0;

    vec2 noiseScale = noiseTexelSize / texelSize;
    vec3 randomVec = texture(texNoise, v_uv0.xy * noiseScale).xyz * 2.0 - 1.0;

    // TODO: This is costly... See below
    vec3 viewSpacePos = reconstructViewPos(currentDepth, v_uv0.xy, invProjMatrix);

    vec3 tangent = normalize(randomVec - normal * dot(randomVec, normal));
    vec3 bitangent = cross(normal, tangent);
    mat3 tbn = mat3(tangent, bitangent, normal);

    float occlusion = 0.0;
    float sampleDepth = 0.0;
    float rangeCheck = 0.0;
    vec3 samplePosition = vec3(0.0);
    vec4 offset = vec4(0.0);
    float samplesTaken = 0.0;
    const float maxDepthDifference = 1;

    for (int i=0; i<SSAO_KERNEL_ELEMENTS; ++i) {
        samplePosition = (tbn * ssaoSamples[i]) * ssaoRadius + viewSpacePos;

        offset = vec4(samplePosition.x, samplePosition.y, samplePosition.z, 1.0);
        offset = projMatrix * offset;
        offset.xy /= offset.w;
        offset.xy = offset.xy * vec2(0.5) + vec2(0.5);

        // TODO: Holy... frustum ray and linearized depth - please!
        sampleDepth = reconstructViewPos(texture2D(texDepth, offset.xy).r * 2.0 - 1.0, v_uv0.xy, invProjMatrix).z;
        float depthDifference = abs(viewSpacePos.z - sampleDepth);

        float rangeCheck;
        if (depthDifference > maxDepthDifference) {
        	rangeCheck = 0;
        } else {
        	rangeCheck = smoothstep(0.0, 1.0, ssaoRadius / depthDifference);
        }

        occlusion += step(samplePosition.z, sampleDepth) * rangeCheck;

        samplesTaken += 1.0;
    }

    occlusion = 1.0 - occlusion / samplesTaken;

    gl_FragData[0].rgba = vec4(pow(occlusion, ssaoStrength));
}
