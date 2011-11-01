varying	vec3 	colorYxy;
varying vec3  skyVec;
varying vec3  cloudVec;
varying	vec4 	McPosition;
varying	float lv;
uniform float time;
uniform samplerCube tex;
uniform	sampler3D	noiseMap;
//uniform bool showClouds;
uniform	vec4 	sunPos;

vec4 	eyePos = vec4(0.0, 0.0, 0.0, 1.0);
vec4 ResColor = vec4(0.0, 0.0, 0.0, 0.0); //Base color of cloud

float	colorExp = 6.0;

vec3	convertColor (){
  vec3 clrYxy = vec3 ( colorYxy );

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

vec4	density ( vec4 v, float d  ){
  return clamp ( vec4((1.0 + d)*pow ( v.x, 2.0 ) - 0.3,
                      (1.0 + d)*pow ( v.y, 2.0 ) - 0.3,
                      (1.0 + d)*pow ( v.z, 2.0 ) - 0.3,
                      (1.0 + d)*pow ( v.a, 2.0 ) - 0.3),
                 vec4( 0.0,0.0,0.0,0.0 ),
                 vec4( 1.0,1.0,1.0,0.8 ) );
}

//octave function
vec4	clouds ( const vec3 tex, float t, const vec3 vel ){
  vec3 	dt   = vel * t;
  vec3 	tex1 = tex + dt; 
  vec4	n1   = texture3D ( noiseMap, tex1.xyz       ) / 2.0;
  vec4	n2   = texture3D ( noiseMap, tex1.xyz * 2.0 ) / 4.0;
  vec4	n3   = texture3D ( noiseMap, tex1.xyz * 4.0 ) / 8.0;
  vec4	n4   = texture3D ( noiseMap, tex1.xyz * 8.0 ) / 16.0;
  vec4	n5   = texture3D ( noiseMap, tex1.xyz * 16.0 ) / 32.0;

  vec4 result = (n1 + n2 + n3 + n4 + n5 )/(0.8 );		// return normalized sum
  const	float	cloudDensity1  = 2.5;

  return mix ( vec4(0.0, 0.0, 0.0, 0.0), vec4(1.0, 1.0, 1.0, 1.0), density(result, cloudDensity1) );

}



void main (){

  vec3 v                 = normalize ( McPosition.xyz );
  vec4	skyColor         = vec4(0.0, 0.0, 0.0, 0.0);

  if(v.y>-0.35){
    vec3 l                  = normalize ( sunPos.xyz );
    vec3 ls                 = normalize ( vec3 (sunPos.x, sunPos.y-0.3, sunPos.z-0.3 ));
    float sunHighlight      = 0.8*pow(max(0.0, dot(ls, v)), 50.0);
    float largeSunHighlight = 0.3*pow(max(0.0, dot(ls, v)), 25.0);
    skyColor          = vec4	( clamp ( convertColor (), 0.0, 1.0 ) + sunHighlight + largeSunHighlight, 1.0 );

    if(false){
      ResColor = vec4(1.0, 1.0, 1.0, 1.0);
      vec4	n1 = clouds ( 0.6112*cloudVec, time*0.05, vec3 ( 0.29987, 0.101123, 0.0 ) ); 
      float alphaCloud = n1.a;
    

      //mix with skyColor
      if(sunPos.y<=0.5){
        ResColor = mix(vec4(1.0,0.7,0.7, 1.0),vec4(1.0,1.0,1.0,1.0),sunPos.y*(1.0/0.5)); //To Do: get alpha
      }else{
        if(sunPos.y<=0.0){
          ResColor = skyColor + vec4(0.1, 0.1, 0.1, 1.0); //To Do: get alpha
        }
      }

      //slightly reduce the alpha chanel 
      alphaCloud = mix(alphaCloud/2.0, alphaCloud, alphaCloud); //It's wrong

      vec3 sunDirection   = normalize (SunPos.xyz) ;

      //get shadow
      vec4	n2;
      for (float i=0.0; i<6.0; i+=1.0){
        n2  += clouds ( ( 0.6112*cloudVec - sunDirection)*0.1*i , time*0.05,vec3 ( 0.29987, 0.101123, 0.0 )  ); 
      }  

      float Tmp = n2.a*0.45;

      vec4 attenuation = (1.0 - clamp ((1.0-skyColor*0.5) * Tmp, 0.0, 1.0));

        ResColor *= attenuation;
    }
  }
  
    float	alpha  = (1.0-sunPos.y) * (1.2 - lv);
    skyColor    += alpha*textureCube ( tex, skyVec );
    skyColor    += ResColor;
    gl_FragColor = skyColor;
}
