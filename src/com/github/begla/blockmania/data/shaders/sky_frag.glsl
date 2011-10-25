varying	vec3 	colorYxy;
varying vec3  skyVec;
varying	vec4 	McPosition;
varying	float lv;
uniform float time;
uniform samplerCube tex;
uniform	sampler3D	  noiseMap;

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

vec3	clouds ( const vec3 tex, float t, const vec2 vel )
{
  vec3 	dt   = vec3 ( vel, 0.061 ) * t;
  vec3 	tex1 = vec3 ( tex.xy, 0.234753 ) + dt; 
	vec3	n1   = texture3D ( noiseMap, tex1       ).xyz / 2.0;
	vec3	n2   = texture3D ( noiseMap, tex1 * 2.0 ).xyz / 4.0;
	vec3	n3   = texture3D ( noiseMap, tex1 * 4.0 ).xyz / 8.0;
	vec3	n4   = texture3D ( noiseMap, tex1 * 8.0 ).xyz / 16.0;
	
	return (n1 + n2 + n3 + n4) / (0.5 + 0.25 + 0.125 + 0.0625 );		// return normalized sum
}

float	density ( float v, float d  )
{
	return clamp ( (1.0 + d)*pow ( v, 4.0 ) - 0.3, 0.0, 1.0 );
}

void main ()
{
       const	vec4	cloudColor     = vec4 ( 1.0, 1.0, 1.0, 1.0 );
       vec3 v               = normalize ( McPosition.xyz );
       const	float	cloudDensity1  = 3.5;
     //  vec3	n1  = clouds ( vec3(McPosition.x*0.06, McPosition.y*0.06, McPosition.z*0.06), 0.5, vec2 ( 0.29, 0.109 ) ); 
       
       if(v.y>-0.35){
        /*vec3 l                  = normalize ( sunPos.xyz );
        vec3 ls                 = normalize ( vec3 (sunPos.x, sunPos.y-0.3, sunPos.z-0.3 ));
        float sunHighlight      = 0.8*pow(max(0.0, dot(ls, v)), 50.0);
        float largeSunHighlight = 0.3*pow(max(0.0, dot(ls, v)), 25.0);
        vec4	skyColor          = vec4	( clamp ( convertColor (), 0.0, 1.0 ) + sunHighlight + largeSunHighlight, 1.0 );
        float	alpha             = 0.75 * (1.2 - lv);*/
        skyColor               += alpha*textureCube ( tex, skyVec );
        /*gl_FragColor   =  texture3D ( noiseMap, vec3 ( McPosition.x*0.06, McPosition.y*0.06, 0.234753 ) + 0.1); 
        gl_FragColor = mix ( vec4(0.0, 0.0, 0.0, 0.0), cloudColor, density ( n1.x, cloudDensity1 ) );*/
        gl_FragColor = vec4	( 0.0, 0.0, 0.0, 1.0);
       }else{
        gl_FragColor = vec4	( 0.0, 0.0, 0.0, 1.0);
       }
}
