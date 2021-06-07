// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

float linDepth(float depth) {
    return (2.0 * zNear) / (zFar + zNear - depth * (zFar - zNear));
}

float linDepthViewingDistance(float depth) {
    return (linDepth(depth) * zFar) / viewingDistance;
}

vec3 uncharted2Tonemap(vec3 x) {
	return ((x*(A*x+C*B)+D*E)/(x*(A*x+B)+D*F))-E/F;
}

float tonemapReinhard(float brightMax, float exposure) {
    return exposure * (exposure/(brightMax * brightMax) + 1.0) / (exposure + 1.0);
}

float calcLambLight(vec3 normal, vec3 lightVec) {
    float diffuse = dot(normal, lightVec);

    if (diffuse < 0.0) {
        return 0.0;
    }

    return diffuse;
}

float calcSpecLightNormalized(vec3 normal, vec3 lightVec, vec3 eyeVec, float exp) {
    return pow(max(0.0, dot(-eyeVec, reflect(lightVec, normal))), exp);
}

vec4 linearToSrgb(vec4 color) {
     return vec4(sqrt(color.rgb), color.a);
}

vec4 srgbToLinear(vec4 color) {
    return vec4(color.rgb * color.rgb, color.a);
}

float expBlockLightValue(float light) {
	float lightScaled = (1.0 - light) * 16.0;
	return pow(BLOCK_LIGHT_POW, lightScaled) * light * BLOCK_INTENSITY_FACTOR;
}

float rescaleRange(float inputvalue, float oldMin,float oldMax, float newMin,float newMax)
{
    float oldRange = (oldMax - oldMin);
    if (oldRange == 0)
        return newMin;
    else
    {
        float newRange = (newMax - newMin);
        return (((inputvalue - oldMin) * newRange) / oldRange) + newMin;
    }
}

float expLightValue(float light) {
	float lightScaled = (1.0 - light) * 16.0;
	return pow(BLOCK_LIGHT_SUN_POW, lightScaled) * light;
}

float expOccValue(float light) {
    return light * light;
}

float timeToTick(float time, float speed) {
    return time * 4000.0 * speed;
}

float fresnel(float nDotL, float fresnelBias, float fresnelPow) {
  float facing = (1.0 - nDotL);
  return clamp(fresnelBias + (1.0 - fresnelBias) * pow(facing, fresnelPow), 0.0, 1.0);
}

vec3 convertColorYxy(vec3 color, float colorExp) {
    if (color.x < 0.0 || color.y < 0.0 || color.z < 0.0) {
        return vec3(0.0, 0.0, 0.0);
    }

    vec3 clrYxy = color;
    clrYxy.x = 1.0 - exp (-clrYxy.x / colorExp);

    float ratio = clrYxy.x / clrYxy.z;

    vec3 XYZ;
    XYZ.x = clrYxy.y * ratio;
    XYZ.y = clrYxy.x;
    XYZ.z = ratio - XYZ.x - XYZ.y;

    const vec3 rCoeffs = vec3 (3.240479, -1.53715, -0.49853);
    const vec3 gCoeffs = vec3 (-0.969256, 1.875991, 0.041556);
    const vec3 bCoeffs = vec3 (0.055684, -0.204043, 1.057311);

    return vec3 (dot(rCoeffs, XYZ), dot(gCoeffs, XYZ), dot(bCoeffs, XYZ));
}

vec2 projectVertexToTexCoord(vec4 projVertexPos) {
    return 0.5 * (projVertexPos.xy/projVertexPos.w) + vec2(0.5);
}

// NOTE: Can also be used to reconstruct the position in world space by passing the inverse view projection matrix
vec3 reconstructViewPos(float depth, vec2 texCoord, mat4 paramInvProjMatrix) {
    vec4 screenSpaceNorm = vec4(texCoord.x, texCoord.y, depth, 1.0);
    vec4 screenSpacePos = screenSpaceNorm * vec4(2.0, 2.0, 1.0, 1.0) - vec4(1.0, 1.0, 0.0, 0.0);
    vec4 viewSpacePos = paramInvProjMatrix * screenSpacePos;
    return viewSpacePos.xyz / viewSpacePos.w;
}

vec2 normalizeAtlasTexCoord(vec2 atlasTexCoord) {
    return vec2(mod(atlasTexCoord.x, TEXTURE_OFFSET) * (1.0 / TEXTURE_OFFSET),
                mod(atlasTexCoord.y, TEXTURE_OFFSET) * (1.0 / TEXTURE_OFFSET));
}

