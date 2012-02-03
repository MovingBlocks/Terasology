uniform sampler2D tex;
uniform float size;

const vec2 s1 = vec2(-1, 1);
const vec2 s2 = vec2( 1, 1);
const vec2 s3 = vec2( 1,-1);
const vec2 s4 = vec2(-1,-1);

void main() {
    vec2 texCoordSample = vec2(0.0);

	texCoordSample = gl_TexCoord[0].xy + s1 / size;
	vec4 color = texture2D(tex, texCoordSample);

	texCoordSample = gl_TexCoord[0].xy + s2 / size;
	color += texture2D(tex, texCoordSample);

	texCoordSample = gl_TexCoord[0].xy + s3 / size;
	color += texture2D(tex, texCoordSample);

	texCoordSample = gl_TexCoord[0].xy + s4 / size;
	color += texture2D(tex, texCoordSample);

	gl_FragColor = color * 0.25;
}
