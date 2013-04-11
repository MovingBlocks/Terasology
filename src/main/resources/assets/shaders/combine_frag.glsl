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
uniform sampler2D texSceneTransparent;
uniform sampler2D texSceneTransparentDepth;
uniform sampler2D texSceneTransparentNormals;
#ifdef SSAO
uniform sampler2D texSsao;
#endif
#ifdef OUTLINE
uniform sampler2D texEdges;

uniform float outlineDepthThreshold;
uniform float outlineThickness;
#endif

uniform float shoreStart;
uniform float shoreEnd;

#define OUTLINE_COLOR 0.0, 0.0, 0.0

void main() {
    vec4 colorOpaque = texture2D(texSceneOpaque, gl_TexCoord[0].xy);
    float depthOpaque = texture2D(texSceneOpaqueDepth, gl_TexCoord[0].xy).r;
    vec4 normalsOpaque = texture2D(texSceneOpaqueNormals, gl_TexCoord[0].xy);
    vec4 colorTransparent = texture2D(texSceneTransparent, gl_TexCoord[0].xy);
    float depthTransparent = texture2D(texSceneTransparentDepth, gl_TexCoord[0].xy).r;
    vec4 normalsTransparent = texture2D(texSceneTransparentNormals, gl_TexCoord[0].xy);

#ifdef SSAO
    float ssao = texture2D(texSsao, gl_TexCoord[0].xy).x;
    colorOpaque *= vec4(ssao, ssao, ssao, 1.0);
#endif

#ifdef OUTLINE
    float outline = step(outlineDepthThreshold, texture2D(texEdges, gl_TexCoord[0].xy).x) * outlineThickness;
    colorOpaque.rgb = (1.0 - outline) * colorOpaque.rgb + outline * vec3(OUTLINE_COLOR);
#endif

    vec4 color = vec4(0.0);
    float depth = depthOpaque;
    vec4 normals = vec4(0.0);

    // Combine transparent and opaque RTs
    if (depthTransparent < depthOpaque) {
        depth = depthTransparent;

        // TODO: Fix alpha blending...
        //float fade = 1.0 - colorTransparent.a;
        float fade = 0.0;
        // Detect water in the transparent RT...
        if (normalsTransparent.a > 0.99) {
            // ... and fade out at the shore
            float linDepthOpaque = linDepth(depthOpaque);
            float linDepthTransparent = linDepth(depthTransparent);

            float depthDiff = linDepthOpaque - linDepthTransparent;
            fade = clamp((shoreEnd - depthDiff) / (shoreEnd - shoreStart), 0.0, 1.0);
        }

        normals = mix(normalsTransparent, normalsOpaque, fade);
        color = mix(colorTransparent, colorOpaque, fade);
    } else {
        normals = normalsOpaque;
        color = colorOpaque;
    }

    gl_FragData[0].rgba = color.rgba;
    gl_FragData[1].rgba = normals.rgba;
    gl_FragDepth = depth;
}
