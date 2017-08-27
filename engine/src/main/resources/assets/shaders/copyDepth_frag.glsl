
uniform sampler2D texSceneOpaque;
uniform sampler2D texSceneOpaqueDepth;

void main() {

    vec4 colorOpaque = texture2D(texSceneOpaque, gl_TexCoord[0].xy);
    //is this normalizing between -1 to 1? done everywhere
    float depthOpaque = texture2D(texSceneOpaqueDepth, gl_TexCoord[0].xy).r * 2.0 - 1.0;

    //normalizing to (0,1) back again
    float depth = depthOpaque*0.5 + 0.5;

    //linearizing with zNear, zFar
    float linDepth = (2.0 * zNear) / (zFar + zNear - depth * (zFar - zNear));

    //setting depth value to color texture
    //this is where I tried making it "bright red" and it does display
    gl_FragData[0].rgba = vec4(linDepth,linDepth,linDepth,1.0f);

    //setting depth is important too, else water goes invisible again
    //Hypothesis: disabling depth test would help
    gl_FragDepth = depthOpaque*0.5+0.5;
}
