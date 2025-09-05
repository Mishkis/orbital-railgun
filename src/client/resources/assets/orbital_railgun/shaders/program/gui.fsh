#version 330 compatibility
#define STEPS 500
#define MIN_DIST 0.001
#define MAX_DIST 250.

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform mat4 InverseTransformMatrix;
uniform mat4 ModelViewMat;
uniform vec3 CameraPosition;

uniform float IsBlockHit;
uniform vec3 BlockPosition;

const vec3 red = 2. * vec3(0.878, 0.427, 0.427);
const vec3 green = 2. * vec3(0.13, 0.65, 0.23);
const vec3 blue = vec3(0.62, 0.93, 0.93);

vec2 scale = vec2(0.);

uniform float iTime;

in vec2 texCoord;
in float viewHeight;
in float viewWidth;

out vec4 fragColor;

// https://www.reedbeta.com/blog/hash-functions-for-gpu-rendering/
float pcg_hash(uint seed) {
    uint state = seed * 747796405u + 2891336453u;
    uint word = ((state >> ((state >> 28u) + 4u)) ^ state) * 277803737u;
    return float((word >> 22u) ^ word) / 2147483647.;
}

// https://iquilezles.org/articles/distfunctions/
float sDist(vec3 p) {
    p = abs(p) - vec3(0.51);
    const float e = 0.02;
    vec3 q = abs(p + e) - e;
    return min(min(
            length(max(vec3(p.x, q.y, q.z), 0.0)) + min(max(p.x, max(q.y, q.z)), 0.0),
            length(max(vec3(q.x, p.y, q.z), 0.0)) + min(max(q.x, max(p.y, q.z)), 0.0)),
        length(max(vec3(q.x, q.y, p.z), 0.0)) + min(max(q.x, max(q.y, p.z)), 0.0));
}

float sdBox(vec2 p, vec2 s) {
    p = abs(p) - s * scale;
    return length(max(p, 0.)) + min(max(p.x, p.y), 0.);
}

vec3 renderUi(vec3 original, float dist) {
    vec3 col = green;
    vec3 overlay = mix(col, col / 2., smoothstep(0.01, 0., dist));
    float threshold = 0.25 * step(0., -dist) + step(0., dist) * 0.004 / dist;
    threshold = clamp(threshold, 0., 1.);

    float scan_lines = 1. + max(sin(20. * (texCoord.y) + 0.5 * iTime) - sin(80. * (texCoord.y) + 3. * iTime), 0.) / 2.;
    overlay *= scan_lines;

    float glitch = 1. + 0.2 * pcg_hash(uint(round((texCoord.x + texCoord.y * viewHeight) * viewWidth) + round(iTime * viewWidth * viewHeight)));
    overlay *= glitch;

    return mix(original, overlay, threshold);
}

vec2 raycast(vec3 point, vec3 dir) {
    float traveled = 0.;
    int close_steps = 0;
    for (int i = 0; i < STEPS; i++) {
        float safe = sDist(point);
        if (safe <= MIN_DIST || traveled >= MAX_DIST) {
            break;
        }

        traveled += safe;
        point += dir * safe;
        if (safe <= 0.01) {
            close_steps += 1;
        }
    }
    return vec2(traveled, close_steps);
}

vec3 worldPos(vec3 point) {
    vec3 ndc = point * 2.0 - 1.0;
    vec4 homPos = InverseTransformMatrix * vec4(ndc, 1.0);
    vec3 viewPos = homPos.xyz / homPos.w;

    return (inverse(ModelViewMat) * vec4(viewPos, 1.)).xyz + CameraPosition;
}

void main() {
    float depth = texture(DepthSampler, texCoord).r;
    vec3 start_point = worldPos(vec3(texCoord, 0)) - BlockPosition;
    vec3 end_point = worldPos(vec3(texCoord, depth)) - BlockPosition;
    vec3 dir = normalize(end_point - start_point);

    vec2 hit_result = raycast(start_point, dir);
    vec3 hit_point = start_point + dir * hit_result.x;

    scale = vec2(min(pow(iTime * 1.5, 2.), 1.), pow(clamp(1.5 * iTime - 1., 0., 1.), 2.));

    float threshold = step(sDist(hit_point), MIN_DIST * 2.);

    vec2 uv = texCoord - vec2(0.5);

    // cover by blocks
    threshold *= step(distance(start_point, hit_point), distance(start_point, end_point));

    float coveredByScreen = min(step(abs(uv.x), 0.45 * scale.x), step(abs(uv.y), 0.45 * scale.y)) * IsBlockHit;
    threshold *= coveredByScreen;

    float rotation = 0.2 * iTime;
    mat2 rotationMatrix = mat2(cos(rotation), -sin(rotation), sin(rotation), cos(rotation));

    vec2 AOE = end_point.xz * rotationMatrix;
    float withinAOE = length(AOE) - 24.;
    float indicator = 0.05 / min(abs(withinAOE), min(max(min(sdBox(AOE, vec2(5.5, 0.)), sdBox(AOE, vec2(0., 5.5))), -length(AOE) + 1.5), abs(length(AOE) - 2.5)));

    vec3 original = texture(DiffuseSampler, texCoord).rgb + (red * 0.03 / sDist(end_point) + blue * indicator + blue / 5. * step(withinAOE, 0.)) * coveredByScreen;

    vec3 world = mix(original, red, threshold);

    vec3 overlay = renderUi(world, sdBox(uv, vec2(0.45)));

    uv.x *= max(viewWidth, viewHeight) / min(viewWidth, viewHeight);
    uv *= rotationMatrix;
    float crosshair = max(min(sdBox(uv, vec2(0.001, 0.03)), sdBox(uv, vec2(0.03, 0.001))), -sdBox(uv, vec2(0.012)));
    overlay = max(overlay, renderUi(overlay, crosshair));

    fragColor = vec4(overlay, 1.);
}
