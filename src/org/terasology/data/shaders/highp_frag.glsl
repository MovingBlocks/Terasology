uniform sampler2D tex;

void main() {
    vec4 color = texture2D(tex, gl_TexCoord[0].xy);
    float lum = 0.2126 * color.r + 0.7152 * color.g + 0.0722 * color.b;

    gl_FragColor = color * (lum - 0.25);
}
