#version 330 core


// === Uniforms ===
uniform vec2 iTextSize;  // viewport resolution (in pixels)
//uniform float iTime;       // time (optional)
uniform float iSeed;       // seed, e.g. from world seed

out vec4 FragColor;

vec2 hash2(vec2 p)
{
    // simple random hash
    p = vec2(dot(p, vec2(127.1, 311.7)),
    dot(p, vec2(269.5, 183.3)));
    return fract(sin(p) * 43758.5453123);
}
/*
float voronoi(vec2 uv)
{
    vec2 g = floor(uv);
    vec2 f = fract(uv);
    float minDist = 1.0;

    // Check neighboring cells
    for (int j = -1; j <= 1; j++)
    {
        for (int i = -1; i <= 1; i++)
        {
            vec2 neighbor = vec2(float(i), float(j));
            vec2 point = hash2(g + neighbor);
            point = neighbor + point - f; // offset inside cell
            float d = length(point);
            minDist = min(minDist, d);
        }
    }
    return minDist;
}*/

float valueNoise(vec2 p)
{
    p += iSeed * 37.0;
    vec2 i = floor(p);
    vec2 f = fract(p);

    // hash four corners
    float a = fract(sin(dot(i + vec2(0,0), vec2(27.619,57.583))) * 43758.5453);
    float b = fract(sin(dot(i + vec2(1,0), vec2(27.619,57.583))) * 43758.5453);
    float c = fract(sin(dot(i + vec2(0,1), vec2(27.619,57.583))) * 43758.5453);
    float d = fract(sin(dot(i + vec2(1,1), vec2(27.619,57.583))) * 43758.5453);

    // Smooth interpolation
    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(mix(a,b,u.x), mix(c,d,u.x), u.y);
}


// High-frequency noise independent of Voronoi
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

/*
struct CellResult {
    float f1;
    float f2;
    vec2 cellPos;
    vec2 f2CellPos;
};

CellResult voronoi2(vec2 uv)
{
    // --- (1) Apply low-frequency noise warp ---
    vec2 warp = vec2(
        valueNoise(uv * 0.35 + 10.0),
        valueNoise(uv * 0.35 - 10.0)
    );
    uv += (warp - 0.5) * 3.0; // distort space

    // --- (2) Apply anisotropic transform (stretch) ---
    //mat2 stretch = mat2(1.0, 0.3, -0.2, 0.6);
    //uv = stretch * uv;

    // --- (3) Regular Voronoi computation ---
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

        // --- (4) Slightly non-Euclidean distance ---
        float p = 1.4; // Minkowski factor < 2 makes boundaries a bit flatter
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
*/

struct Neighbor {
    vec2 pos;   // seed position
    float dist; // distance to current uv
};

struct Cell {
    Neighbor cells[9];
};

/*
Cell voronoiAllNeighbors(vec2 uv)
{
    // --- (1) Low-frequency noise warp ---
    vec2 warp = vec2(
        valueNoise(uv * 0.35 + 10.0),
        valueNoise(uv * 0.35 - 10.0)
    );
    uv += (warp - 0.5) * 3.0;

    vec2 g = floor(uv);
    vec2 f = fract(uv);

    Cell cell;

    int index = 0;
    for (int j = -1; j <= 1; ++j)
    for (int i = -1; i <= 1; ++i)
    {
        vec2 neighborOffset = vec2(float(i), float(j));
        vec2 seed = hash2(g + neighborOffset);
        vec2 diff = neighborOffset + seed - f;

        float p = 1.4; // Minkowski factor
        float dist = pow(pow(abs(diff.x), p) + pow(abs(diff.y), p), 1.0 / p);

        cell.cells[index].pos = g + neighborOffset + seed;
        cell.cells[index].dist = dist;
        index++;
    }

    return cell;
}*/
/*
Cell voronoiAllNeighbors(vec2 uv)
{
    vec2 g = floor(uv);
    vec2 f = fract(uv);

    Cell cell;

    int nIndex = 0;

    for (int j = -1; j <= 1; ++j) {
        for (int i = -1; i <= 1; ++i) {
            vec2 neighborOffset = vec2(float(i), float(j));
            vec2 seed = hash2(g + neighborOffset);
            vec2 diff = neighborOffset + seed - f;

            //float p = 1.4;
            //float dist = pow(pow(abs(diff.x), p) + pow(abs(diff.y), p), 1.0 / p);
            float dist = length(diff);
            if (i == 0 && j == 0) {
                // This is our center cell
                cell.center.pos = g + neighborOffset + seed;
                cell.center.dist = dist;
            } else {
                // This is one of the 8 neighbors
                cell.cells[nIndex].pos = g + neighborOffset + seed;
                cell.cells[nIndex].dist = dist;
                nIndex++;
            }
        }
    }

    // --- Sort only neighbors by distance ---
    for (int i = 0; i < 8 - 1; ++i) {
        for (int j = i + 1; j < 8; ++j) {
            if (cell.cells[j].dist < cell.cells[i].dist) {
                Neighbor tmp = cell.cells[i];
                cell.cells[i] = cell.cells[j];
                cell.cells[j] = tmp;
            }
        }
    }

    return cell;
}*/

