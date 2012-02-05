uniform sampler2D textureAtlas;
uniform sampler2D textureWater;
uniform sampler2D textureLava;
uniform sampler2D textureEffects;

uniform float tick;
uniform float daylight = 1.0;
uniform bool swimming;
uniform bool carryingTorch;

varying vec4 vertexWorldPos;
varying vec3 normal;
varying float distance;

uniform vec4 playerPosition;

uniform vec2 waterCoordinate;
uniform vec2 lavaCoordinate;
uniform vec2 grassCoordinate;

vec4 srgbToLinear(vec4 color){
    return pow(color, vec4(1.0 / GAMMA));
}

vec4 linearToSrgb(vec4 color){
    return pow(color, vec4(GAMMA));
}

void main(){
    vec4 texCoord = gl_TexCoord[0];
    vec4 color;

    // Switch the texture atlases based on the currently active texture
    if (texCoord.x >= waterCoordinate.x && texCoord.x < waterCoordinate.x + TEXTURE_OFFSET && texCoord.y >= waterCoordinate.y && texCoord.y < waterCoordinate.y + TEXTURE_OFFSET) {
        texCoord.x = mod(texCoord.x, TEXTURE_OFFSET) * (1.0 / TEXTURE_OFFSET);
        texCoord.y = mod(texCoord.y, TEXTURE_OFFSET) / (64.0 / (1.0 / TEXTURE_OFFSET));
        texCoord.y += mod(tick,95.0) * 1.0/96.0;

        color = texture2D(textureWater, vec2(texCoord));
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
    float daylightValue = daylight * pow(0.86, (1.0-gl_TexCoord[1].x)*15.0);

    float blocklightDayIntensity = 1.0 - daylightValue * 0.95;
    float blocklightValue = pow(0.86, (1.0-gl_TexCoord[1].y)*15.0);

    float occlusionValue = gl_TexCoord[1].z;

    float highlight = 0.0;
    float torchlight = 0.0;

    // Calculate Lambertian lighting
    vec3 N = normalize(normal * ((gl_FrontFacing) ? 1.0 : -1.0));
    vec3 L = normalize(-vertexWorldPos.xyz);

    highlight = dot(N,L);

    // Apply torchlight
    if (carryingTorch) {
        torchlight = highlight * clamp(1.0 - (distance / 16.0), 0.0, 1.0) * blocklightDayIntensity;
    }

    // Apply some lighting highlights to the daylight light value
    vec3 daylightColorValue = vec3(daylightValue * 0.95 + highlight * 0.05);

    float blockBrightness = blocklightValue + torchlight - ((sin(tick*0.05) + 1.0) / 16.0) * blocklightValue;
    blockBrightness *= blocklightDayIntensity;

    vec3 blocklightColorValue = vec3(blockBrightness * 1.0, blockBrightness * 0.99,blockBrightness * 0.98);

    // Apply the final lighting mix
    color.xyz *= (daylightColorValue * occlusionValue + blocklightColorValue * occlusionValue);

    // Apply linear fog
    float fog = clamp((gl_Fog.end - gl_FogFragCoord) * gl_Fog.scale, 0.25, 1.0);

    // Check if the player is below the water surface
    if (!swimming) {
        gl_FragColor.rgb = linearToSrgb(mix(vec4(1.0) * daylight, color, fog)).rgb;
        gl_FragColor.a = color.a;
    } else {
        color.rg *= 0.6;
        color.b *= 0.9;
        gl_FragColor.rgb = linearToSrgb(color).rgb;
        gl_FragColor.a = color.a;
    }
}
