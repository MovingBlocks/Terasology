varying vec2 tex_coord;

void main()
{
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
	tex_coord = vec2(gl_MultiTexCoord0);
        gl_FrontColor = gl_Color;
}
