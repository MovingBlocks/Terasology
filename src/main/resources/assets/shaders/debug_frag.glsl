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

uniform sampler2D texDebug;

uniform int debugRenderingStage;

uniform mat4 invProjMatrix;

void main(){
    vec4 color;
    vec4 texColor;

    if (debugRenderingStage == DEBUG_STAGE_OPAQUE_DEPTH) {

        texColor = texture2D(texDebug, gl_TexCoord[0].xy);
        float linDepth = linDepth(texColor.x);
        color.xyz = vec3(linDepth);
        color.a = 1.0;

    } else if (debugRenderingStage == DEBUG_STAGE_OPAQUE_COLOR) {

        texColor = texture2D(texDebug, gl_TexCoord[0].xy);
        color.xyz = texColor.xyz;
        color.a = 1.0;

    } else if (debugRenderingStage == DEBUG_STAGE_OPAQUE_NORMALS) {

        texColor = texture2D(texDebug, gl_TexCoord[0].xy);
        color.xyz = texColor.xyz * 2.0 - 1.0;
        color.a = 1.0;

    } else if (debugRenderingStage == DEBUG_STAGE_OPAQUE_LIGHT_BUFFER) {

        texColor = texture2D(texDebug, gl_TexCoord[0].xy);
        color.rgb = texColor.rgb;
        color.rgb += texColor.aaa;
        color.a = 1.0;

    } else if (debugRenderingStage == DEBUG_STAGE_OPAQUE_SUNLIGHT) {

        texColor = texture2D(texDebug, gl_TexCoord[0].xy);
        color.rgb = texColor.aaa;
        color.a = 1.0;

    } else if (debugRenderingStage == DEBUG_STAGE_BAKED_OCCLUSION) {

        texColor = texture2D(texDebug, gl_TexCoord[0].xy);
        color.rgb = texColor.aaa;
        color.a = 1.0;

   }  else if (debugRenderingStage == DEBUG_STAGE_RECONSTRUCTED_POSITION) {

       float depth = texture2D(texDebug, gl_TexCoord[0].xy).r;
       vec3 viewSpacePos = reconstructViewPos(depth, gl_TexCoord[0].xy, invProjMatrix);

       color.rgb = viewSpacePos.rgb;
       color.a = 1.0;

   }  else {

       texColor = texture2D(texDebug, gl_TexCoord[0].xy);
       color = texColor;

   }

   gl_FragData[0] = color;
}
