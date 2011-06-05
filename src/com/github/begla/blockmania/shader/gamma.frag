varying vec2 tex_coord; 
uniform sampler2D tex;
varying vec4 vertColor;

vec4 gamma(vec4 color){
    return pow(color, vec4(1.0/1.4));
}

void main(){
    gl_FragColor = gamma(gl_Color*texture2D(tex, tex_coord));
}
