uniform float tick;

varying vec3 normal;
varying vec4 vertexWorldPos;

void main()
{
	vec4 vertexPos =  ftransform();
    gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_FrontColor = gl_Color;

    vertexWorldPos = gl_ModelViewMatrix * gl_Vertex;
    normal = gl_NormalMatrix * gl_Normal;

    vertexPos.y += cos(tick*0.1 + vertexPos.x * 0.1) * sin(tick*0.1 + vertexPos.x * 0.1 + 0.483921) * 0.15;

    gl_Position = vertexPos;
}
