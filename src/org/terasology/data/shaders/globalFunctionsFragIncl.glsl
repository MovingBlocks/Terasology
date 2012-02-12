float lambLight(vec3 normal, vec3 lightVec) {
    return dot(normal,lightVec);
}

float specLight(vec3 normal, vec3 lightVec, vec3 eyeVec, float exp) {
    vec3 reflect = reflect(-normalize(lightVec), normalize(normal));
    return pow(max(dot(normalize(reflect), normalize(eyeVec)), 0.0), exp);
}

float torchlight(float light, vec3 lightPos) {
    return light * clamp(1.0 - (length(lightPos) / 16.0), 0.0, 1.0);
}

vec4 srgbToLinear(vec4 color) {
    return pow(color, vec4(1.0 / GAMMA));
}

vec4 linearToSrgb(vec4 color) {
    return pow(color, vec4(GAMMA));
}

float expLightValue(float light) {
    return pow(0.86, (1.0-light)*15.0);
}
