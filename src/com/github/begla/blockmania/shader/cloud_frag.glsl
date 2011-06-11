#version 120

uniform float daylight = 1.0;

vec4 gamma(vec4 color){
    return pow(color, vec4(1.0/1.2));
}

void main(){
    vec4 color = gl_Color;
    color.xyz *= daylight;
    gl_FragColor = gamma(color);
}
