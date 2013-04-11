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

uniform sampler2D texSceneShadowMap;

uniform sampler2D texSceneOpaqueColor;
uniform sampler2D texSceneOpaqueNormals;
uniform sampler2D texSceneOpaqueDepth;

uniform sampler2D texSceneTransparentColor;
uniform sampler2D texSceneTransparentNormals;
uniform sampler2D texSceneTransparentDepth;

uniform int debugRenderingStage;

void main(){
    vec4 color;

    vec4 texColor;
    
        if (debugRenderingStage == DEBUG_STAGE_OPAQUE_COLOR)
        {
        
            texColor = texture2D(texSceneOpaqueColor, gl_TexCoord[0].xy);
            color = texColor;
        
        } else if (debugRenderingStage == DEBUG_STAGE_OPAQUE_NORMALS) {
        
            texColor = texture2D(texSceneOpaqueNormals, gl_TexCoord[0].xy);
            color.xyz = texColor.xyz;
            color.a = 1.0;
        
        } else if (debugRenderingStage == DEBUG_STAGE_OPAQUE_DEPTH) {
        
            texColor = texture2D(texSceneOpaqueDepth, gl_TexCoord[0].xy);
            float linDepth = linDepth(texColor.x);
            color.xyz = vec3(linDepth);
            color.a = 1.0;
        
        } else if (debugRenderingStage == DEBUG_STAGE_OPAQUE_NORMALS_ALPHA) {
        
            texColor = texture2D(texSceneOpaqueNormals, gl_TexCoord[0].xy);
            color.xyz = vec3(texColor.a);
            color.a = 1.0;
        
        } else if (debugRenderingStage == DEBUG_STAGE_TRANSPARENT_COLOR) {
        
            texColor = texture2D(texSceneTransparentColor, gl_TexCoord[0].xy);
            color = texColor;
        
        } else if (debugRenderingStage ==  DEBUG_STAGE_TRANSPARENT_NORMALS) {
        
            texColor = texture2D(texSceneTransparentNormals, gl_TexCoord[0].xy);
            color.xyz = texColor.xyz;
            color.a = 1.0;
        
        } else if (debugRenderingStage == DEBUG_STAGE_TRANSPARENT_DEPTH) {
        
            texColor = texture2D(texSceneTransparentDepth, gl_TexCoord[0].xy);
            float linDepth = linDepth(texColor.x);
            color.xyz = vec3(linDepth);
            color.a = 1.0;
        
        } else if (debugRenderingStage == DEBUG_STAGE_TRANSPARENT_NORMALS_ALPHA) {
        
            texColor = texture2D(texSceneTransparentNormals, gl_TexCoord[0].xy);
            color.xyz = vec3(texColor.a);
            color.a = 1.0;
        
        } else if (debugRenderingStage == DEBUG_STAGE_SHADOW_MAP) {
        
            texColor = texture2D(texSceneShadowMap, gl_TexCoord[0].xy);
            color = texColor;
        
        }

    gl_FragData[0] = color;
}
