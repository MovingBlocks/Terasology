float light(vec3 normal, vec4 worldPos) {
    vec3 N = normalize(normal * ((gl_FrontFacing) ? 1.0 : -1.0));
    vec3 L = normalize(-worldPos.xyz);

    return dot(N,L);
}

float torchlight(float light, vec4 worldPos) {
    return light * clamp(1.0 - (length(worldPos) / 16.0), 0.0, 1.0);
}

vec4 srgbToLinear(vec4 color) {
    return pow(color, vec4(1.0 / GAMMA));
}

vec4 linearToSrgb(vec4 color) {
    return pow(color, vec4(GAMMA));
}
