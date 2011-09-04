#version 120

uniform sampler2D textureAtlas;
uniform float daylight = 1.0;

varying float fog;
varying vec3 normal;

vec4 srgbToLinear(vec4 color){
    return pow(color, vec4(1.0 / 2.2));
}

vec4 linearToSrgb(vec4 color){
    return pow(color, vec4(2.2));
}

void main(){
    vec4 color = texture2D(textureAtlas, vec2(gl_TexCoord[0]));
    color = srgbToLinear(color);

    if (color.a < 0.1)
        discard;

    color.rgb *= gl_Color.rgb;
    color.a *= gl_Color.a;

    vec2 lightCoord = vec2(gl_TexCoord[1]);

    float daylightPow = clamp(pow(0.8, (1.0-daylight)*15.0) + 0.4, 0.0, 1.0);
    float daylightValue = daylightPow * lightCoord.x;

    float blocklightValue = lightCoord.y;

    vec3 daylightColorValue = vec3(daylightValue);
    vec3 blocklightColorValue = vec3(blocklightValue);

    blocklightColorValue.r *= 1.0;
    blocklightColorValue.g *= 0.9;
    blocklightColorValue.b *= 0.8;

    blocklightColorValue = clamp(blocklightColorValue,0.0,1.0);
    daylightColorValue = clamp(daylightColorValue, 0.0, 1.0);

    color.xyz *= daylightColorValue + blocklightColorValue * (1.0-daylightValue);

    gl_FragColor.rgb = mix(linearToSrgb(color), vec4(0.84,0.88,1,1) * daylight, fog).rgb;
    gl_FragColor.a = color.a;
}
