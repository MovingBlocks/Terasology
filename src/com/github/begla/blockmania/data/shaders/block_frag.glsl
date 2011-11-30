uniform sampler2D textureAtlas;

uniform float light = 1.0;

vec4 srgbToLinear(vec4 color){
    return pow(color, vec4(1.0 / GAMMA));
}

vec4 linearToSrgb(vec4 color){
    return pow(color, vec4(GAMMA));
}

void main(){
    vec4 color = srgbToLinear(texture2D(textureAtlas, vec2(gl_TexCoord[0].x , gl_TexCoord[0].y)));
    color.rgb *= clamp(gl_Color.rgb*1.5, 0.0, 1.0);
    color.rgb *= pow(0.86, (1.0-light)*15.0);
    gl_FragColor = linearToSrgb(color);
}
