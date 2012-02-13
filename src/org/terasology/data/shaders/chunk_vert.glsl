varying vec3 normal;
varying vec4 vertexWorldPosRaw;
varying vec4 vertexWorldPos;
varying vec3 eyeVec;
varying vec3 lightDir;

uniform float tick;
uniform float wavingCoordinates[32];
uniform vec2 waterCoordinate;
uniform vec2 lavaCoordinate;
uniform vec3 chunkOffset;

void main()
{
	gl_TexCoord[0] = gl_MultiTexCoord0;
    gl_TexCoord[1] = gl_MultiTexCoord1;

	vertexWorldPosRaw = gl_Vertex;

	vertexWorldPos = gl_ModelViewMatrix * vertexWorldPosRaw;

	lightDir = gl_LightSource[0].position.xyz;
	eyeVec = -vertexWorldPos.xyz;

    normal = gl_NormalMatrix * gl_Normal;
    gl_FrontColor = gl_Color;

	float distance = length(vertexWorldPos);

    #ifdef ANIMATED_WATER_AND_GRASS
    vec3 vertexChunkPos = vertexWorldPosRaw.xyz + chunkOffset.xyz;

    if (distance < 64.0) {
        // GRASS ANIMATION
        for (int i=0; i < 32; i+=2) {
           if (gl_TexCoord[0].x >= wavingCoordinates[i] && gl_TexCoord[0].x < wavingCoordinates[i] + TEXTURE_OFFSET && gl_TexCoord[0].y >= wavingCoordinates[i+1] && gl_TexCoord[0].y < wavingCoordinates[i+1] + TEXTURE_OFFSET) {
               if (gl_TexCoord[0].y < wavingCoordinates[i+1] + TEXTURE_OFFSET / 2.0) {
                   vertexWorldPos.x += sin(tick*0.05 + vertexChunkPos.x + vertexChunkPos.z) * 0.25;
                   vertexWorldPos.y += sin(tick*0.075 + vertexChunkPos.x + vertexChunkPos.z) * 0.25;
               }
           }
        }
       }

       if (gl_TexCoord[0].x >= waterCoordinate.x && gl_TexCoord[0].x < waterCoordinate.x + TEXTURE_OFFSET && gl_TexCoord[0].y >= waterCoordinate.y && gl_TexCoord[0].y < waterCoordinate.y + TEXTURE_OFFSET) {
            vertexWorldPos.y += sin(tick * 0.05 + vertexChunkPos.x +  + vertexChunkPos.z) * sin(tick * 0.075 + vertexChunkPos.x  + vertexChunkPos.z + 16.0) * 0.1;
       } else if (gl_TexCoord[0].x >= lavaCoordinate.x && gl_TexCoord[0].x < lavaCoordinate.x + TEXTURE_OFFSET && gl_TexCoord[0].y >= lavaCoordinate.y && gl_TexCoord[0].y < lavaCoordinate.y + TEXTURE_OFFSET) {
            vertexWorldPos.y += sin(tick * 0.05 + vertexChunkPos.x + vertexChunkPos.z) * 0.1;
       }
    #endif

    gl_Position = gl_ProjectionMatrix * vertexWorldPos;
}
