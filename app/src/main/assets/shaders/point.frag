precision highp float;
uniform vec2 aCirclePosition;
uniform float aRadius;
uniform vec4 aColor;
const float threshold = 0.005;
void main()
{
    float d, dist;
    dist = distance(aCirclePosition, gl_FragCoord.xy);
    if(dist == 0.)
    dist = 1.;
    d = aRadius / dist;
    if(d >= 1.)
    gl_FragColor = aColor;
    else if(d >= 1. - threshold)
    {
        float a = (d - (1. - threshold)) / threshold;
        gl_FragColor = vec4(aColor.r, aColor.g, aColor.b, a);
    }
    else
    gl_FragColor = vec4(0., 0., 0., 0.);
}