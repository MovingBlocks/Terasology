uniform sampler2D textureAtlas;

uniform float light;
uniform vec3 colorOffset;

uniform bool textured;

vec4 srgbToLinear(vec4 color){
    return pow(color, vec4(1.0 / GAMMA));
}

vec4 linearToSrgb(vec4 color){
    return pow(color, vec4(GAMMA));
}

void main(){
    vec4 color = vec4(1.0,1.0,1.0,1.0);

    if (textured) {
        color = srgbToLinear(texture2D(textureAtlas, vec2(gl_TexCoord[0].x , gl_TexCoord[0].y)));
    }

    color.rgb *= gl_Color.rgb * clamp(light, 0.0, 1.0);
    color.rgb *= colorOffset.rgb;

    if (textured)
        gl_FragColor = linearToSrgb(color);
    else
        gl_FragColor = color;
}
