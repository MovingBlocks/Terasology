uniform sampler2D textureAtlas;
uniform sampler2D textureWater;
uniform sampler2D textureLava;
uniform sampler2D textureEffects;

uniform float tick;
uniform float daylight = 1.0;
uniform bool swimming;
uniform bool carryingTorch;

varying vec4 vertexWorldPos;
varying vec3 eyeVec;
varying vec3 lightDir;
varying vec3 normal;

uniform vec2 waterCoordinate;
uniform vec2 lavaCoordinate;
uniform vec2 grassCoordinate;

void main(){
    vec4 texCoord = gl_TexCoord[0];
    vec4 color;

    float specFact = 0.05;

    // Switch the texture atlases based on the currently active texture
    if (texCoord.x >= waterCoordinate.x && texCoord.x < waterCoordinate.x + TEXTURE_OFFSET && texCoord.y >= waterCoordinate.y && texCoord.y < waterCoordinate.y + TEXTURE_OFFSET) {
        texCoord.x = mod(texCoord.x, TEXTURE_OFFSET) * (1.0 / TEXTURE_OFFSET);
        texCoord.y = mod(texCoord.y, TEXTURE_OFFSET) / (64.0 / (1.0 / TEXTURE_OFFSET));
        texCoord.y += mod(tick,95.0) * 1.0/96.0;

        color = texture2D(textureWater, vec2(texCoord));
        specFact = 1.0;
    } else if (texCoord.x >= lavaCoordinate.x && texCoord.x < lavaCoordinate.x + TEXTURE_OFFSET && texCoord.y >= lavaCoordinate.y && texCoord.y < lavaCoordinate.y + TEXTURE_OFFSET) {
        texCoord.x = mod(texCoord.x, TEXTURE_OFFSET) * (1.0 / TEXTURE_OFFSET);
        texCoord.y = mod(texCoord.y, TEXTURE_OFFSET) / (128.0 / (1.0 / TEXTURE_OFFSET));
        texCoord.y += mod(tick,127.0) * 1.0/128.0;

        color = texture2D(textureLava, vec2(texCoord));
    } else {
        color = texture2D(textureAtlas, vec2(texCoord));
    }

    color = srgbToLinear(color);

    if (color.a < 0.1)
        discard;

    // APPLY TEXTURE OFFSET
    if (!(texCoord.x >= grassCoordinate.x && texCoord.x < grassCoordinate.x + TEXTURE_OFFSET && texCoord.y >= grassCoordinate.y && texCoord.y < grassCoordinate.y + TEXTURE_OFFSET)) {
        color.rgb *= gl_Color.rgb;
        color.a *= gl_Color.a;
    } else {
        // MASK GRASS
        if (texture2D(textureEffects, vec2(10.0 * TEXTURE_OFFSET + mod(texCoord.x,TEXTURE_OFFSET), mod(texCoord.y,TEXTURE_OFFSET))).a != 0.0)
            color.rgb *= gl_Color.rgb;
        else
            color.rgb *= gl_Color.a;
    }

    // Calculate daylight lighting value
    float daylightValue = daylight * expLightValue(gl_TexCoord[1].x);

    float blocklightDayIntensity = 1.0 - daylightValue * 0.95;
    float blocklightValue = expLightValue(gl_TexCoord[1].y);

    float occlusionValue = gl_TexCoord[1].z;

    float diffuseLighting = lambLight(normal, -normalize(vertexWorldPos.xyz));
    float torchlight = 0.0;

    // Apply torchlight
    if (carryingTorch) {
       torchlight = torchlight(diffuseLighting, vertexWorldPos.xyz) * blocklightDayIntensity;
    }

    // Apply some Lambertian lighting to the daylight light value
    vec3 daylightColorValue = vec3(daylightValue * 0.85 + diffuseLighting * daylightValue * 0.15);

    // Add specular highlights
    daylightColorValue += specLight(normal, lightDir, eyeVec, 2.0) * daylightValue * specFact;

    float blockBrightness = clamp(blocklightValue + torchlight - ((sin(tick*0.05) + 1.0) / 16.0) * blocklightValue, 0.0, 1.0);
    blockBrightness *= blocklightDayIntensity;
    vec3 blocklightColorValue = vec3(blockBrightness * 1.0, blockBrightness * 0.99, blockBrightness * 0.98);

    // Apply the final lighting mix
    color.xyz *= (daylightColorValue * occlusionValue + blocklightColorValue * occlusionValue);
    gl_FragColor = linearToSrgb(color);
}
