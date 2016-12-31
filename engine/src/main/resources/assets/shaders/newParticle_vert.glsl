/*
 * Copyright 2015 Benjamin Glatzel <benjamin.glatzel@me.com>
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

uniform vec3 position;
uniform vec3 scale;
uniform vec4 color;


mat4 undoRotation(in mat4 matrix) {
	mat4 noRotation = mat4(matrix);

	for(int row = 0; row < 3; row++) {
		for(int column = 0; column < 3; column++) {
			noRotation[row][column] = (row == column) ? 1 : 0;
		}
	}

	return noRotation;
}

mat4 objectTranslationMatrix() {
    return mat4(
        vec4(1.0,  0.0, 0.0, 0.0),
        vec4(0.0,  1.0, 0.0, 0.0),
        vec4(0.0,  0.0, 1.0, 0.0),
        vec4(position,       1.0)
    );
}

mat4 objectScaleMatrix() {
    return mat4(
        vec4(scale.x, 0.0,     0.0,     0.0),
        vec4(0.0,     scale.y, 0.0,     0.0),
        vec4(0.0,     0.0,     scale.z, 0.0),
        vec4(0.0,     0.0,     0.0,     1.0)
    );
}

mat4 objectRotationMatrix() {
    mat4 rotation = transpose(gl_ModelViewMatrix);
    rotation[0][3] = rotation[1][3] = rotation[2][3] = 0.0;
    rotation[3] = vec4(0.0, 0.0, 0.0, 1.0);
    return rotation;
}

mat4 objectTransformMatrix() {
    return objectTranslationMatrix() * objectRotationMatrix() * objectScaleMatrix();
}


vec4 applyScale(in vec4 vertex) {
    return vec4(
        vertex.x * scale.x,
        vertex.y * scale.y,
        vertex.z * scale.z,
        vertex.w
    );
}

void main()
{
    gl_Position = objectTransformMatrix() * gl_Vertex;
    gl_Position = gl_ProjectionMatrix * gl_ModelViewMatrix * gl_Position;

    gl_FrontColor = color;
    gl_TexCoord[0] = gl_MultiTexCoord0;
}
