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

varying vec3 normal;

uniform mat3 normalMatrix;
uniform mat4 worldViewMatrix;
uniform mat4 projectionMatrix;

void main()
{
#if !defined (FEATURE_USE_MATRIX_STACK)
    normal = normalMatrix * gl_Normal;
	gl_Position = (projectionMatrix * worldViewMatrix) * gl_Vertex;
#else
    normal = gl_NormalMatrix * gl_Normal;
    gl_Position = ftransform();
#endif

    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_FrontColor = gl_Color;
}

