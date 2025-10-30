#version 330 core


// === Uniforms ===
uniform vec2 iTextSize;  // viewport resolution (in pixels)
//uniform float iTime;       // time (optional)
uniform float iSeed;       // seed, e.g. from world seed

out vec4 FragColor;


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
    mat2 stretch = mat2(1.0, 0.3, -0.2, 0.6);
    uv = stretch * uv;
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

    return CellResult(minDist,secondDist, closestSeed, f2Seed);
}

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

float remap(float value, float oldMin, float oldMax, float newMin, float newMax) {
    return newMin + (value - oldMin) * (newMax - newMin) / (oldMax - oldMin);
}

bool is_continental(vec2 pos, float continental_threshold){
    float typeVal = detailNoise(floor(pos), 10);
    return  typeVal > continental_threshold;
}

void mainImage(out vec4 fragColor, in vec2 fragCoord)
{
    vec2 uv = fragCoord.xy / iTextSize.xy;
    uv *= 10.0;

    CellResult cell = voronoi2(uv);
    bool continent_render = true;
    bool plate_dir_render = false;
    bool height_map_render = true;
    float continental_threshold = 0.5;
    float noise_frequency = 15.0;

    if (continent_render){
        bool isContinental = is_continental(cell.cellPos, continental_threshold);
        vec3 baseColor = isContinental ? vec3(0.65, 0.55, 0.3) : vec3(0.1, 0.3, 0.6);
        vec3 color;
        float borderWidth = 0.01;
        float edgeDist = cell.f2 - cell.f1;

        if (edgeDist < borderWidth && false)
            color = vec3(0.0);
        else{
            if (plate_dir_render){
                vec2 dir = plateDirection(cell.cellPos, iSeed);
                color = vec3(0.5 + 0.5*dir.x, 0.5 + 0.5*dir.y, 0.0);
            } else if (height_map_render){
                float height = detailNoise(uv,6);
                if (!isContinental){
                    height = 0.0;
                    bool isF2Continental = is_continental(cell.f2CellPos, continental_threshold);
                    if (isF2Continental){
                        float edgeDist = cell.f2 - cell.f1;
                        float smoothFactor = smoothstep(0.0, 0.1, edgeDist);
                        float targetHeight = 0.4;
                        height = mix(targetHeight, height, smoothFactor);
                    }
                }else{
                    height = clamp(height, 0.4, 0.5);
                    bool isF2Oceanic = !is_continental(cell.f2CellPos, continental_threshold);
                    if (isF2Oceanic){
                        float edgeDist = cell.f2 - cell.f1;
                        float smoothFactor = smoothstep(0.0, 0.2, edgeDist);
                        float targetHeight = 0.4;
                        height = mix(targetHeight, height, smoothFactor);
                    }
                }
                //color = heightToColor(height);
                color = vec3(height);
            } else {
                color = baseColor;
            }
        }
        fragColor = vec4(color, 1.0);
    } else {
        fragColor = vec4(vec3(cell.f1), 1.0);
    }
}

void main()
{
    vec4 color;
    mainImage(color, gl_FragCoord.xy);
    FragColor = color;
}