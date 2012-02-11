uniform sampler2D texture;
uniform vec4 colorOffset;
uniform bool carryingTorch;

varying vec3 normal;
varying vec4 vertexWorldPos;

uniform float light = 1.0;

void main(){
    vec4 color = srgbToLinear(texture2D(texture, vec2(gl_TexCoord[0].x , gl_TexCoord[0].y)));

    float torchlight = 0.0;

    if (carryingTorch)
        torchlight = torchlight(lambLight(normal, vertexWorldPos), vertexWorldPos);

    color.rgb *= clamp(gl_Color.rgb, 0.0, 1.0) * colorOffset.rgb;
    color.rgb *= expLightValue(light) + torchlight;
    color.a = gl_Color.a;

    gl_FragColor = linearToSrgb(color);
}
