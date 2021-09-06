#version 330 core

varying vec4 vertexColor;
varying vec2 textureCoord;

uniform sampler2D texImage;

void main() {
    vec4 textureColor = texture(texImage, textureCoord);
    gl_FragColor = vertexColor * textureColor;
}
