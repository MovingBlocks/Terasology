#version 120

varying vec4 vertexColor;
varying vec2 textureCoord;

uniform sampler2D texImage;

void main() {
    vec4 textureColor = texture2D(texImage, textureCoord);
    gl_FragColor = vertexColor * textureColor;
}
