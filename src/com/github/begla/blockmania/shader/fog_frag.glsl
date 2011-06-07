uniform sampler2D tex;
varying vec4 vertColor;
varying float fog;

vec4 gamma(vec4 color){
    return pow(color, vec4(1.0/1.2));
}

void main(){
    vec4 color = gl_Color*texture2D(tex, vec2(gl_TexCoord[0]));
    vec4 fogColor = pow(gl_Fog.color, gl_Fog.color*8.0);
    gl_FragColor = gamma(mix(color, fogColor, fog));
    gl_FragColor.w = color.w;
}
