/*
 * Copyright 2013 Moving Blocks
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


uniform float alpha;
uniform mat4 posMatrix;

varying vec3 normal;
varying vec2 relPos;

void main()
{
    vec3 pos = (posMatrix * gl_Vertex).xyz;
    relPos = pos.xy;

    normal = normalize(gl_NormalMatrix * gl_Normal);
    gl_Position = ftransform();
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_FrontColor = vec4(gl_Color.rgb, gl_Color.a * alpha);
}
