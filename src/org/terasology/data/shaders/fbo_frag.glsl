uniform sampler2D texScene;
uniform sampler2D texBloom;
uniform sampler2D texDepth;
uniform sampler2D texBlur;

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

    if (depth > 0.1)
       blur = clamp((depth - 0.1) / 0.5, 0.0, 1.0);

    if (depth < 0.01)
        blur = clamp((0.01 - depth) / 0.005, 0.0, 1.0);

    // Display depth map
    //gl_FragColor = vec4(linDepth());

    gl_FragColor = mix(color, colorBlur, blur);
}
