#version 120

uniform sampler2D textureAtlas;
uniform float daylight = 1.0;
uniform bool swimming;
uniform bool animated;
uniform float animationOffset;

varying float fog;
varying vec3 normal;

vec4 srgbToLinear(vec4 color){
    return pow(color, vec4(1.0 / 2.2));
}

vec4 linearToSrgb(vec4 color){
    return pow(color, vec4(2.2));
}

void main(){
    vec4 texCoord = gl_TexCoord[0];

    // TEXTURE ANIMATION
    if (animated) {
        texCoord.x *= 16;
        texCoord.y /= 4;

        texCoord.y += animationOffset;
    }

    float daylightTrans = pow(0.82, (1.0-daylight)*15.0);

    vec4 color = texture2D(textureAtlas, vec2(texCoord));
    color = srgbToLinear(color);

    if (color.a < 0.1)
        discard;

    // APPLY TEXTURE OFFSET
    if (!(texCoord.x >= 0.0625 * 3 && texCoord.x < 0.0625 * 4 && texCoord.y >= 0.0625 * 0 && texCoord.y < 0.0625 * 1)) {
        color.rgb *= gl_Color.rgb;
        color.a *= gl_Color.a;
    } else {
        // MASK GRASS
        if (texture2D(textureAtlas, vec2(texCoord.x + 3*0.0625, texCoord.y + 2*0.0625)).a > 0) {
            color.rgb *= gl_Color.rgb;
            color.a *= gl_Color.a;
        }
    }

    // FETCH LIGHT VALUES
    vec2 lightCoord = vec2(gl_TexCoord[1]);

    // CALCULATE DAYLIGHT AND BLOCKLIGHT
    float daylightValue = clamp(daylightTrans + 0.2, 0.0, 1.0) * pow(0.86, (1.0-lightCoord.x)*15.0);
    float blocklightValue = lightCoord.y;

    vec3 daylightColorValue = vec3(daylightValue);
    vec3 blocklightColorValue = vec3(blocklightValue);

    blocklightColorValue = clamp(blocklightColorValue,0.0,1.0);
    daylightColorValue = clamp(daylightColorValue, 0.0, 1.0);

    blocklightColorValue.x *= 1.2;
    blocklightColorValue.y *= 1.1;
    blocklightColorValue.z *= 0.7;

    color.xyz *= daylightColorValue + blocklightColorValue * (1.0-daylightValue);

    if (!swimming) {
        gl_FragColor.rgb = mix(linearToSrgb(color), vec4(0.95,0.95,1.0,1.0) * daylightTrans, clamp(fog,0.0,0.4)).rgb;
        gl_FragColor.a = color.a;
    } else {
        gl_FragColor.rgb = mix(linearToSrgb(color), vec4(0.0,0.0,0.0,1.0), fog*16.0).rgb;
        gl_FragColor.rg *= 0.4;
        gl_FragColor.b *= 0.7;
        gl_FragColor.a = 1.0;
    }
}
