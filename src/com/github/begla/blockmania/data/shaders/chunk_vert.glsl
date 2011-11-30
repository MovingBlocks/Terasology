varying vec4 vertexWorldPos;
varying float fog;
uniform float tick;

float fogEyeRadial(vec4 eyePos) {
    return length(eyePos / eyePos.w);
}

void main()
{
    const vec4[5] grassCoordinates = vec4[5](
        vec4(0.0625 * 12, 0.0625 * 13, 0.0625 * 11, 0.0625 * 12),
        vec4(0.0625 * 13, 0.0625 * 14, 0.0625 * 0, 0.0625 * 1),
        vec4(0.0625 * 12, 0.0625 * 13, 0.0625 * 0, 0.0625 * 1),
        vec4(0.0625 * 13, 0.0625 * 14, 0.0625 * 11, 0.0625 * 12),
        vec4(0.0625 * 14, 0.0625 * 15, 0.0625 * 11, 0.0625 * 12)
    );

    const vec4 waterCoordinate = vec4(0.0625 * 15, 0.0625 * 16, 0.0625 * 12, 0.0625 * 13);
    const vec4 lavaCoordinate = vec4(0.0625 * 15, 0.0625 * 16, 0.0625 * 15, 0.0625 * 16);

	vec4 vertexPos = gl_ModelViewProjectionMatrix * gl_Vertex;

	gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_TexCoord[1] = gl_MultiTexCoord1;
    gl_FrontColor = gl_Color;

    // FOG
    gl_FogFragCoord = fogEyeRadial(gl_ModelViewMatrix  * gl_Vertex);

    float fogScale = 1.0 / (gl_Fog.end - gl_Fog.start);
    fog = (gl_Fog.end - gl_FogFragCoord) * fogScale;

    fog = clamp(1.0 - fog,0.0,1.0);

#ifdef ANIMATED_WATER_AND_GRASS
       // GRASS ANIMATION
       for (int i=0; i < 5; i++) {
           if (gl_TexCoord[0].x >= grassCoordinates[i].x && gl_TexCoord[0].x < grassCoordinates[i].y && gl_TexCoord[0].y >= grassCoordinates[i].z && gl_TexCoord[0].y < grassCoordinates[i].w) {
               if (gl_TexCoord[0].y < grassCoordinates[i].w - 0.0625 / 2) {
                   vertexPos.x += sin(tick*0.05 + vertexPos.x + 1.437291) * 0.2;
                   vertexPos.y += sin(tick*0.01 + vertexPos.x) * 0.15;
               }
           }
       }

       if (gl_TexCoord[0].x >= waterCoordinate.x && gl_TexCoord[0].x < waterCoordinate.y && gl_TexCoord[0].y >= waterCoordinate.z && gl_TexCoord[0].y < waterCoordinate.w) {
            vertexPos.y += sin(tick*0.1 + vertexPos.x) * 0.05;
        } else if (gl_TexCoord[0].x >= lavaCoordinate.x && gl_TexCoord[0].x < lavaCoordinate.y && gl_TexCoord[0].y >= lavaCoordinate.z && gl_TexCoord[0].y < lavaCoordinate.w) {
            vertexPos.y += sin(tick*0.1 + vertexPos.x) * 0.05;
        }
#endif

    gl_Position = vertexPos;
    vertexWorldPos = vertexPos;
}
