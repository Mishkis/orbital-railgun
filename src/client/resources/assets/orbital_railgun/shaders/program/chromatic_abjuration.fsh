#version 330 compatibility

uniform sampler2D DiffuseSampler;
uniform vec3 CameraPosition;

uniform vec3 BlockPosition;

uniform float iTime;

in vec2 texCoord;
in float viewHeight;
in float viewWidth;

out vec4 fragColor;

vec2 rotate(vec2 p, float r) {
    return mat2(cos(r), -sin(r), sin(r), cos(r)) * p;
}

void main() {
    float frameTimeCounter = max(iTime - 37., 0.);

    vec3 original = texture(DiffuseSampler, texCoord).rgb;
    vec2 one_pixel = vec2(1. / viewWidth, 1. / viewHeight);
    vec2 rotated_pixel = rotate(one_pixel, -frameTimeCounter);

    float scale = max((-pow((frameTimeCounter - 0.84) * 8., 2.) + 50.) * 25. / (distance(CameraPosition, BlockPosition) - 24. + 25.), 0.);
    float ca_red = texture(DiffuseSampler, texCoord + (rotated_pixel) * scale).r;
    rotated_pixel = rotate(rotated_pixel, 2.09439510239);
    float ca_green = texture(DiffuseSampler, texCoord + (rotated_pixel - one_pixel) * scale).g;
    rotated_pixel = rotate(rotated_pixel, 2.09439510239);
    float ca_blue = texture(DiffuseSampler, texCoord + (rotated_pixel - one_pixel) * scale).b;

    fragColor = vec4(mix(original, vec3(ca_red, ca_green, ca_blue), 2. * length(texCoord - vec2(0.5))), 1.);
}
