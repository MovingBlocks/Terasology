#version 120

uniform sampler2D texture;

void main(){
    vec4 color = texture2D(texture, vec2(gl_TexCoord[0].x , gl_TexCoord[0].y));
    color.a = color.r;
    color.a *= clamp(1.0 - (length(gl_TexCoord[0].xy - 0.5) / 0.4), 0, 1);

    gl_FragColor = color;
}
