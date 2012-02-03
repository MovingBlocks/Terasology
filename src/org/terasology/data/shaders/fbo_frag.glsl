uniform sampler2D texScene;
uniform sampler2D texBloom;

uniform float exposure = 1.0;
const float brightMax = 2.0;

void main(){
    vec4 color = texture2D(texScene, gl_TexCoord[0].xy);

    // HDR Tone Mapping
    float Y = dot(vec4(0.30, 0.59, 0.11, 0.0), color);
    float YD = exposure * (exposure/brightMax + 1.0) / (exposure + 1.0);

    // Apply bloom
    color += texture2D(texBloom, gl_TexCoord[0].xy) * 1.0;
    color *= YD;

    gl_FragColor = color;
}
