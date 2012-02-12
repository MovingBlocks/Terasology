uniform sampler2D textureAtlas;

uniform float light;
uniform vec3 colorOffset;
uniform bool textured;
uniform bool carryingTorch;

varying vec3 normal;
varying vec4 vertexWorldPos;

void main(){
    vec4 color;

    if (textured) {
        color = srgbToLinear(texture2D(textureAtlas, vec2(gl_TexCoord[0].x , gl_TexCoord[0].y)));
    } else {
        color = gl_Color;
    }

    float torchlight = 0.0;
    float highlight = calcLambLight(normal, -normalize(vertexWorldPos.xyz));

    // Apply torchlight
    if (carryingTorch)
        torchlight = calcTorchlight(highlight, vertexWorldPos.xyz);

    // Apply light
    float lightValue = expLightValue(light);
    color.rgb *= clamp(lightValue * 0.85 + highlight * 0.15 * lightValue + torchlight, 0.0, 1.0);

    if (textured) {
        color.rgb *= colorOffset.rgb;
        gl_FragColor = linearToSrgb(color);
    } else {
        gl_FragColor = color;
    }
}
