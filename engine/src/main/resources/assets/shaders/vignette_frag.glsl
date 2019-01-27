#ifdef VIGNETTE
uniform sampler2D texScene;

uniform sampler2D texVignette;
uniform vec3 inLiquidTint;
uniform vec3 tint = vec3(0.937,0.126,0.016);
uniform bool isTint = true;

void main(){
	vec4 color = texture2D(texScene, gl_TexCoord[0].xy);
    float vig = texture2D(texVignette, gl_TexCoord[0].xy).x;    
    if (!swimming) {
		if(isTint){
			color.rgb *=  vec3(vig)+vec3(1-vig)*tint.rgb;      
        }
        else{
        	color.rgb *= vig;
    	}
    } else {
        color.rgb *= vig * vig * vig;
        color.rgb *= inLiquidTint;
    }
    gl_FragData[0].rgba = color.rgba;
}
#endif