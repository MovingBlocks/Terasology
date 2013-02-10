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

uniform float weight;
uniform float decay;
uniform float exposure;
uniform float density;

uniform sampler2D texScene;
uniform sampler2D texNormals;

uniform float lightDirDotViewDir;
uniform vec2 lightScreenPos;

void main() {
    gl_FragData[0].rgba = vec4(0.0, 0.0, 0.0, 1.0);

    if (lightDirDotViewDir > 0.0) {
        vec2 textCoo = gl_TexCoord[0].xy;
        vec2 deltaTextCoord = vec2( textCoo.xy - lightScreenPos.xy);

        deltaTextCoord *= (1.0 / float(LIGHT_SHAFT_SAMPLES)) * density;

        float illuminationDecay = 1.0;
        for(int i=0; i < LIGHT_SHAFT_SAMPLES ; i++)
        {
            textCoo -= deltaTextCoord;

            float mask = texture2D(texNormals, textCoo).w;
            vec3 sample = texture2D(texScene, textCoo).rgb * mask;

            sample *= illuminationDecay * weight;

            gl_FragData[0].rgb += sample;

            illuminationDecay *= decay;
        }

        gl_FragData[0].rgb *= exposure * lightDirDotViewDir;
    }
}
