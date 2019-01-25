#ifdef VIGNETTE
uniform sampler2D texVignette;
uniform vec3 inLiquidTint;
#endif

void main(){
	#ifdef VIGNETTE
	    float vig = texture2D(texVignette, gl_TexCoord[0].xy).x;

	    if (!swimming) {
	        color.rgb *= vig;
	    } else {
	        color.rgb *= vig * vig * vig;
	        color.rgb *= inLiquidTint;
	    }
	#endif

	gl_FragData[0].rgba = color.rgba;
}