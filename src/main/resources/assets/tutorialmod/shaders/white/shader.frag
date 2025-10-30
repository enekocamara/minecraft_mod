#version 330 core

// === Uniforms ===
uniform vec2 iResolution;  // viewport resolution (in pixels)
//uniform float iTime;       // time (optional)
uniform float iSeed;       // seed, e.g. from world seed

out vec4 FragColor;


void main()
{
    FragColor = vec4(1.0);
}