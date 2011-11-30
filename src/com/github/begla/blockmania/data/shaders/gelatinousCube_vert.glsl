uniform float tick;

void main()
{
	vec4 vertexPos = gl_ModelViewProjectionMatrix * gl_Vertex;
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_FrontColor = gl_Color;

    vertexPos.y += cos(tick*0.25 + vertexPos.x) * 0.2;

    gl_Position = vertexPos;
}
