varying	vec3 	colorYxy;
varying vec3  skyVec;
varying	vec4 	McPosition;
varying	float lv;
uniform samplerCube tex;

vec4 	eyePos = vec4(0.0, 0.0, 0.0, 1.0);

uniform	vec4 	sunPos;
float	colorExp = 25.0;

vec3	convertColor ()
{
	vec3	clrYxy = vec3 ( colorYxy );

	clrYxy [0] = 1.0 - exp ( -clrYxy [0] / colorExp );

	float	ratio    = clrYxy [0] / clrYxy [2];	

	vec3	XYZ;

	XYZ.x = clrYxy [1] * ratio;						// X = x * ratio
	XYZ.y = clrYxy [0];								// Y = Y
	XYZ.z = ratio - XYZ.x - XYZ.y;					// Z = ratio - X - Y

	const	vec3	rCoeffs = vec3 ( 3.240479, -1.53715, -0.49853  );
	const	vec3	gCoeffs = vec3 ( -0.969256, 1.875991, 0.041556 );
	const	vec3	bCoeffs = vec3 ( 0.055684, -0.204043, 1.057311 );

	return	vec3 ( dot ( rCoeffs, XYZ ), dot ( gCoeffs, XYZ ), dot ( bCoeffs, XYZ ));
}

void main ()
{
       vec3 v               = normalize ( McPosition.xyz );
       
       if(v.y>-0.35){
        vec3 l                  = normalize ( sunPos.xyz );
        vec3 ls                 = normalize ( vec3 (sunPos.x, sunPos.y-0.3, sunPos.z-0.3 ));
        float sunHighlight      = 0.8*pow(max(0.0, dot(ls, v)), 50.0);
        float largeSunHighlight = 0.3*pow(max(0.0, dot(ls, v)), 25.0);
        vec4	skyColor          = vec4	( clamp ( convertColor (), 0.0, 1.0 ) + sunHighlight + largeSunHighlight, 1.0 );
        float	alpha             = 0.2 * (1.0 - lv);
        skyColor               += alpha*textureCube ( tex, skyVec );
        gl_FragColor            = skyColor;
       }else{
        gl_FragColor = vec4	( 0.0, 0.0, 0.0, 1.0);
       }
}
