#version 330 core
// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

uniform sampler2D texDebug;

uniform int debugRenderingStage;

uniform mat4 invProjMatrix;


layout(location = 0) out vec4 outColor;

void main(){
    vec4 texColor;

    if (debugRenderingStage == DEBUG_STAGE_OPAQUE_DEPTH) {

        texColor = texture(texDebug, gl_TexCoord[0].xy);
        float linDepth = linDepth(texColor.x);
        outColor.xyz = vec3(linDepth);
        outColor.a = 1.0;

    } else if (debugRenderingStage == DEBUG_STAGE_OPAQUE_COLOR) {

        texColor = texture(texDebug, gl_TexCoord[0].xy);
        outColor.xyz = texColor.xyz;
        outColor.a = 1.0;

    } else if (debugRenderingStage == DEBUG_STAGE_OPAQUE_NORMALS) {

        texColor = texture(texDebug, gl_TexCoord[0].xy);
        outColor.xyz = texColor.xyz * 2.0 - 1.0;
        outColor.a = 1.0;

    } else if (debugRenderingStage == DEBUG_STAGE_OPAQUE_LIGHT_BUFFER) {

        texColor = texture(texDebug, gl_TexCoord[0].xy);
        outColor.rgb = texColor.rgb;
        outColor.rgb += texColor.aaa;
        outColor.a = 1.0;

    } else if (debugRenderingStage == DEBUG_STAGE_OPAQUE_SUNLIGHT) {

        texColor = texture(texDebug, gl_TexCoord[0].xy);
        outColor.rgb = texColor.aaa;
        outColor.a = 1.0;

    } else if (debugRenderingStage == DEBUG_STAGE_BAKED_OCCLUSION) {

        texColor = texture(texDebug, gl_TexCoord[0].xy);
        color.rgb = texColor.aaa;
        color.a = 1.0;

    } else if (debugRenderingStage == DEBUG_STAGE_RECONSTRUCTED_POSITION) {

        float depth = texture(texDebug, gl_TexCoord[0].xy).r;
        vec3 viewSpacePos = reconstructViewPos(depth, gl_TexCoord[0].xy, invProjMatrix);

        outColor.rgb = viewSpacePos.rgb;
        outColor.a = 1.0;

    } else {
        texColor = texture(texDebug, gl_TexCoord[0].xy);
        outColor = texColor;
    }
}
