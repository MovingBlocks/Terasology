varying float fog;

void main()
{
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;
	gl_TexCoord[0] = gl_MultiTexCoord0;
        gl_FrontColor = gl_Color;

        gl_FogFragCoord = length(gl_Position);
        float fogScale = 1.0 / (gl_Fog.end - gl_Fog.start);
        fog = (gl_Fog.end - gl_FogFragCoord) * fogScale;
        fog = 1.0 - clamp(fog, 0.0, 1.0);
}
