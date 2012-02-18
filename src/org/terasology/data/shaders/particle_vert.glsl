varying vec4 vertexWorldPos;

void main()
{
	vertexWorldPos = ftransform();

	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
    gl_TexCoord[0] = gl_MultiTexCoord0;
}
