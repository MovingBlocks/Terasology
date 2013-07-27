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

// TODO: define could be removed
#define CHROMATIC_ABERRATION

#ifdef BLOOM
uniform float bloomFactor;

uniform sampler2D texBloom;
#endif

#if defined (CHROMATIC_ABERRATION)
uniform vec2 aberrationOffset = vec2(0.0, 0.0);
#endif

uniform sampler2D texScene;

#ifdef VIGNETTE
uniform sampler2D texVignette;
uniform vec3 inLiquidTint;
#endif

#ifdef LIGHT_SHAFTS
uniform sampler2D texLightShafts;
#endif

void main() {

#if !defined (CHROMATIC_ABERRATION)
    vec4 color = texture2D(texScene, gl_TexCoord[0].xy);
#else
    float r = texture2D(texScene, gl_TexCoord[0].xy - aberrationOffset).r;
    vec2 ga = texture2D(texScene, gl_TexCoord[0].xy).ga;
    float b = texture2D(texScene, gl_TexCoord[0].xy - aberrationOffset).b;

    vec4 color = vec4(r, ga.x, b, ga.y);
#endif

#ifdef LIGHT_SHAFTS
    vec4 colorShafts = texture2D(texLightShafts, gl_TexCoord[0].xy);
    color.rgb += colorShafts.rgb;
#endif

#ifdef BLOOM
    vec4 colorBloom = texture2D(texBloom, gl_TexCoord[0].xy);
    color += colorBloom * bloomFactor;
#endif

#ifdef VIGNETTE
    float vig = texture2D(texVignette, gl_TexCoord[0].xy).x;

    if (!swimming) {
        color.rgb *= vig;
    } else {
        color.rgb *= vig * vig * vig;
        color.rgb *= inLiquidTint;
    }
#endif

    gl_FragData[0].rgba = color.rgba;
}
