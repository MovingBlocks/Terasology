#version 120

uniform sampler2D textureAtlas;
uniform float daylight = 1.0;

varying float fog;
varying vec3 normal;

vec3 daylightColor = vec3(1.15, 1.13, 1.13);
vec3 moonlightColor = vec3(0.7, 0.7, 1.0);

vec4 srgbToLinear(vec4 color){
    return pow(color, vec4(1.0 / 2.2));
}

vec4 linearToSrgb(vec4 color){
    return pow(color, vec4(2.2));
}

void main(){
    vec4 color = texture2D(textureAtlas, vec2(gl_TexCoord[0]));
    color = srgbToLinear(color);

    /*
        Apply non-grey vertex colors only to grey texture values.
    */
    if (color.r == color.g && color.g == color.b) {
        color.rgb *= gl_Color.rgb; 
    }

    vec2 lightCoord = vec2(gl_TexCoord[1]);

    float daylightPow = clamp(pow(0.8, (1.0-daylight)*15.0)+0.5, 0.0, 1.0);
    float daylightValue = daylightPow * lightCoord.x;

    float blocklightValue = lightCoord.y;

    vec3 daylightColorValue = vec3(daylightValue);
    vec3 blocklightColorValue = vec3(blocklightValue);

    /*
        Blocklight has a reddish color.
    */
    blocklightColorValue.r *= 1.0;
    blocklightColorValue.g *= 0.8;
    blocklightColorValue.b *= 0.8;

    /*
        Nights are slightly bluish and not completely black.
    */
    daylightColorValue.r *= moonlightColor.x + (1.0 - moonlightColor.x) * daylight;
    daylightColorValue.g *= moonlightColor.y + (1.0 - moonlightColor.y) * daylight;
    daylightColorValue.b *= moonlightColor.z + (1.0 - moonlightColor.z) * daylight;

    daylightColorValue.r *= daylightColor.x + (1.0 - daylightColor.x) * (1.0-daylight);
    daylightColorValue.g *= daylightColor.y + (1.0 - daylightColor.y) * (1.0-daylight);
    daylightColorValue.b *= daylightColor.z + (1.0 - daylightColor.z) * (1.0-daylight);

    blocklightColorValue = clamp(blocklightColorValue,0.0,1.0);
    daylightColorValue = clamp(daylightColorValue, 0.0, 1.0);

    color.xyz *= clamp(daylightColorValue + blocklightColorValue, 0.0, 1.0);

    gl_FragColor.rgb = mix(linearToSrgb(color), vec4(0.84,0.88,1,1) * daylight, fog).rgb;
    gl_FragColor.w = color.w;
}
