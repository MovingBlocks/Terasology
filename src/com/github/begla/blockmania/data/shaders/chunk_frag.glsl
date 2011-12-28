uniform sampler2D textureAtlas;
uniform sampler2D textureWater;
uniform sampler2D textureLava;
uniform sampler2D textureEffects;

uniform float tick;
uniform float daylight = 1.0;
uniform bool swimming;
uniform bool carryingTorch;

varying vec3 vertexWorldPos;
varying vec3 normal;

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
    float daylightTrans = pow(0.86, (1.0-daylight)*15.0);
    float daylightValue = clamp(daylightTrans, 0.0, 1.0) * pow(0.92, (1.0-gl_TexCoord[1].x)*15.0);

    // Slightly flickering blocklight!
    float blocklightValue = gl_TexCoord[1].y - (sin(tick*0.02) + 1.0) / 16.0;;
    float occlusionValue = gl_TexCoord[1].z;

    // Calculate Lambertian lighting
    vec3 N = normalize(normal * ((gl_FrontFacing) ? 1.0 : -1.0));
    vec3 L = normalize(-vertexWorldPos);
    float highlight = clamp(dot(N,L) + 0.3, 0.0, 1.0);

    float torchlight = 0.0;

    // Apply torchlight
    if (carryingTorch) {
        torchlight = highlight * clamp(1.0 - (length(vertexWorldPos) / 16.0), 0.0, 1.0) * 0.75;
    }

    // Apply some lighting highlights to the daylight light value
    // Looks cool during morning and evning hours and can be seen moonlight during the night
    vec3 daylightColorValue = vec3(daylightValue + highlight * 0.25);

    vec3 blocklightColorValue = vec3(blocklightValue + torchlight);

    blocklightColorValue = clamp(blocklightColorValue,0.0,1.0);
    daylightColorValue = clamp(daylightColorValue, 0.0, 1.0);

    // Color block light a reddish
    blocklightColorValue.x *= 1.0;
    blocklightColorValue.y *= 0.95;
    blocklightColorValue.z *= 0.9;

    // Apply the final lighting mix
    color.xyz *= clamp(daylightColorValue + blocklightColorValue * (1.0-daylightValue), 0.0, 1.0) * occlusionValue;

    // Apply linear fog
    float fog = 1.0 - ((gl_Fog.end - gl_FogFragCoord) * gl_Fog.scale);
    fog /= 2.0;

    fog = clamp(fog, 0.0, 1.0);

    // Check if the player is below the water surface
    if (!swimming) {
        gl_FragColor.rgb = linearToSrgb(mix(color, vec4(1.0) * daylightTrans, fog)).rgb;
        gl_FragColor.a = color.a;
    } else {
        color.rg *= 0.6;
        color.b *= 0.9;
        gl_FragColor.rgb = linearToSrgb(color).rgb;
        gl_FragColor.a = color.a;
    }
}
