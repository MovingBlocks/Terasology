#version 120

varying float fog;
uniform float daylight = 1.0;

void main(){
    vec4 color = gl_Color;

    color.xyz *= daylight;
    gl_FragColor.rgb = mix(color, vec4(0.9,0.9,1.0,1.0) * daylight, fog).rgb;
    gl_FragColor.a = color.a;
}
