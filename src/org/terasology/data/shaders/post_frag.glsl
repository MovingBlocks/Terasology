uniform sampler2D texScene;
uniform sampler2D texBloom;
uniform sampler2D texDepth;
uniform sampler2D texBlur;

uniform bool swimming;
uniform float exposure = 1.0;
const float brightMax = 1.0;

float linDepth() {
    float cNear = 0.1;
    float cZFar = 512.0;
    float z = texture2D(texDepth, gl_TexCoord[0].xy).x;

    return (2.0 * cNear) / (cZFar + cNear - z * (cZFar - cNear));
}

void main(){
    vec4 color = texture2D(texScene, gl_TexCoord[0].xy);
    vec4 colorBlur = texture2D(texBlur, gl_TexCoord[0].xy);

    // HDR Tone Mapping
    float Y = dot(vec4(0.30, 0.59, 0.11, 0.0), color);
    float YD = exposure * (exposure/brightMax + 1.0) / (exposure + 1.0);

    // Apply bloom
    color += texture2D(texBloom, gl_TexCoord[0].xy) * 1.0;
    colorBlur += texture2D(texBloom, gl_TexCoord[0].xy) * 1.0;

    colorBlur *= YD;
    color *= YD;

    float depth = linDepth();
    float blur = 0.0;

    if (depth > 0.1 && !swimming)
       blur = clamp((depth - 0.1) / 0.1, 0.0, 1.0);
    else if (swimming)
       blur = 1.0;

    vec4 finalColor = mix(color, colorBlur, blur);

    vec2 vignette = vec2(0.25, 0.25);

    float d = distance(gl_TexCoord[0].xy, vec2(0.5,0.5));

    // Vignette
    if (swimming)
        finalColor.rgb *= (1.0 - d) / 4.0;
    else
        finalColor.rgb *= (1.0 - d);

    gl_FragColor = finalColor;
}