Cell voronoiAllNeighbors(vec2 uv)
{
    // --- Low-frequency noise warp ---
    vec2 warp = vec2(
    valueNoise(uv * 0.35 + 10.0),
    valueNoise(uv * 0.35 - 10.0)
    );
    //uv += (warp - 0.5) * 3.0;

    vec2 g = floor(uv);
    vec2 f = fract(uv);
    //uv = g;

    Cell cell;

    int index = 0;
    // Compute all 9 neighbors
    for (int j = -1; j <= 1; ++j){
        for (int i = -1; i <= 1; ++i)
        {
            vec2 neighborOffset = vec2(float(i), float(j));
            vec2 seed = hash2(g + neighborOffset);
            vec2 diff = neighborOffset + seed - f;

            //       float p = 1.4;
            //pow(pow(abs(diff.x), p) + pow(abs(diff.y), p), 1.0 / p);
            float dist = length(diff);

            cell.cells[index].pos = g + neighborOffset + seed;
            cell.cells[index].dist = dist;
            index++;
        }
    }

    // --- Simple bubble sort by distance ---
    for (int i = 0; i < 9-1; ++i)
    {
        for (int j = i+1; j < 9; ++j)
        {
            if (cell.cells[j].dist < cell.cells[i].dist)
            {
                Neighbor tmp = cell.cells[i];
                cell.cells[i] = cell.cells[j];
                cell.cells[j] = tmp;
            }
        }
    }

    return cell;
}

// Simplex noise by Ian McEwan, Ashima Arts
// https://github.com/ashima/webgl-noise