float calcVolumetricFog(vec3 fogWorldPosition, float volumetricHeightDensityAtViewer, float globalDensity, float heightFalloff) {
    vec3 cameraToFogWorldPosition = -fogWorldPosition;

    float totalFogToSample = length(cameraToFogWorldPosition) * globalDensity * volumetricHeightDensityAtViewer;

    const float slopeThreshold = 0.01;
    float heightDensityFactor = heightFalloff * cameraToFogWorldPosition.y;
    if (abs(heightDensityFactor) > slopeThreshold) {
        totalFogToSample *= (1.0 - exp(-heightDensityFactor)) / heightDensityFactor;
    }

    return 1 - exp(-totalFogToSample);
}

float calcLuminance(vec3 color) {
    return 0.2126 * color.r + 0.7152 * color.g + 0.0722 * color.b;
}

vec3 calcBlocklightColor(float blockBrightness) {
    // Calculate the final blocklight color value and add a slight reddish tint to it
    return vec3(blockBrightness) * vec3(1.0, 0.95, 0.94);
}

float calcBlocklightColorBrightness(float blocklightValue
#if defined (FLICKERING_LIGHT)
, float flickeringLightOffset
#endif
) {
    // Calculate the final block light brightness
    float blockBrightness = expBlockLightValue(blocklightValue);
#if defined (FLICKERING_LIGHT)
    blockBrightness -= clamp(flickeringLightOffset,0,1) * blocklightValue;
#endif
    return blockBrightness;
}

float calcDayAndNightLightingFactor(float daylightValue, float daylight) {
    float daylightScaledValue = daylight * daylightValue;
    return expLightValue(daylightScaledValue) + (NIGHT_BRIGHTNESS * (1.0 - daylight) * expLightValue(daylightValue));
}

vec3 calcSunlightColorDeferred(float daylightValue, float diffuseLighting, float ambientIntensity, float diffuseIntensity, vec3 ambientColor, vec3 diffuseColor) {
    vec3 daylightColorValue = max(vec3(ambientIntensity),diffuseLighting * diffuseIntensity * diffuseColor);

    vec3 ambientTint = mix(vec3(MOONLIGHT_AMBIENT_COLOR), vec3(DAYLIGHT_AMBIENT_COLOR), daylight) * ambientColor;
    daylightColorValue.xyz *= ambientTint;

    // Scale the lighting according to the daylight and daylight block values and add moonlight during the nights
    daylightColorValue.xyz *= calcDayAndNightLightingFactor(daylightValue, daylight);

    return daylightColorValue;
}

vec3 calcSunlightColorWater(float daylightValue, vec3 normal, vec3 sunVecViewAdjusted) {
    float diffuseLighting = calcLambLight(normal, sunVecViewAdjusted);
    return calcSunlightColorDeferred(daylightValue, diffuseLighting, WATER_AMB, WATER_DIFF, vec3(1.0), vec3(1.0));
}

vec3 calcSunlightColorOpaque(float daylightValue, vec3 normal, vec3 sunVecViewAdjusted) {
    float diffuseLighting = calcLambLight(normal, sunVecViewAdjusted);
    return calcSunlightColorDeferred(daylightValue, diffuseLighting, BLOCK_AMB, BLOCK_DIFF, vec3(1.0), vec3(1.0));
}

bool epsilonEquals(float val, float expectedResult) {
    return abs(val - expectedResult) < EPSILON;
}

bool epsilonEqualsOne(float val) {
    return epsilonEquals(val, 1.0);
}

vec3 cubizePoint(vec3 pos) {
    vec3 f = abs(pos);
    vec3 result = pos;

    if (f.y >= f.x && f.y >= f.z) {
        if (pos.y > 0) {
            result.y = 1.0;
        }
        else {
            // bottom face
            result.y = -1.0;
        }
    }
    else if (f.x >= f.y && f.x >= f.z) {
        if (pos.x > 0) {
            // right face
            result.x = 1.0;
        }
        else {
            // left face
            result.x = -1.0;
        }
    }
    else {
        if (pos.z > 0) {
            // front face
            result.z = 1.0;
        }
        else {
            // back face
            result.z = -1.0;
        }
    }

    return result;
}
