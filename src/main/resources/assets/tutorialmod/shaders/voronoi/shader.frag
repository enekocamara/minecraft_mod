#version 330 core

// === Uniforms ===
uniform vec2 iTextSize;  // viewport resolution (in pixels)
//uniform float iTime;       // time (optional)
uniform float iSeed;       // seed, e.g. from world seed

out vec4 FragColor;

// === Functions ===

vec2 hash2(vec2 p)
{
    p = vec2(dot(p, vec2(127.1, 311.7)),
             dot(p, vec2(269.5, 183.3)));
    return fract(sin(p) * 43758.5453123);
}

float valueNoise(vec2 p)
{
    p += iSeed * 37.0;
    vec2 i = floor(p);
    vec2 f = fract(p);

    float a = fract(sin(dot(i + vec2(0,0), vec2(27.619,57.583))) * 43758.5453);
    float b = fract(sin(dot(i + vec2(1,0), vec2(27.619,57.583))) * 43758.5453);
    float c = fract(sin(dot(i + vec2(0,1), vec2(27.619,57.583))) * 43758.5453);
    float d = fract(sin(dot(i + vec2(1,1), vec2(27.619,57.583))) * 43758.5453);

    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(a,b,u.x), mix(c,d,u.x), u.y);
}

float detailNoise(vec2 uv)
{
    float n = 0.0;
    float freq = 1.0;
    float amp = 1.0;
    for(int i=0;i<4;i++)
    {
        n += valueNoise(uv * freq) * amp;
        freq *= 2.0;
        amp *= 0.5;
    }
    return n;
}

struct CellResult {
    float f1;
    float f2;
    vec2 cellPos;
};

CellResult voronoi2(vec2 uv)
{
    vec2 warp = vec2(
        valueNoise(uv * 0.35 + 10.0),
        valueNoise(uv * 0.35 - 10.0)
    );
    uv += (warp - 0.5) * 3.0;

    mat2 stretch = mat2(1.0, 0.3, -0.2, 0.6);
    uv = stretch * uv;

    vec2 g = floor(uv);
    vec2 f = fract(uv);
    float minDist = 1e9;
    float secondDist = 1e9;
    vec2 closestSeed = vec2(0.0);

    for (int j = -1; j <= 1; ++j)
    for (int i = -1; i <= 1; ++i)
    {
        vec2 neighbor = vec2(float(i), float(j));
        vec2 seed = hash2(g + neighbor);
        vec2 diff = neighbor + seed - f;

        float p = 1.4;
        float d = pow(pow(abs(diff.x), p) + pow(abs(diff.y), p), 1.0 / p);

        if (d < minDist) {
            secondDist = minDist;
            minDist = d;
            closestSeed = g + neighbor + seed;
        } else if (d < secondDist) {
            secondDist = d;
        }
    }

    CellResult res;
    res.f1 = minDist;
    res.f2 = secondDist;
    res.cellPos = closestSeed;
    return res;
}

vec2 plateDirection(vec2 cellSeed, float seed)
{
    vec2 h = hash2(cellSeed + seed * 100.0);
    float angle = h.x * 6.2831853;
    return vec2(cos(angle), sin(angle));
}

// === Main ===

void main()
{
    vec2 uv = gl_FragCoord.xy / iTextSize.xy;
    uv *= 10.0;

    CellResult cell = voronoi2(uv);
    bool continent_render = true;
    bool plate_dir_render = false;

    if (continent_render) {
        float typeVal = valueNoise(floor(cell.cellPos) * 7.0);
        bool isContinental;
        if (typeVal > 0.6)
                isContinental = true;
        else
                isContinental = false;

        vec3 baseColor = isContinental ? vec3(0.65, 0.55, 0.3) : vec3(0.1, 0.3, 0.6);
        vec3 color;

        float borderWidth = 0.02;
        float edgeDist = cell.f2 - cell.f1;
        if (edgeDist < borderWidth)
            color = vec3(0.0);
        else {
            if (plate_dir_render) {
                vec2 dir = plateDirection(cell.cellPos, iSeed);
                color = vec3(0.5 + 0.5*dir.x, 0.5 + 0.5*dir.y, 0.0);
            } else {
                color = baseColor;
            }
        }
        FragColor = vec4(color, 1.0);
    } else {
        vec3 col = vec3(cell.f1);
        FragColor = vec4(col, 1.0);
    }
}