#version 120

uniform sampler2D textureAtlas;
uniform float daylight = 1.0;

varying vec4 vertColor;
varying float fog;
varying vec3 normal;

vec4 gamma(vec4 color){
    return pow(color, vec4(1.0 / 1.2));
}

void main(){
    vec4 color = gl_Color * texture2D(textureAtlas, vec2(gl_TexCoord[0]));
    vec3 lightCoord = vec3(gl_TexCoord[1]);

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
        Nights are slightly bluish.
    */
    daylightValue.b += 0.1;

    color.xyz *= clamp(daylightValue + blocklightValue, 0.0, 1.0);

    gl_FragColor.rgb = gamma(mix(color, gl_Fog.color, fog / 2.0)).rgb;
    gl_FragColor.w = color.w;
}
