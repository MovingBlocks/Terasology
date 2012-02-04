uniform sampler2D tex;

void main() {
    vec4 color = texture2D(tex, gl_TexCoord[0].xy);
    float lum = 0.2126 * color.r + 0.7152 * color.g + 0.0722 * color.b;

    if (lum > 1.0)
        gl_FragColor = color;
    else
        gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);
}
