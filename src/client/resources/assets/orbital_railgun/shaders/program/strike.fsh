#version 330 compatibility
#define STEPS 120
#define MIN_DIST 0.001
#define MAX_DIST 2500.

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform mat4 InverseTransformMatrix;
uniform mat4 ModelViewMat;
uniform vec3 CameraPosition;
uniform vec3 BlockPosition;

uniform float iTime;

in vec2 texCoord;

out vec4 fragColor;

float smooth_min(float a, float b, float k) {
    float diff = a - b;
    return 0.5 * (a + b - sqrt(diff * diff + k * k * k));
}

float sDist(vec3 p) {
    if (iTime < 32.) {
        float scale = 1.2 * (5. / iTime - 32. * (1. - pow(clamp(iTime / 16. - 1., 0., 10.), 2.))) + 5.;
        float main_sphere = length(p) + scale;

        // https://iquilezles.org/articles/sdfrepetition/
        float rotation = 80. / (iTime - 32.);
        p.xz *= mat2(cos(rotation), -sin(rotation), sin(rotation), cos(rotation));

        const float num = 6.28 / 6.;
        float offset = 2. * -scale;
        float theta = atan(p.z, p.x);
        theta = floor(theta / num);

        float c1 = num * (theta + 0.0);
        vec3 p1 = mat3(cos(c1), 0., -sin(c1), 0., 1., 0., sin(c1), 0., cos(c1)) * p;
        float c2 = num * (theta + 1.0);
        vec3 p2 = mat3(cos(c2), 0., -sin(c2), 0., 1., 0., sin(c2), 0., cos(c2)) * p;

        p1.x -= offset;
        p2.x -= offset;

        float outer_spheres = min(length(p1) + max(scale, -3), length(p2) + max(scale, -3));
        float outer_beams = min(length(p1.xz) - 0.2, length(p2.xz) - 0.2);

        return smooth_min(main_sphere, smooth_min(outer_spheres, outer_beams, 1.), 5.);
    }

    float explosion_cylindar = length(p.xz) + 8. / (iTime - 32.) - 24.;
    return explosion_cylindar;
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

float shockwave(vec3 point) {
    float dist = sDist(point);

    float default_light = 10. / pow(dist, 2.);

    if (iTime < 32.5) {
        float speed_factor = 1. - pow(iTime / 32., 2.);
        speed_factor = clamp(5., -5., speed_factor);
        float shock = 0.05 / abs(fract(dist / 16. - iTime * speed_factor) - 0.5) * 2.;

        return default_light + shock * smoothstep(dist - 10., dist, iTime * speed_factor * 16.);
    }

    float fade_factor = clamp(5. / (iTime - 32.5) - 0.25, 0., 1.);
    return fade_factor * (default_light
            + 20. / abs(dist - 50. * (iTime - 32.5)) - 0.3
            + 5. / abs(dist - 25. * (iTime - 32.5)) - 0.2
        ) + smoothstep(dist - 10., dist, 80. * (iTime - 34.5));
}

void main() {
    float depth = texture(DepthSampler, texCoord).r;
    vec3 start_point = worldPos(vec3(texCoord, 0)) - BlockPosition;
    vec3 end_point = worldPos(vec3(texCoord, depth)) - BlockPosition;
    vec3 dir = normalize(end_point - start_point);

    vec2 hit_result = raycast(start_point, dir);
    vec3 hit_point = start_point + dir * hit_result.x;

    vec3 col = mix(vec3(0.62, 0.93, 0.93), vec3(0.), abs(sin(0.09817477042 * iTime))) + vec3(smoothstep(5., 10., hit_result.y)) * vec3(0.62, 0.93, 0.93);

    float threshold = step(sDist(hit_point), MIN_DIST * 2.);

    // cover by blocks
    threshold *= step(distance(start_point, hit_point), distance(start_point, end_point));

    threshold *= 1. - pow(clamp(iTime / 32. - 1., 0., 1.), 2.);
    vec3 original = texture(DiffuseSampler, texCoord).rgb;
    vec3 shockwave_color = mix(vec3(0.62, 0.93, 0.93), vec3(1.), clamp(iTime / 32. - 1., 0., 1.));

    fragColor = vec4(mix(original * shockwave(end_point) * shockwave_color, vec3(col), threshold), 1.);
}
