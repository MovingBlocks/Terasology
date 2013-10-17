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

#define UNCHARTED_2_TONEMAP
// #define REINHARD_TONEMAP
// #define BURGESS_TONEMAP

uniform sampler2D texScene;
uniform float exposure = 1.0;
uniform float whitePoint = W;

void main(){
    vec4 color = srgbToLinear(texture2D(texScene, gl_TexCoord[0].xy));

#ifdef REINHARD_TONEMAP
    float t = tonemapReinhard(2.5, exposure);
    color *= t;
#endif

#ifdef UNCHARTED_2_TONEMAP
    color.rgb *= exposure;
    vec3 adjColor = uncharted2Tonemap(color.rgb);
    vec3 whiteScale = 1.0/uncharted2Tonemap(vec3(whitePoint));
    vec3 finalColor = adjColor*whiteScale;
    color.rgb = finalColor;
#endif

#ifdef BURGESS_TONEMAP
    color.rgb *= exposure;
    vec3 x = max(vec3(0.0),color.rgb-vec3(0.004));
    vec3 finalColor = (x*(6.2*x+.5))/(x*(6.2*x+1.7)+0.06);
    color.rgb = finalColor;
#endif

    gl_FragData[0].rgba = linearToSrgb(color);
}
