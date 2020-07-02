#version 120

attribute vec2 position;
attribute vec4 color;
attribute vec2 texcoord;

varying vec4 vertexColor;
varying vec2 textureCoord;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

void main() {
	vertexColor = color;
	textureCoord = texcoord;
	mat4 mvp = projection * view * model;
	gl_Position = mvp * vec4(position, 0.0, 1.0);
}
