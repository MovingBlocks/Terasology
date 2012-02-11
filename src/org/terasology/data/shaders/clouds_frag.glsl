uniform sampler2D texture;

uniform float light = 1.0;

void main(){
    vec4 color = srgbToLinear(texture2D(texture, vec2(gl_TexCoord[0].x , gl_TexCoord[0].y)));

    color.a = color.r;

    // Fade the clouds at the horizon
    color.a *= clamp(1.0 - (length(gl_TexCoord[0].xy - 0.5) / 0.4), 0.0, 1.0);

    // Fade out the clouds at night
    if (light < 0.25)
        color.a *= 1.0 - clamp(abs(0.25 - light) / 0.25, 0.0, 1.0);

    gl_FragColor = linearToSrgb(color);
}
