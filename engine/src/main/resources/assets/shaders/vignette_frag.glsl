uniform sampler2D texScene;

uniform sampler2D texVignette;
uniform vec3 inLiquidTint;

void main(){
	vec4 color = texture2D(texScene, gl_TexCoord[0].xy);

	    float vig = texture2D(texVignette, gl_TexCoord[0].xy).x;

	    if (!swimming) {
	        color.rgb *= vig;
	    } else {
	        color.rgb *= vig * vig * vig;
	        color.rgb *= inLiquidTint;
	    }
	    gl_FragData[0].rgba = color.rgba;

}