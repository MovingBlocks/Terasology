#version 120

varying float fog;

float fogEyeRadial(vec4 eyePos) {
    return length(eyePos / eyePos.w);
}

void main()
{
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    gl_FrontColor = gl_Color;
}
