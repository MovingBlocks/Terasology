varying vec3 vertexWorldPos;
varying vec3 normal;

uniform float tick;
uniform float wavingCoordinates[32];
uniform vec2 waterCoordinate;
uniform vec2 lavaCoordinate;

void main()
{
	vertexWorldPos = (gl_ModelViewMatrix * gl_Vertex).xyz;
	gl_Position = gl_ModelViewProjectionMatrix * gl_Vertex;

	gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_TexCoord[1] = gl_MultiTexCoord1;

    normal = gl_NormalMatrix * gl_Normal;

    float colorOffset = 1.0;

    // Slightly dim three sides of blocks
    if (normal == vec3(1.0, 0.0, 0.0) || normal == vec3(0.0, -1.0, 0.0) || normal == vec3(0.0, 0.0, 1.0))
        colorOffset = 0.9;

    gl_FrontColor = gl_Color * colorOffset;

#ifdef ANIMATED_WATER_AND_GRASS
       // GRASS ANIMATION
        for (int i=0; i < 32; i+=2) {
           if (gl_TexCoord[0].x >= wavingCoordinates[i] && gl_TexCoord[0].x < wavingCoordinates[i] + TEXTURE_OFFSET && gl_TexCoord[0].y >= wavingCoordinates[i+1] && gl_TexCoord[0].y < wavingCoordinates[i+1] + TEXTURE_OFFSET) {
               if (gl_TexCoord[0].y < wavingCoordinates[i+1] + TEXTURE_OFFSET / 2.0) {
                   gl_Position.x += sin(tick*0.05 + gl_Position.x + 1.437291) * 0.2;
                   gl_Position.y += sin(tick*0.01 + gl_Position.x) * 0.15;
               }
           }
        }

       if (gl_TexCoord[0].x >= waterCoordinate.x && gl_TexCoord[0].x < waterCoordinate.x + TEXTURE_OFFSET && gl_TexCoord[0].y >= waterCoordinate.y && gl_TexCoord[0].y < waterCoordinate.y + TEXTURE_OFFSET) {
            gl_Position.y += sin(tick * 0.05 + gl_Position.x) * 0.05;
       } else if (gl_TexCoord[0].x >= lavaCoordinate.x && gl_TexCoord[0].x < lavaCoordinate.x + TEXTURE_OFFSET && gl_TexCoord[0].y >= lavaCoordinate.y && gl_TexCoord[0].y < lavaCoordinate.y + TEXTURE_OFFSET) {
            gl_Position.y += sin(tick * 0.05 + gl_Position.x) * 0.05;
       }
#endif

    // Linear fog
    vec4 eyePos = gl_ModelViewMatrix * gl_Vertex;
    gl_FogFragCoord = abs(eyePos.z/eyePos.w);
}
