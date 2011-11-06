#version 120
varying	vec3 colorYxy;
varying vec3 skyVec;
varying vec3 cloudVec;
varying	vec4 McPosition;
varying	float lv;
uniform float time;
uniform	vec4  sunPos;
uniform samplerCube texCube;
uniform	sampler3D   noiseMap;

vec4 	eyePos   = vec4(0.0, 0.0, 0.0, 1.0);
float	colorExp = 25.0;

vec3 convertColor (){
    vec3 clrYxy = vec3 ( colorYxy );
    clrYxy [0] = 1.0 - exp ( -clrYxy [0] / colorExp );

    float	ratio    = clrYxy [0] / clrYxy [2];	

    vec3	XYZ;

    XYZ.x = clrYxy [1] * ratio;						// X = x * ratio
    XYZ.y = clrYxy [0];							// Y = Y
    XYZ.z = ratio - XYZ.x - XYZ.y;					// Z = ratio - X - Y

    const vec3 rCoeffs = vec3 ( 3.240479, -1.53715, -0.49853  );
    const vec3 gCoeffs = vec3 ( -0.969256, 1.875991, 0.041556 );
    const vec3 bCoeffs = vec3 ( 0.055684, -0.204043, 1.057311 );

    return vec3 ( dot ( rCoeffs, XYZ ), dot ( gCoeffs, XYZ ), dot ( bCoeffs, XYZ ));
}

float	density ( float v, float d  ){
    return clamp ( (1.0 + d)*pow ( v, 4.0 ) - 0.3, 0.0, 1.0 );
}

vec3 clouds ( const vec3 tex, float t, const vec2 vel ){
    vec3 dt   = vec3 ( vel, 0.06 ) * t;
    vec3 tex1 = vec3 ( tex.x, tex.y, tex.z ) + dt; 

    vec3 n1   = texture3D ( noiseMap, tex1.xyz       ).xyz / 2.0;
    vec3 n2   = texture3D ( noiseMap, tex1.xyz * 2.0 ).xyz / 4.0;
    vec3 n3   = texture3D ( noiseMap, tex1.xyz * 4.0 ).xyz / 8.0;
    vec3 n4   = texture3D ( noiseMap, tex1.xyz * 8.0 ).xyz / 16.0;
    vec3 n5   = texture3D ( noiseMap, tex1.xyz * 16.0 ).xyz / 32.0;

    return (n1 + n2 + n3 + n4 + n5) / (0.5 + 0.25 + 0.125 + 0.0625 );
}



void main (){
    const vec4 cloudColor = vec4 ( 1.0, 1.0, 1.0, 1.0 );
    vec3 v = normalize ( McPosition.xyz );


    if(v.y>-0.35){
        vec3 l                  = normalize ( sunPos.xyz );
        vec3 ls                 = normalize ( vec3 (sunPos.x, sunPos.y-0.3, sunPos.z-0.3 ));
        float sunHighlight      = 0.8*pow(max(0.0, dot(ls, v)), 45.0);
        float largeSunHighlight = 0.3*pow(max(0.0, dot(ls, v)), 28.0);
        float posSunY = 0.0;

        if(sunPos.y>0.0){
            posSunY = sunPos.y;
        }

        /*ALPHA STARRY NIGHT*/
        float alpha  = (0.7-posSunY) * (1.0 - lv);

        if(alpha<0.0){
            alpha = 0.0;
        }

        vec4 skyColor = vec4	( clamp ( convertColor (), 0.0, 1.0 ) + sunHighlight + largeSunHighlight, 1.0 );

        /*GET CLOUDS*/
        vec3 n1       = clouds ( 0.2*cloudVec, time*10.5, vec2 ( 0.03, 0.01 ) ); 
        vec4 ResColor = vec4(1.0,1.0,1.0, 1.0);

        if(sunPos.y<=0.5){
            ResColor = mix(vec4(0.7,0.7,0.7, 1.0),vec4(1.0,1.0,1.0,1.0),sunPos.y*(1.0/0.5));
        }else{
            if(sunPos.y<=0.0){
                ResColor = vec4(0.1, 0.1, 0.1, 1.0);
            }
        }

        vec3 SunD         = sunPos.xyz;
        vec3 sunDirection = normalize (SunD) ;


        vec3 n2; //for shadow on the clouds
        vec3 posParticleCloud;
        vec4 shadowCloud; //result shadow of cloud

        /*STARRY NIGHT*/
        skyColor += alpha*textureCube ( texCube, skyVec );

        /*SHADOW ON THE CLOUDS*/
        for (float i=0.0; i<6.0; i+=1.0){
            posParticleCloud = 0.2*(cloudVec + sunDirection)*i;
            n2  = clouds ( posParticleCloud, time*10.5, vec2 ( 0.03, 0.01 ) ); 
            shadowCloud += 0.07*mix ( skyColor, vec4(1.0,1.0,1.0, 1.0), density ( n2.y, 3.5 ) );
        }  

        /*CALCULATE CLOUDS WITH SHADOW*/
        ResColor = mix ( skyColor, vec4(clamp(ResColor.xyz*(ResColor.xyz - shadowCloud.xyz), 0.0, 1.0), 1.0), density ( n1.y, 3.5 ) );

       // gl_FragColor = ResColor; //not work 
        gl_FragColor = skyColor;
    }else{
        gl_FragColor = vec4	( 0.0, 0.0, 0.0, 1.0);
    }
}
