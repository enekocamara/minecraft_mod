#version 330 core
precision highp float;
precision highp int;

// === Uniforms ===
uniform vec2 iTextSize;  // viewport resolution (in pixels)
//uniform float iTime;       // time (optional)
uniform float iSeed;       // seed, e.g. from world seed

out vec4 FragColor;

// --------------------------------------------------------
// Hash and noise functions
// --------------------------------------------------------

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

float detailNoise(vec2 uv, int iters)
{
    float n = 0.0;
    float freq = 1.0;
    float amp = 1.0;
    float totalAmp = 0.0;

    for(int i=0;i<iters;i++)
    {
        n += valueNoise(uv * freq) * amp;
        totalAmp += amp;
        freq *= 2.0;
        amp *= 0.5;
    }
    return n / totalAmp;
}

// --------------------------------------------------------
// Simplex noise by Ashima Arts
// --------------------------------------------------------

vec3 mod289(vec3 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec2 mod289(vec2 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec3 permute(vec3 x) { return mod289(((x*34.0)+1.0)*x); }

float snoise(vec2 v)
{
    const vec4 C = vec4(0.211324865405187,  // (3.0-sqrt(3.0))/6.0
    0.366025403784439,  // 0.5*(sqrt(3.0)-1.0)
    -0.577350269189626, // -1.0 + 2.0 * C.x
    0.024390243902439); // 1.0 / 41.0

    vec2 i  = floor(v + dot(v, C.yy));
    vec2 x0 = v - i + dot(i, C.xx);

    vec2 i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
    vec4 x12 = x0.xyxy + C.xxzz;
    x12.xy -= i1;

    i = mod289(i);
    vec3 p = permute(
        permute(i.y + vec3(0.0, i1.y, 1.0))
        + i.x + vec3(0.0, i1.x, 1.0));

    vec3 x_ = fract(p * C.w) * 2.0 - 1.0;
    vec3 h = abs(x_) - 0.5;
    vec3 ox = floor(x_ + 0.5);
    vec3 a0 = x_ - ox;

    vec2 g0 = vec2(a0.x, h.x);
    vec2 g1 = vec2(a0.y, h.y);
    vec2 g2 = vec2(a0.z, h.z);

    float t0 = 0.5 - dot(x0, x0);
    float t1 = 0.5 - dot(x12.xy, x12.xy);
    float t2 = 0.5 - dot(x12.zw, x12.zw);

    vec3 t = max(vec3(t0, t1, t2), 0.0);
    t *= t; t *= t;

    vec3 n = t * vec3(
    dot(g0, x0),
    dot(g1, x12.xy),
    dot(g2, x12.zw));

    return 70.0 * dot(n, vec3(1.0));
}

float fbm(vec2 p, int octaves, float lacunarity, float gain)
{
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;

    for (int i = 0; i < 10; i++)
    {
        if (i >= octaves) break;
        value += amplitude * snoise(p * frequency);
        frequency *= lacunarity;
        amplitude *= gain;
    }

    return value;
}

// --------------------------------------------------------
// Voronoi with noise warp
// --------------------------------------------------------

struct CellResult {
    float f1;
    float f2;
    vec2 cellPos;
    vec2 f2CellPos;
};

CellResult voronoi2(vec2 uv)
{
    vec2 warp = vec2(
    valueNoise(uv * 0.35 + 10.0),
    valueNoise(uv * 0.35 - 10.0)
    );
    uv += (warp - 0.5) * 3.0;

    vec2 g = floor(uv);
    vec2 f = fract(uv);
    float minDist = 1e9;
    float secondDist = 1e9;
    vec2 closestSeed;
    vec2 f2Seed;

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
            f2Seed = closestSeed;
            closestSeed = g + neighbor + seed;
        } else if (d < secondDist) {
            secondDist = d;
            f2Seed = g + neighbor + seed;
        }
    }

    return CellResult(minDist, secondDist, closestSeed, f2Seed);
}

// --------------------------------------------------------
// Helpers
// --------------------------------------------------------

vec2 plateDirection(vec2 cellSeed, float iSeed)
{
    vec2 h = hash2(cellSeed + iSeed * 100.0);
    float angle = h.x * 6.2831853;
    return vec2(cos(angle), sin(angle));
}

vec3 heightToColor(float h){
    vec3 color;
    if (h < 0.5) {
        float t = h / 0.5;
        color = mix(vec3(0.0, 0.0, 0.3), vec3(0.0, 0.5, 1.0), t);
    } else {
        color = vec3(0.0, 0.5, 0.0);
    }
    return color;
}

bool is_continental(vec2 pos, float continental_threshold){
    //float typeVal = fbm(floor(pos) * 10.0, 10, 1.0, 0.4);
    float typeVal = detailNoise(floor(pos) * 10.0, 10);
    return typeVal > 0.5;
}

// --------------------------------------------------------
// Main image
// --------------------------------------------------------

void mainImage(out vec4 fragColor, in vec2 fragCoord)
{
    vec2 uv = fragCoord.xy / iTextSize.xy;
    uv.y = 1.0 - uv.y;
    uv *= 10.0;

    CellResult cell = voronoi2(uv);

    //fragColor = vec4(vec3(cell.f1), 1.0);
    //return;

    bool continent_render = true;
    bool plate_dir_render = false;
    bool height_map_render = true;
    bool render_edge = false;
    float continental_threshold = 0.55;
    float noise_frequency = 15.0;

    vec3 color;

    if (continent_render)
    {
        float height = detailNoise(uv,6);

        bool isContinental = is_continental(cell.cellPos, continental_threshold);
        vec3 baseColor = isContinental ? vec3(0.65, 0.55, 0.3) : vec3(0.1, 0.3, 0.6);

        float borderWidth = 0.01;
        float edgeDist = cell.f2 - cell.f1;

        if (edgeDist < borderWidth && render_edge)
        color = vec3(0.0);
        else
        {
            if (plate_dir_render)
            {
                vec2 dir = plateDirection(cell.cellPos, iSeed);
                color = vec3(0.5 + 0.5*dir.x, 0.5 + 0.5*dir.y, 0.0);
            }
            else if (height_map_render)
            {
                float height = detailNoise(uv, 6);
                if (!isContinental)
                {
                    height = 0.0;
                }
                else
                {
                    height = clamp(height, 0.4, 0.5);
                    if (height > 0.4)
                    height = 0.5;

                    bool isF2Oceanic = !is_continental(cell.f2CellPos, continental_threshold);
                    if (isF2Oceanic)
                    height = 0.4;
                }
                //fragColor = vec4(height);
                color = heightToColor(height);
            }
            else color = baseColor;
        }
    }
    else
    {
        color = vec3(cell.f1);
    }

    fragColor = vec4(color, 1.0);
}

void main()
{
    vec4 color;
    mainImage(color, gl_FragCoord.xy);
    FragColor = color;
}