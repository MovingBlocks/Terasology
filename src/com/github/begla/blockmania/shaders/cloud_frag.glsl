#version 120

uniform float daylight = 1.0;

void main(){
    vec4 color = gl_Color;

    color.xyz *= daylight;
    gl_FragColor = color;
}
