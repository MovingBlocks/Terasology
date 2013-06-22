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

uniform sampler2D texSceneOpaque;
uniform sampler2D texSceneOpaqueDepth;
uniform sampler2D texSceneOpaqueNormals;
uniform sampler2D texSceneOpaqueLightBuffer;

void main() {
    vec4 colorOpaque = texture2D(texSceneOpaque, gl_TexCoord[0].xy);
    float depthOpaque = texture2D(texSceneOpaqueDepth, gl_TexCoord[0].xy).r;
    vec4 normalsOpaque = texture2D(texSceneOpaqueNormals, gl_TexCoord[0].xy);
    vec4 lightBufferOpaque = texture2D(texSceneOpaqueLightBuffer, gl_TexCoord[0].xy);

    // Gamma to linear
    //colorOpaque.rgb = colorOpaque.rgb*colorOpaque.rgb;

    // Diffuse
    colorOpaque.rgb *= lightBufferOpaque.rgb * colorOpaque.a; // colorOpaque.a contains occlusion
    // Specular
    colorOpaque.rgb += lightBufferOpaque.aaa;

    // Linear to gamma
    //colorOpaque.rgb = sqrt(colorOpaque.rgb);

    gl_FragData[0].rgba = colorOpaque.rgba;
    gl_FragData[1].rgba = normalsOpaque.rgba;
    gl_FragData[2].rgba = lightBufferOpaque.rgba;
    gl_FragDepth = depthOpaque;
}
