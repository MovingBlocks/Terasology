uniform sampler2D textureAtlas;

uniform float light = 1.0;
uniform float texOffsetX = 0.0;
uniform float texOffsetY = 0.0;

uniform vec3 colorOffset = vec3(1.0, 1.0, 1.0);

uniform bool carryingTorch = false;

varying vec4 vertexWorldPos;

void main(){
    vec4 color = srgbToLinear(texture2D(textureAtlas, vec2(gl_TexCoord[0].x + texOffsetX , gl_TexCoord[0].y + texOffsetY )));

    float torchlight = 0.0;

    // Apply torchlight
    if (carryingTorch)
        torchlight = calcTorchlight(1.0, vertexWorldPos.xyz);

    color.rgb *= colorOffset.rgb;

    float lightValue = expLightValue(light);
    color.rgb *= clamp(lightValue + torchlight, 0.0, 1.0);

    gl_FragColor = linearToSrgb(color);
}
