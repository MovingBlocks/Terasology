uniform sampler2D texScene;

#ifdef VIGNETTE
uniform sampler2D texVignette;
uniform vec3 inLiquidTint;
uniform vec3 tint = vec3(1.0,1.0,1.0);
#endif

void main(){
    vec4 color = texture2D(texScene, gl_TexCoord[0].xy);

    #ifdef VIGNETTE
        float vig = texture2D(texVignette, gl_TexCoord[0].xy).x;
        if (!swimming) {
            color.rgb *= vec3(vig)+(1-vig)*tint.rgb;
        } else {
            color.rgb *= vig * vig * vig;
            color.rgb *= inLiquidTint;
        }
    #endif
    gl_FragData[0].rgba = color.rgba;
}
