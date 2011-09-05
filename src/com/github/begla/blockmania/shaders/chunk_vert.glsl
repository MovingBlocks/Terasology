#version 120

varying float fog;

float fogEyeRadial(vec4 eyePos) {
    return length(eyePos / eyePos.w);
}

void main()
{
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
	gl_Normal = vec3(0,1,0);

	gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_TexCoord[1] = gl_MultiTexCoord1;
    gl_FrontColor = gl_Color;

    gl_FogFragCoord = fogEyeRadial(gl_ModelViewMatrix  * gl_Vertex);

    float fogScale = 1.0 / (gl_Fog.end - gl_Fog.start);
    fog = (gl_Fog.end - gl_FogFragCoord) * fogScale;
    fog = clamp(1.0 - fog,0.0,1.0);
}
