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

    /*
        Apply non-grey vertex colors only to grey texture values.
    */
    if (color.r == color.g && color.g == color.b) {
        color.rgb *= gl_Color.rgb; 
    }

    vec2 lightCoord = vec2(gl_TexCoord[1]);

    float daylightPow = pow(0.8, (1.0-daylight)*15.0);

    vec3 daylightValue = vec3(daylightPow * lightCoord.x);
    vec3 blocklightValue = vec3(lightCoord.y);

    /*
        Blocklight has a reddish color.
    */
    blocklightValue.r *= 1.0;
    blocklightValue.g *= 0.4;
    blocklightValue.b *= 0.2;

    /*
        Nights are slightly bluish and not completely black.
    */
    daylightValue.b += 0.175;
    daylightValue.g += 0.075;
    daylightValue.r += 0.075;


    color.xyz *= clamp(daylightValue + blocklightValue, 0.0, 1.0);

    gl_FragColor.rgb = mix(linearToSrgb(color), gl_Fog.color, fog / 2.0).rgb;
    gl_FragColor.w = color.w;
}
