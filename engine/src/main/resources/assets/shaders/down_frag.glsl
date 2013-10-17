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

uniform sampler2D tex;
uniform float size;

const vec2 s1 = vec2(-1, 1);
const vec2 s2 = vec2( 1, 1);
const vec2 s3 = vec2( 1,-1);
const vec2 s4 = vec2(-1,-1);

void main() {
    vec2 texCoordSample = vec2(0.0);

	texCoordSample = gl_TexCoord[0].xy + s1 / size;
	vec4 color = texture2D(tex, texCoordSample);

	texCoordSample = gl_TexCoord[0].xy + s2 / size;
	color += texture2D(tex, texCoordSample);

	texCoordSample = gl_TexCoord[0].xy + s3 / size;
	color += texture2D(tex, texCoordSample);

	texCoordSample = gl_TexCoord[0].xy + s4 / size;
	color += texture2D(tex, texCoordSample);

	gl_FragData[0].rgba = color * 0.25;
}