vec3 mod289(vec3 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec2 mod289(vec2 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
vec3 permute(vec3 x) { return mod289(((x*34.0)+1.0)*x); }

float snoise(vec2 v)
{
    const vec4 C = vec4(0.211324865405187,  // (3.0-sqrt(3.0))/6.0
    0.366025403784439,  // 0.5*(sqrt(3.0)-1.0)
    -0.577350269189626, // -1.0 + 2.0 * C.x
    0.024390243902439); // 1.0 / 41.0

    // First corner
    vec2 i  = floor(v + dot(v, C.yy));
    vec2 x0 = v - i + dot(i, C.xx);

    // Other corners
    vec2 i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
    vec4 x12 = x0.xyxy + C.xxzz;
    x12.xy -= i1;

    // Permutations
    i = mod289(i);
    vec3 p = permute(
        permute(i.y + vec3(0.0, i1.y, 1.0))
        + i.x + vec3(0.0, i1.x, 1.0));

    vec3 x_ = fract(p * C.w) * 2.0 - 1.0;
    vec3 h = abs(x_) - 0.5;
    vec3 ox = floor(x_ + 0.5);
    vec3 a0 = x_ - ox;

    // Normalise gradients
    vec2 g0 = vec2(a0.x, h.x);
    vec2 g1 = vec2(a0.y, h.y);
    vec2 g2 = vec2(a0.z, h.z);
    float t0 = 0.5 - dot(x0, x0);
    float t1 = 0.5 - dot(x12.xy, x12.xy);
    float t2 = 0.5 - dot(x12.zw, x12.zw);

    vec3 t = max(vec3(t0, t1, t2), 0.0);
    t = t * t;
    t = t * t;

    vec3 n = t * vec3(
    dot(g0, x0),
    dot(g1, x12.xy),
    dot(g2, x12.zw));

    // Final noise value
    return 70.0 * dot(n, vec3(1.0));
}

// Fractal Brownian Motion (FBM) with octave control
float fbm(vec2 p, int octaves, float lacunarity, float gain)
{
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;

    for (int i = 0; i < 10; i++) // Max 10 octaves for safety
    {
        if (i >= octaves) break;
        value += amplitude * snoise(p * frequency);
        frequency *= lacunarity;
        amplitude *= gain;
    }

    return value;
}

vec2 plateDirection(vec2 cellSeed, float iSeed)
{
    // deterministic hash from cell position + global seed
    vec2 h = hash2(cellSeed + iSeed * 100.0);

    // map to direction vector
    float angle = h.x * 6.2831853; // 0..2π
    return vec2(cos(angle), sin(angle)); // unit vector
}

vec3 heightToColor(float h){
    vec3 color;

    if (h < 0.5) {
        // Ocean: dark blue to light blue
        float t = h / 0.5;
        color = mix(vec3(0.0, 0.0, 0.3), vec3(0.0, 0.5, 1.0), t);
    } else
    color = vec3(0.0, 0.5, 0.0);
    return color;
}

float remap(float value, float oldMin, float oldMax, float newMin, float newMax) {
    return newMin + (value - oldMin) * (newMax - newMin) / (oldMax - oldMin);
}

bool is_continental(vec2 pos, float continental_threshold){
    //float typeVal = fbm(floor(pos) * 10.0, 10, 1.0,0.4); // low-freq correlation
    float typeVal = detailNoise(floor(pos) * 10.0, 10);
    return  typeVal > 0.5;//continental_threshold;
}

struct Features {
    float uplift;       // 0..1, amount of upward motion
    float subduction;   // 0..1, downward motion
    float volcano;      // 0..1, likelihood of volcanic activity
    float rift;         // 0..1, stretching / thinning
    float shear;        // 0..1, lateral sliding stress
};

float mountainFalloff(float dist, float edgeStart, float peakPos, float edgeEnd)
{
    // dist = distance to neighbor seed, normalized if you want 0..1
    // edgeStart = distance at which uplift starts (0 = edge)
    // peakPos   = distance at which uplift is maximum
    // edgeEnd   = distance beyond which uplift drops to 0

    if (dist < edgeStart || dist > edgeEnd) return 0.0;

    if (dist < peakPos)
    {
        // rising part: 0 at edgeStart to 1 at peakPos
        float t = (dist - edgeStart) / (peakPos - edgeStart);
        return pow(t, 2.0); // exponent controls sharpness of rise
    }
    else
    {
        // falling part: 1 at peakPos to 0 at edgeEnd
        float t = (dist - peakPos) / (edgeEnd - peakPos);
        return pow(1.0 - t, 2.0); // exponent controls sharpness of fall
    }
}

float boost(float x, float power)
{
    // x in 0..1
    return pow(x, 1.0 / power); // power > 1 boosts high values
}

float plateAlignment(Cell cell, vec2 uv, float iSeed)
{
    float uplift = 0.0;
    uv = cell.cells[0].pos;

    for (int n = 0; n < 8; ++n)
    {
        vec2 neighborPos = cell.cells[n].pos;
        float dist = cell.cells[n].dist;

        vec2 dirToNeighbor = normalize(neighborPos - uv);

        // Neighbor's plate direction
        vec2 plateDir = plateDirection(neighborPos, iSeed);

        float alignment = clamp(dot(-plateDir, dirToNeighbor), 0.0, 1.0);
        //uplift += alignment;
        //continue;

        float maxDist = 5.0; // adjust to control "range of influence"

        // normalize distance so 0 = center, 1 = max effective distance
        float normalizedDist = clamp(dist / maxDist, 0.0, 1.0);

        // apply hard falloff
        float exponent;
        float peakPos;
        float edgeEnde;

        float boost_val;
        if (is_continental(cell.cells[0].pos, iSeed) && is_continental(cell.cells[n].pos, iSeed)){
            exponent = 2.0;
            peakPos = 0.4;
            edgeEnde = 0.5;
            boost_val = 3.0f;
        }
        else{
            exponent = 4.0;
            peakPos = 0.2;
            edgeEnde = 0.3;
            boost_val = 0.5;
        }

        float falloff = mountainFalloff(normalizedDist, 0.05, peakPos, edgeEnde);
        //float falloff = pow(1.0 - normalizedDist, exponent); // higher exponent = sharper drop
        //falloff = 1.0;
        uplift += boost(alignment, boost_val) * falloff;
        //alignment = falloff;
    }

    // Optional: normalize by max possible contribution (9 neighbors)
    uplift /= 8.0;

    return uplift; // 0..1
}

void mainImage(out vec4 fragColor, in vec2 fragCoord)
{
    vec2 uv = fragCoord.xy / iTextSize.xy;

    uv *= 10.0; // zoom level

    Cell  cell = voronoiAllNeighbors(voronoiAllNeighbors(uv).cells[0].pos);

    for (int i = 0; i < 9; i++){
        cell.cells[i].dist = length(cell.cells[i].pos - uv);
    }
    Cell ordered = cell;

    for (int i = 1; i < 9-1; ++i)
    {
        for (int j = i+1; j < 9; ++j)
        {
            if (ordered.cells[j].dist < ordered.cells[i].dist)
            {
                Neighbor tmp = ordered.cells[i];
                ordered.cells[i] = ordered.cells[j];
                ordered.cells[j] = tmp;
            }
        }
    }

    bool continent_render = true;
    bool plate_dir_render = false;
    bool height_map_render = true;
    bool render_edge = false;
    bool render_features = true;
    float continental_threshold = 0.55;
    float noise_frequency = 15.0;

    if (continent_render){
        // --- determine cell type ---


        //


        //vec3 baseColor = isContinental ? vec3(0.65, 0.55, 0.3) : vec3(0.1, 0.3, 0.6);
        vec3 color;

        float borderWidth = 0.01;
        float edgeDist = cell.cells[1].dist - cell.cells[0].dist;
        if (edgeDist < borderWidth && render_edge)
        color = vec3(1.0); // black border
        else{

            int idx = int(mod(iTime, 9.0));  // cycles 0..8
            color = vec3(cell.cells[idx].pos / 10.0, 0.0);
            fragColor = vec4(color, 1.0);
            return;
            if (plate_dir_render){
                vec2 dir = plateDirection(cell.cells[0].pos, iSeed);
                color = vec3(0.5 + 0.5*dir.x, 0.5 + 0.5*dir.y, 0.0);
            } else if (height_map_render){
                float height = detailNoise(uv,6);

                bool isContinental = is_continental(cell.cells[0].pos, continental_threshold);

                if (!isContinental){
                    height = 0.0;

                }else{
                    height = clamp(height, 0.4, 0.5);
                    if (height > 0.4)
                    height = 0.5;


                    bool isF2Oceanic = !is_continental(ordered.cells[1].pos, continental_threshold);
                    if (isF2Oceanic && ordered.cells[1].dist - cell.cells[0].dist < 0.6){
                        height = 0.4;

                    }


                }
                if (render_features){
                    float alignment = plateAlignment(cell, uv, iSeed);
                    vec3 mountain = vec3(alignment,0.0,0.0);
                    //fragColor = vec4(mountain,1.0);
                    //return;
                    if (alignment > 0.0){
                        vec3 mountain = vec3(alignment,0.0,0.0);
                        //fragColor = vec4(mountain,1.0);
                        //return;
                        vec3 color = heightToColor(height);
                        float factor = 0.1;
                        color.r = mix(mountain.r, color.r, factor);
                        //color.gb = color.gb / 2.0;
                        fragColor = vec4(color,1.0);
                        return;
                    }
                }
                color = heightToColor(height);
            }
        }
        fragColor = vec4(color, 1.0);

    }else{


        //  fragColor = vec4(col, 1.0);
    }
}