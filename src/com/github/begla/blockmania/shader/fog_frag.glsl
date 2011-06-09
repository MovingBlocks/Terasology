uniform sampler2D tex;
varying vec4 vertColor;
varying float fog;

vec4 gamma(vec4 color){
    return pow(color, vec4(1.0/1.2));
}

void main(){
    vec4 color = gl_Color*texture2D(tex, vec2(gl_TexCoord[0]));
    gl_FragColor = gamma(mix(color, gl_Fog.color, fog/2.0));
    gl_FragColor.w = color.w;
}
