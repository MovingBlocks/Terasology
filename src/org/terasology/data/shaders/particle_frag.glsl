uniform sampler2D textureAtlas;

uniform float light = 1.0;
uniform float texOffsetX = 0.0;
uniform float texOffsetY = 0.0;
uniform vec3 colorOffset = vec3(1.0, 1.0, 1.0);

void main(){
    vec4 color = srgbToLinear(texture2D(textureAtlas, vec2(gl_TexCoord[0].x + texOffsetX , gl_TexCoord[0].y + texOffsetY )));

    color.rgb *= colorOffset.rgb;
    color.rgb *= pow(0.86, (1.0-light)*15.0);

    gl_FragColor = linearToSrgb(color);
}
