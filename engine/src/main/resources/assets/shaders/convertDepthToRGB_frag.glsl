uniform sampler2D texSceneOpaqueDepth;

void main() {
    float depthOpaque = texture2D(texSceneOpaqueDepth, gl_TexCoord[0].xy).r;
    gl_FragData[0] = vec4(depthOpaque,depthOpaque,depthOpaque,1.0);
}
