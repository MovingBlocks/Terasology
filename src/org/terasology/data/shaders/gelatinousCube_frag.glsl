uniform sampler2D texture;
uniform vec4 colorOffset;

uniform float light = 1.0;

vec4 srgbToLinear(vec4 color){
    return pow(color, vec4(1.0 / GAMMA));
}

vec4 linearToSrgb(vec4 color){
    return pow(color, vec4(GAMMA));
}

void main(){
    vec4 color = srgbToLinear(texture2D(texture, vec2(gl_TexCoord[0].x , gl_TexCoord[0].y)));

    color.rgb *= clamp(gl_Color.rgb, 0.0, 1.0) * colorOffset.rgb;
    color.rgb *= pow(0.86, (1.0-light)*15.0);
    color.a = gl_Color.a;

    gl_FragColor = linearToSrgb(color);
}
