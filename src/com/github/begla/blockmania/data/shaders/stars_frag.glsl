#version 120

uniform samplerCube tex;
varying vec3 texCoord;

void main()
{
    vec4 texel = textureCube(tex, texCoord);
    gl_FragColor = texel;
}