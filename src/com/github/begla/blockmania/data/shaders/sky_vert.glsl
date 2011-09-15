varying	vec3 	v;
varying	vec3 	l;
varying	vec3 	colorYxy;
varying	float	lv;						// dot (  l, v )
uniform	vec4 	sunPos;
vec4 	eyePos = vec4(0.0, 0.0, 0.0, 1.0);
uniform	float	turbidity;
uniform vec3 zenith;
//uniform float chi;
#define	EPS	0.1


vec3	allweather ( float t, float cosTheta, float cosGamma )
{
	float	gamma      = acos ( cosGamma );
	float	cosGammaSq = cosGamma * cosGamma;
	float	aY =  0.17872 * t - 1.46303;
	float	bY = -0.35540 * t + 0.42749;
	float	cY = -0.02266 * t + 5.32505;
	float	dY =  0.12064 * t - 2.57705;
	float	eY = -0.06696 * t + 0.37027;
	float	ax = -0.01925 * t - 0.25922;
	float	bx = -0.06651 * t + 0.00081;
	float	cx = -0.00041 * t + 0.21247;
	float	dx = -0.06409 * t - 0.89887;
	float	ex = -0.00325 * t + 0.04517;
	float	ay = -0.01669 * t - 0.26078;
	float	by = -0.09495 * t + 0.00921;
	float	cy = -0.00792 * t + 0.21023;
	float	dy = -0.04405 * t - 1.65369;
	float	ey = -0.01092 * t + 0.05291;

	return vec3 ( (1.0 + aY * exp(bY/cosTheta)) * (1.0 + cY * exp(dY * gamma) + eY*cosGammaSq),
	              (1.0 + ax * exp(bx/cosTheta)) * (1.0 + cx * exp(dx * gamma) + ex*cosGammaSq),
				  (1.0 + ay * exp(by/cosTheta)) * (1.0 + cy * exp(dy * gamma) + ey*cosGammaSq) );
}

vec3	allweatherSky ( float t, float cosTheta, float cosGamma, float cosThetaSun )
{
	float	thetaSun = acos( cosThetaSun );

	vec3	clrYxy   = zenith * allweather ( t, cosTheta, cosGamma ) / allweather ( t, 1.0, cosThetaSun );
  
 if(cosTheta<0.35){
   thetaSun=0;
  }

	//clrYxy [0] *= smoothstep ( 0.0, EPS, thetaSun);
	
	return clrYxy;
}

void main(void)
{
	v               = normalize ( (gl_Vertex-eyePos).xyz );
	l               = normalize ( sunPos.xyz );
	lv              = dot  ( l, v );
	colorYxy        = allweatherSky ( turbidity, v.y+0.35, lv, l.y );
	gl_Position     = gl_ModelViewProjectionMatrix * gl_Vertex;
	gl_TexCoord [0] = gl_MultiTexCoord0;
}