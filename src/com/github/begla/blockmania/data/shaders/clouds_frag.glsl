uniform sampler2D texture;

uniform float light = 1.0;

vec4 srgbToLinear(vec4 color){
    return pow(color, vec4(1.0 / GAMMA));
}

vec4 linearToSrgb(vec4 color){
    return pow(color, vec4(GAMMA));
}

void main(){
    vec4 color = srgbToLinear(texture2D(texture, vec2(gl_TexCoord[0].x , gl_TexCoord[0].y)));

    color.a = color.r;
    color.a *= clamp(1.0 - (length(gl_TexCoord[0].xy - 0.5) / 0.4), 0.0, 1.0);

    color.rgb *= light;

    gl_FragColor = linearToSrgb(color);
}
