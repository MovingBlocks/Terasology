varying vec3 normal;
varying vec4 vertexWorldPos;

void main()
{
	vertexWorldPos = gl_ModelViewMatrix * gl_Vertex;
    normal = gl_NormalMatrix * gl_Normal;

	gl_Position = ftransform();
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_FrontColor = gl_Color;
}
